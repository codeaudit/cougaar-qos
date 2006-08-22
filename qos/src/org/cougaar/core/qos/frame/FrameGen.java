/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.core.qos.frame;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class parses frame protocol xml files and generates classes
 * for each prototype.  It has a main method and is intended to be run
 * standalone.  The parsing is SAX.
 */
public class FrameGen
extends DefaultHandler
{
    private static final String DRIVER_PROPERTY = "org.xml.sax.driver";
    private static final String DRIVER_DEFAULT =
	"org.apache.crimson.parser.XMLReaderImpl";
    private static final String MetricPath = "metric-path";
    
    static {
	String driver = System.getProperty(DRIVER_PROPERTY);
	if (driver == null) 
	    System.setProperty(DRIVER_PROPERTY, DRIVER_DEFAULT);
    }
    
    private static final String NoWarn = "__NoWarn";
    private static final String AsObject = "__AsObject";
    private static final String Metric_Type = "Metric";

    // Copyright notice
    private boolean inCopyright;
    private String copyright;
    
    // The FrameSet's package
    private String package_name;

    // The FrameSet's domain
    private String domain;

    // Path to the root of the output trees
    private String java_output_root;
    private String jess_output_root;
    private String dtd_output_root;
    private String structs; // subpackage for struct classes

    // The prototype currently being parsed
    private String current_prototype;

    // Temporary storage during prototype parsing
    private Map<String,Attributes> slots;

    // Prototype name -> prototype attributes
    private Map<String,Attributes> proto_attrs; 

    // Prototype name -> Map slot name -> slot attributes
    private Map<String,Map<String,Attributes>> proto_slots; 

    // The path currently being parsed
    private String current_path;

    // Path name -> path attrs (Attributes)
    private Map<String,Attributes> path_attrs;

    // Path name -> forks (List)
    private Map<String,List<Attributes>> path_forks;

    // Path name -> slot (String)
    private Map<String,String> path_slots;
    
    // The prototype xml from which we're generating code
    private File xml_file;



    // All defined relations
    private Set<String> relation_prototypes;

    public FrameGen(String java_path, String clp_path, String dtd_path, String structs) {
	this.java_output_root = java_path;
	this.jess_output_root = clp_path;
	this.dtd_output_root = dtd_path;
	this.structs = structs;
    }

    public void parseProtoFile(File xml_file) {
	this.xml_file = xml_file;
	proto_slots = new HashMap<String,Map<String,Attributes>>();
	proto_attrs = new HashMap<String,Attributes>();
	path_attrs = new HashMap<String,Attributes>();
	path_forks = new HashMap<String,List<Attributes>>();
	path_slots = new HashMap<String,String>();
	relation_prototypes = new HashSet<String>();
	try {
	    XMLReader producer = XMLReaderFactory.createXMLReader();
	    DefaultHandler consumer = this; 
	    producer.setContentHandler(consumer);
	    producer.setErrorHandler(consumer);
	    URL url = xml_file.toURL();
	    producer.parse(url.toString());
	} catch (Throwable ex) {
	    ex.printStackTrace();
	    return;
	}

	if (java_output_root != null) {
	    generatePrototypes(package_name, java_output_root);
	} else {
	    generatePrototypes(package_name, "src");
	}
	if (jess_output_root != null) {
	    generateShadowClasses(package_name, jess_output_root, domain);
	}
	if (dtd_output_root != null) {
	    generateDTD(domain, dtd_output_root);
	} else {
	    generateDTD(domain, xml_file.getParentFile().getPath());
	}
    }




    
    // SAX
    public void startElement(String uri, String local, String name, Attributes attrs) {
	if (name.equals("frameset")) {
	    startFrameset(attrs);
	} else if (name.equals("prototypes")) {
	    // no-op
	} else if (name.equals("prototype")) {
	    startPrototype(attrs);
	} else if (name.equals("relation-prototype")) {
	    startRelationPrototype(attrs);
	} else if (name.equals("path")) {
	    startPath(attrs);
	} else if (name.equals("fork")) {
	    List<Attributes> forks = path_forks.get(current_path);
	    forks.add(new AttributesImpl(attrs));
	} else if (name.equals("slot")) {
	    slot(attrs);
	} else if (name.equals("slot-reference")) {
	    slot_reference(attrs);
	} else if (name.equals("copyright")) {
	   inCopyright = true;
	}
    }

    public void endElement(String uri, String local, String name) {
	if (name.equals("frameset")) {
	    endFrameset();
	} else if (name.equals("prototypes")) {
	    // no-op
	} else if (name.equals("prototype")) {
	    endPrototype();
	} else if (name.equals("relation-prototype")) {
	    endRelationPrototype();
	} else if (name.equals("path")) {
	    endPath();
	} else if (name.equals("copyright")) {
	    inCopyright = false; 
	}
    }

    public void characters(char buf[], int offset, int length) {
	if (inCopyright) {
	    String text = new String(buf, offset, length);
	    if (copyright == null) {
		copyright = text;
	    } else {
		copyright += text;
	    }
	}
    }




    // Parsing 

    private void startFrameset(Attributes attrs) {
	package_name = attrs.getValue("package");
	domain = attrs.getValue("domain");

	String inheritance = attrs.getValue("frame-inheritance");
	if (!inheritance.equals("single")) {
	    throw new RuntimeException("Only single-inheritance FrameSets are supported!");
	}

    }

    private void startPrototype(Attributes attrs) {
	current_prototype = attrs.getValue("name");
	proto_attrs.put(current_prototype, new AttributesImpl(attrs));
	slots = new HashMap();
    }

    private void endPrototype() {
	proto_slots.put(current_prototype, slots);
	current_prototype = null;
	slots = null;
    }


    private void startRelationPrototype(Attributes attrs) {
	current_prototype = attrs.getValue("name");
	proto_attrs.put(current_prototype, new AttributesImpl(attrs));
	relation_prototypes.add(current_prototype);
	slots = new HashMap();
    }

    private void endRelationPrototype() {
	proto_slots.put(current_prototype, slots);
	current_prototype = null;
	slots = null;
    }

    private void startPath(Attributes attrs) {
	current_path = attrs.getValue("name");
	path_attrs.put(current_path, attrs);
	path_forks.put(current_path, new ArrayList<Attributes>());
    }

    private void endPath() {
	current_path = null;
    }

    private void slot(Attributes attrs) {
	if (slots != null) {
	    String slot = attrs.getValue("name");
	    slots.put(slot, new AttributesImpl(attrs));
	}
    }

    private void slot_reference(Attributes attrs) {
	if (current_path != null) {
	    String slot = attrs.getValue("name");
	    path_slots.put(current_path, slot);
	}
    }

    private void endFrameset() {
    }








    // Code Generation 
    private enum FileType {
	JAVA,
	CLP,
	DTD
    }
    
    private void generateCopyright(PrintWriter writer, FileType extension) {
	Date date = new Date(System.currentTimeMillis());
	DateFormat dformat = DateFormat.getDateTimeInstance();
	String[] comments =  { 
		"Generated by Cougaar QoS FrameGen",
		"  from " +xml_file,
		"  at "+ dformat.format(date)
	};
	StringTokenizer tk = null;
	if (copyright != null) {
	    tk = new StringTokenizer(copyright, "\n");
	}
	switch (extension) {
	case JAVA:
	    writer.println("/*");
	    for (String comment : comments) {
		writer.print(" * ");
		writer.println(comment);
	    }
	    if (tk != null) {
		writer.println(" *");
		while (tk.hasMoreTokens()) {
		    writer.print(" * ");
		    writer.println(tk.nextToken());
		}
		writer.println(" *");
	    }
	    writer.println(" */");
	    break;

	case CLP:
	    for (String comment : comments) {
		writer.print(";; ");
		writer.println(comment);
	    }
	    if (tk != null) {
		writer.println(";;");
		while (tk.hasMoreTokens()) {
		    writer.print(";; ");
		    writer.println(tk.nextToken());
		}
		writer.println(";;");
	    }
	    break;

	case DTD:
	    for (String comment : comments) {
		writer.print("<!-- ");
		writer.print(comment);
		writer.println(" -->");
	    }
	    if (tk != null) {
		writer.println();
		while (tk.hasMoreTokens()) {
		    writer.print("<!-- ");
		    writer.print(tk.nextToken());
		    writer.println(" -->");
		}
		writer.println();
	    }
	    break;
	}
    }

    private void generateDTD(String domain, String root) {
	File out = new File(root, domain+".dtd");
	PrintWriter writer = null;
	try {
	    FileWriter fw = new FileWriter(out);
	    writer = new PrintWriter(fw);
	} catch (java.io.IOException iox) {
	    iox.printStackTrace();
	    System.exit(-1);
	}

	writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	generateCopyright(writer, FileType.DTD);
	writer.print("<!ELEMENT "+domain+" (");
	boolean first = true;
	for (String proto : proto_slots.keySet()) {
	    if (!first) writer.print(" | ");
	    first = false;
	    writer.print(proto);
	}
	writer.println(")*>");

	for (String proto : proto_slots.keySet()) {
	    write_prototype_dtd(writer, proto);
	}

	writer.close();
	System.out.println("Wrote " + out);
    }

    private void write_slots_dtd(PrintWriter writer, String proto, Map<String,Attributes> slots) {
	writer.print("<!ATTLIST " +proto);
	for (Map.Entry<String,Attributes> slot : slots.entrySet()) {
	    Attributes attrs = slot.getValue();
	    boolean readonly = "true".equalsIgnoreCase(attrs.getValue("immutable"));
	    writer.print("\n          " +slot.getKey()+ " CDATA ");
	    if (readonly) {
		writer.print("#REQUIRED");
	    } else {
		writer.print("#IMPLIED");
	    }
	}
	writer.println(">");
    }

    private void collectProtoSlots(String proto, Map<String,Attributes> slots) {
	Map<String,Attributes> local_slots = proto_slots.get(proto);
	if (local_slots != null) {
	    for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
		slots.put(entry.getKey(), entry.getValue());
	    }
	}
	Attributes attrs = proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	if (parent != null) collectProtoSlots(parent, slots);
    }

    private void write_prototype_dtd (PrintWriter writer, String proto) {
	writer.println("<!ELEMENT " +proto+ " EMPTY>");
	Map<String,Attributes> slots = new HashMap<String,Attributes>();
	collectProtoSlots(proto, slots);
	if (!slots.isEmpty()) write_slots_dtd(writer, proto, slots);
    }

    private void generateShadowClass(PrintWriter writer, String proto, Set<String> generated) {
        if (generated.contains(proto)) {
            return;
        }
        generated.add(proto);
        String base = proto_attrs.get(proto).getValue("prototype");
        if (base != null && !generated.contains(base)) {
            generateShadowClass(writer, base, generated);
        }
        String class_name = fixName(proto, true);
        writer.print("(defclass " +proto+ " " +package_name+ "." +class_name);
        if (base != null) {
            writer.print(" extends " + base);
        }
        writer.println(")");
    }

    private void generateShadowClasses(String pkg, String root, String domain) {
	File out = new File(root, domain+".clp");
	PrintWriter writer = null;
	try {
	    FileWriter fw = new FileWriter(out);
	    writer = new PrintWriter(fw);
	} catch (java.io.IOException iox) {
	    iox.printStackTrace();
	    System.exit(-1);
	}
	generateCopyright(writer, FileType.CLP);
        Set<String> generated = new HashSet<String>();
	for (String proto : proto_slots.keySet()) {
            generateShadowClass(writer, proto, generated);
	}
	writer.println("(defclass frame-change org.cougaar.core.qos.frame.Frame$Change)");
	writer.close();
	System.out.println("Wrote " + out);
    }

    private void generatePrototypes(String pkg, String root) {
	String output_dir = root + File.separator+pkg.replace('.', File.separatorChar);
	for (Map.Entry<String,Map<String,Attributes>> entry : proto_slots.entrySet()) {
	    String prototype = entry.getKey();
	    Attributes attrs = proto_attrs.get(prototype);
	    String parent = attrs.getValue("prototype");
	    String container = attrs.getValue("container");
	    String doc = attrs.getValue("doc");
	    Map<String,Attributes> slots = entry.getValue();
	    Map<String,Attributes> override_slots = new HashMap<String,Attributes>();
	    Map<String,Attributes> local_slots = new HashMap<String,Attributes>();
	    Map<String,String> units_override_slots = new HashMap<String,String>();


	    for (Map.Entry<String,Attributes> entry2 : slots.entrySet()) {
		String slot = entry2.getKey();
		Attributes slot_attrs = entry2.getValue();
		if (inheritsSlot(prototype, slot)) {
		    override_slots.put(slot, slot_attrs);
		    String units = slot_attrs.getValue("units");
		    if (units != null) 
			units_override_slots.put(slot, units);
		} else {
		    local_slots.put(slot, slot_attrs);
		}
	    }


	    generatePrototype(prototype, output_dir, pkg,
		    doc,
		    parent, container, 
		    local_slots, override_slots,
		    units_override_slots);
	}
    }

    private void generatePrototype(String prototype,
	    String output_directory,
	    String pkg,
	    String doc,
	    String parent, 
	    String container,
	    Map<String,Attributes> local_slots,
	    Map<String,Attributes> override_slots,
	    Map<String,String> units_override_slots) {
	String name = fixName(prototype, true);
	File out = new File(output_directory, name+".java");
	
	PrintWriter writer = null;
	try {
	    FileWriter fw = new FileWriter(out);
	    writer = new PrintWriter(fw);
	} catch (java.io.IOException iox) {
	    iox.printStackTrace();
	    System.exit(-1);
	}
	boolean importMetrics = false;
	
	for (Attributes attrs : local_slots.values()) {
	    if (attrs.getValue(MetricPath) != null) {
		importMetrics = true;
		break;
	    }
	}
	if (!importMetrics) {
	    for (Attributes attrs : override_slots.values()) {
		if (attrs.getValue(MetricPath) != null) {
		    importMetrics = true;
		    break;
		}
	    }
	}
	Set<String> container_slots = new HashSet<String>();
	if (container != null) {
	    Map<String,Attributes> container_accessors = collectContainerSlots(container);
	    for (Map.Entry<String,Attributes> entry : container_accessors.entrySet()) {
		String slot = entry.getKey();
		if(!local_slots.containsKey(slot) && !inheritsSlot(prototype, slot)) {
		    container_slots.add(slot);
		    importMetrics = importMetrics || entry.getValue().getValue(MetricPath) != null;
		}
	    }
	}
	boolean importSlotDescriptions = !local_slots.isEmpty() || !container_slots.isEmpty();
	writeDecl(writer, prototype, doc, parent, importMetrics, importSlotDescriptions);
	writeRegisterer(writer, pkg, prototype);
	writeSlots(writer, prototype, local_slots);
	writeConstructors(writer, prototype, local_slots);
	writeDisplayString(writer, prototype);
	writeKind(writer, prototype);
	writeCollector(writer, prototype, local_slots, container_slots);
	if (relation_prototypes.contains(prototype))
	    writeRelationAccessors(writer, prototype);
	writeAccessors(writer, prototype, local_slots, override_slots);
	if (container != null) {
	    writeContainerReaders(writer, container, container_slots);
	    writeUpdaters(writer, prototype, container_slots, container);
	}
	writeDynamicAccessors(writer, prototype, parent,local_slots, container);
	writePostInitializer(writer, prototype, local_slots);
	writeSlotList(writer, prototype, container, local_slots, container_slots);
	writeDescriptionGetters(writer, prototype, container,
		local_slots, 
		container_slots, 
		units_override_slots);
	
	if (structs != null) {
	    // TODO: write toStruct method?
	}
	
	writer.println("}");

	writer.close();
	System.out.println("Wrote " +out);
	
	if (structs != null) {
	    String structs_pkg = pkg +"."+ structs;
	    File structs_dir = new File(output_directory, structs);
	    if (!structs_dir.exists()) {
		structs_dir.mkdir();
	    }
	    File sout = new File(structs_dir, name + ".java");
	    PrintWriter swriter = null;
	    try {
		FileWriter fw = new FileWriter(sout);
		swriter = new PrintWriter(fw);
	    } catch (java.io.IOException iox) {
		iox.printStackTrace();
		System.exit(-1);
	    }
	    writeStructDecl(swriter, prototype, structs_pkg, doc, parent);
	    writeStructSlots(swriter, prototype, local_slots, container_slots);
	    writeStructAccessors(swriter, prototype, local_slots, container_slots);
	    swriter.println("}");
	    swriter.close();
	    System.out.println("Wrote " +sout);
	}
    }
    
    private void writeStructDecl(PrintWriter writer,
	    String prototype, 
	    String pkg,
	    String doc,
	    String parent) {
	String name = fixName(prototype, true);
	generateCopyright(writer, FileType.JAVA);
	writer.println("package " +pkg+ ";\n");
	if (doc != null) {
	    writer.println("\n/**");
	    writer.println(doc);
	    writer.print("*/");
	}
	writer.print("\npublic class " +name+ " ");
	if (parent != null) {
	    writer.println("extends " +fixName(parent, true)+ " {");
	} else {
	    writer.println("{");
	}
    }

    private void writeDecl(PrintWriter writer,
	    String prototype, 
	    String doc,
	    String parent,
	    boolean importMetrics,
	    boolean importSlotDescriptions) {
	generateCopyright(writer, FileType.JAVA);
	
	boolean is_root_relation = 
	    parent == null && relation_prototypes.contains(prototype);
	String name = fixName(prototype, true);
	writer.println("package " +package_name+ ";\n");
	writer.println("import org.cougaar.core.qos.frame.DataFrame;");
	writer.println("import org.cougaar.core.qos.frame.FrameSet;");
	if (is_root_relation) {
	    writer.println("import org.cougaar.core.qos.frame.RelationFrame;");
	}
	if (importSlotDescriptions) {
	    writer.println("import org.cougaar.core.qos.frame.SlotDescription;");
	}
	if (importMetrics) {
	    writer.println("import org.cougaar.core.qos.metrics.Metric;");
	}
	writer.println("import org.cougaar.core.util.UID;");
	if (doc != null) {
	    writer.println("\n/**");
	    writer.println(doc);
	    writer.print("*/");
	}
	writer.println("\npublic class " +name);
	if (is_root_relation) {
	    writer.println("    extends RelationFrame {");
	} else if (parent != null) {
	    writer.println("    extends " +fixName(parent, true)+ " {");
	} else {
	    writer.println("    extends DataFrame {");
	}
    }

    private void writeStructSlots(PrintWriter writer,
	    String prototype, 
	    Map<String,Attributes> local_slots,
	    Set<String> container_slots) {
	for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
	    String slot = entry.getKey();
	    String fixed_name = fixName(slot, false);
	    writer.print("    private");
	    String type = getSlotType(prototype, slot);
	    if (type.equals(Metric_Type)) {
		type = "Object";
	    } else if (proto_attrs.containsKey(type)) {
		type = fixName(type, true);
	    }
	   
	    writer.println(" "+type+" " +fixed_name+ ";");
	}
	for (String slot : container_slots) {
	    String fixed_name = fixName(slot, false);
	    writer.print("    private");
	    String type = getSlotType(prototype, slot);
	    if (type.equals(Metric_Type)) {
		type = "Object";
	    } else if (proto_attrs.containsKey(type)) {
		type = fixName(type, true);
	    }
	    writer.println(" "+type+" " +fixed_name+ ";"); 
	}
    }
    
    
    private void writeSlots(PrintWriter writer,
	    String prototype, 
	    Map<String,Attributes> local_slots) {
	for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
	    String slot = entry.getKey();
	    Attributes attrs = entry.getValue();
	    writeSlot(writer, prototype, slot, attrs);
	}
    }

    private void writeSlot(PrintWriter writer, 
	    String prototype,
	    String  slot, 
	    Attributes attrs) {
	String type = getSlotType(prototype, slot);
	if (proto_attrs.containsKey(type)) type = fixName(type, true);
	boolean memberp = isMember(prototype, slot);
	boolean transientp = isTransient(prototype, slot);
	if (memberp) {
	    String fixed_name = fixName(slot, false);
	    writer.print("    private");
	    if (transientp) writer.print(" transient");
	    writer.println(" "+type+" " +fixed_name+ ";");
	}
    }

    private void writeRegisterer(PrintWriter writer,
	    String pkg,
	    String prototype) {
	String classname = fixName(prototype, true);
	writer.println("    static {");
	writer.println("        org.cougaar.core.qos.frame.FrameMaker __fm = ");
	writer.println("            new org.cougaar.core.qos.frame.FrameMaker() {");
	writer.println("                public DataFrame makeFrame(FrameSet frameSet, UID uid) {");
	writer.println("                     return new " +classname+ "(frameSet, uid);");
	writer.println("                }");
	writer.println("            };");
	writer.println("            DataFrame.registerFrameMaker(\""
		+pkg+ "\", \"" +prototype+ "\", __fm);");
	writer.println("    }");
    }

    private void writeConstructors(PrintWriter writer,
	    String prototype,
	    Map<String,Attributes> slots) {
	// Define values for inherited slots!
	String cname = fixName(prototype, true);

	writer.println("\n\n    public " +cname + "(UID uid) {");
	writer.println("        this(null, uid);");
	writer.println("    }");

	writer.println("\n\n    public " +cname + "(FrameSet frameSet,");
	int spaces = 12 + cname.length();
	for (int i=0; i<spaces; i++) writer.print(' ');
	writer.println("UID uid) {");
	writer.println("        super(frameSet, uid);");
	for (Map.Entry<String,Attributes> entry : slots.entrySet()) {
	    String slot = entry.getKey();
	    if (!isMember(prototype, slot)) continue;
	    Attributes attrs = entry.getValue();
	    String value = attrs.getValue("default-value");
	    String type = getSlotType(prototype, slot);
	    if (value != null) {
		String method = "initialize" + fixName(slot, true);
		String vstr = simpleValue(type, value);
		writer.println("        " +method+ "(" +vstr+ ");");
	    }
	}
	writer.println("    }");

    }
    
    private void writeDisplayString(PrintWriter writer, String prototype) {
	String slot = proto_attrs.get(prototype).getValue("displaySlot");
	if (slot != null) {
	    String accessor_name = fixName(slot, true);
	    writer.println("    protected String displayString() {");
	    writer.println("        return get" +accessor_name+ "();");
	    writer.println("    }");
	}
    }

    private void writeCollector(PrintWriter writer,
	    String prototype, 
	    Map<String,Attributes> local_slots,
	    Set<String> container_slots) {
	String props = "__props";
	String val = "__value";
	Set<Map.Entry<String,Attributes>> local_slots_entries = local_slots.entrySet();
	if (!local_slots_entries.isEmpty()) {
	    writer.println("\n\n    protected void collectSlotValues(java.util.Properties "
		    +props+ ") {");
	    writer.println("        super.collectSlotValues(__props);");
	    writer.println("        Object " +val+ ";");
	    for (Map.Entry<String,Attributes> entry : local_slots_entries) {
		String slot = entry.getKey();
		String getter = "get" + fixName(slot, true) +AsObject;
		writer.println("        " +val+ " = " +getter+ "();");
		writer.println("        " +props+ ".put(\"" +slot+ "\", "
			+val+ " != null ? " +val+ " : NIL);");
	    }
	    writer.println("    }");
	}

	if (!container_slots.isEmpty()) {
	    writer.println("\n\n    protected void collectContainerSlotValues(java.util.Properties "
		    +props+ ") {");
	    writer.println("        super.collectContainerSlotValues(__props);");
	    writer.println("        Object " +val+ ";");
	    for (String slot : container_slots) {
		String getter = "get" +fixName(slot, true)+ AsObject;
		writer.println("        " +val+ " = " +getter+ "();");
		writer.println("        " +props+ ".put(\"" +slot+ "\", " 
			+val+ " != null ? " +val+ " : NIL);");
	    }
	    writer.println("    }");
	}
    }

    private void writeKind(PrintWriter writer, String prototype) {
	writer.println("\n\n    public String getKind() {");
	writer.println("        return \"" +prototype+ "\";");
	writer.println("    }");
    }
    
    private void writeStructAccessors(PrintWriter writer,
	    String prototype, 
	    Map<String,Attributes> local_slots,
	    Set<String> container_slots) {
	for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
	    String slot = entry.getKey();
	    String sname = fixName(slot, false);
	    String fixed_name = fixName(slot, true);
	    String type = getSlotType(prototype, slot);
	    if (type.equals(Metric_Type)) {
		type = "Object";
	    } else if (proto_attrs.containsKey(type)) {
		type = fixName(type, true);
	    }
	    String prefix = "get";
	    if (type.equalsIgnoreCase("boolean")) {
		prefix = "is";
	    } 

	    writer.println();
	    writer.println("    public " +type+" " +prefix+fixed_name+ "() {");
	    writer.println("        return " + sname+ ";");
	    writer.println("    }");
	    writer.println();
	    writer.println("    public void set" +fixed_name+ "("+type+" " +sname+ ") {");
	    writer.println("        this." + sname+ " = " +sname+ ";");
	    writer.println("    }");
	}
	for (String slot : container_slots) {
	    String sname = fixName(slot, false);
	    String fixed_name = fixName(slot, true);
	    String type = getSlotType(prototype, slot);
	    if (type.equals(Metric_Type)) {
		type = "Object";
	    } else if (proto_attrs.containsKey(type)) {
		type = fixName(type, true);
	    }
	    String prefix = "get";
	    if (type.equalsIgnoreCase("boolean")) {
		prefix = "is";
	    }

	    writer.println();
	    writer.println("    public " +type+" " +prefix+fixed_name+ "() {");
	    writer.println("        return " + sname+ ";");
	    writer.println("    }");
	    writer.println();
	    writer.println("    public void set" +fixed_name+ "("+type+" " +sname+ ") {");
	    writer.println("        this." + sname+ " = " +sname+ ";");
	    writer.println("    }"); 
	}
    }

    private void writeRelationAccessor(PrintWriter writer, 
	    Attributes attrs,
	    String attr) {
	String value = attrs.getValue(attr);
	if (value == null) return;

	writer.println("\n\n    public String get" +fixName(attr, true)+
	"() {");
	writer.println("        return \"" +value+ "\";");
	writer.println("    }");
    }

    private void writeRelationAccessors(PrintWriter writer, String prototype) {
	Attributes attrs = proto_attrs.get(prototype);
	if (attrs == null) return;
	writeRelationAccessor(writer, attrs, "parent-prototype");
	writeRelationAccessor(writer, attrs, "parent-slot");
	writeRelationAccessor(writer, attrs, "child-prototype");
	writeRelationAccessor(writer, attrs, "child-slot");
    }

    private void writeAccessors(PrintWriter writer,
	    String prototype,
	    Map<String,Attributes> local_slots,
	    Map<String,Attributes> override_slots) {
	for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
	    String slot = entry.getKey();
	    Attributes attrs = entry.getValue();
	    String slotType = getSlotType(prototype, slot);
	    String javaType = slotType;
	    if (proto_attrs.containsKey(javaType)) javaType = fixName(javaType, true);
	    writeGetter(writer, prototype, slot, attrs, javaType, true);
	    writeGetter(writer, prototype, slot, attrs, javaType, false);
	    writeGetterAsObject(writer, prototype, slot, attrs, javaType);
	    writeSetter(writer, prototype, slot, attrs, slotType, javaType);
	    writeSetterAsObject(writer, prototype, slot, attrs, slotType, javaType);
	    writeInitializer(writer, prototype, slot, attrs, javaType);
	    writeInitializerAsObject(writer, prototype, slot, attrs, slotType, javaType);
	    writeRemover(writer, prototype, slot, attrs, javaType);
	}
	for (Map.Entry<String,Attributes> entry : override_slots.entrySet()) {
	    String slot = entry.getKey();
	    if (isMember(prototype, slot)) continue;
	    Attributes attrs = entry.getValue();
	    String stype = getSlotType(prototype, slot);
	    String type = stype;
	    if (proto_attrs.containsKey(type)) type = fixName(type, true);
	    writeOverrideGetter(writer, prototype, slot, attrs, type, true);
	    writeOverrideGetter(writer, prototype, slot, attrs, type, false);
	    writeOverrideGetterAsObject(writer, prototype, slot, attrs, type);
	}
    }

    private String getterPrefix(String type) {
	return type.equalsIgnoreCase("boolean") ? "is" : "get";
    }
    
    private void writeGetter(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String type,
	    boolean warn) {
	String prefix = getterPrefix(type);
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	String default_value = attrs.getValue("default-value");
	boolean memberp = isMember(prototype, slot);
	String doc = attrs.getValue("doc");
	if (warn) {
	    writer.print("\n\n");
	    if (doc != null) {
		writer.println("    /**");
		writer.println("      " +doc);
		writer.println("    **/");
	    }
	    writer.print("    public "+type+" " + prefix +accessor_name);
	} else {
	    writer.print("\n\n    "+type+" " +prefix +accessor_name+ NoWarn);
	}
	writer.println("() {");
	if (memberp) {
	    writer.println("        return "  +fixed_name+ ";");
	} else {
	    String result_var = "__result";
	    writer.println("        "+type+" " +result_var+ " = ("
		    +type+ ") getProperty(\"" 
		    +slot+ "\");");
	    writer.println("        if (" +result_var+ " != null) return "
		    +result_var+ ";");
	    if (default_value != null) {
		writer.println("        return " 
			+literalToObject(type, default_value)+ ";");
	    } else {
		writer.println("        return ("+type+
			") getInheritedValue(this, \"" 
			+slot+ "\");");
	    }
	}
	writer.println("    }");
    }

    private void writeGetterAsObject(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String type) {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	String default_value = attrs.getValue("default-value");
	boolean memberp = isMember(prototype, slot);
	writer.print("\n\n    Object " + "get" +accessor_name+ AsObject);
	writer.println("() {");
	if (memberp) {
	    writer.println("        return "
		    +typeToObject(type, fixed_name)+ ";");
	} else {
	    String result_var = "__result";
	    writer.println("        Object " +result_var+ " = getProperty(\"" 
		    +slot+ "\");");
	    writer.println("        if (" +result_var+ " != null) return "
		    +result_var+ ";");
	    if (default_value != null) {
		writer.println("        return " 
			+literalToObject(type, default_value)+ ";");
	    } else {
		writer.println("        return getInheritedValue(this, \"" 
			+slot+ "\");");
	    }
	}
	writer.println("    }");
    }



    private void writeOverrideGetter(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String type,
	    boolean warn) {
	String accessor_name = fixName(slot, true);
	String prefix = getterPrefix(type);
	String default_value = attrs.getValue("default-value");
	if (warn) {
	    writer.print("\n\n    public "+type+" " +prefix +accessor_name);
	} else {
	    writer.print("\n\n    "+type+" " +prefix +accessor_name+	 NoWarn);
	}
	writer.println("() {");
	String result_var = "__result";
	writer.println("        "+type+" " +result_var+ " = super." +prefix 
		+accessor_name+ NoWarn+ "();");
	writer.println("        if (" +result_var+ " != null) return "
		+result_var+ ";");

	writer.println("        return " 
		+literalToObject(type, default_value)+ ";");
	writer.println("    }");
    }


    private void writeOverrideGetterAsObject(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String type) {
	String prefix = "get";
	String accessor_name = fixName(slot, true);
	String default_value = attrs.getValue("default-value");
	writer.print("\n\n    Object " + prefix +accessor_name+ AsObject);
	writer.println("() {");
	String result_var = "__result";
	writer.println("        Object " +result_var+ " = super." +prefix 
		+accessor_name+ AsObject+ "();");
	writer.println("        if (" +result_var+ " != null) return "
		+result_var+ ";");

	writer.println("        return " 
		+literalToObject(type, default_value)+ ";");
	writer.println("    }");
    }


    private void writeSetter(PrintWriter writer,
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String slotType,
	    String javaType) {
	if (isReadOnly(prototype, slot)) return;

	boolean synchronize = relation_prototypes.contains(prototype);
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot);
	boolean notify_listeners_p = notifyListeners(prototype, slot);
	boolean notify_blackboard_p = notifyBlackboard(prototype, slot);
	writer.print("\n\n    public ");
	if (synchronize) writer.print("synchronized ");
	writer.println("void set" +accessor_name+
		"("+javaType+" __new_value) {");
	if (memberp) {
	    writer.println("        "+javaType+" __old_value = " +fixed_name+ ";");
	    writer.println("        this." +fixed_name+ " = __new_value;");
	} else {
	    writer.println("        "+javaType+" __old_value = ("
		    +javaType+ ") getProperty(\"" +slot+ "\");");
	    writer.println("        setProperty(\"" +slot+ "\", __new_value);");
	}
	writer.println("        slotModified(\"" +slot+ "\", "
		+typeToObject(slotType, "__old_value")+ ", "
		+typeToObject(slotType, "__new_value")+ ", "
		+notify_listeners_p+ ", " +notify_blackboard_p
		+ ");");
	writer.println("    }");
    }


    private void writeSetterAsObject(PrintWriter writer,
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String slotType,
	    String javaType) {
	if (isReadOnly(prototype, slot)) return;

	boolean synchronize = relation_prototypes.contains(prototype);
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot);
	boolean notify_listeners_p = notifyListeners(prototype, slot);
	boolean notify_blackboard_p = notifyBlackboard(prototype, slot);
	writer.print("\n\n    public ");
	if (synchronize) writer.print("synchronized ");
	writer.println("void set" +accessor_name+ AsObject+
	"(Object __new_value) {");
	if (memberp) {
	    writer.println("        Object __old_value = get" 
		    +accessor_name+AsObject+ "();");
	    writer.println("        this." +fixed_name+ " = "
		    + objectToType(slotType, javaType, "__new_value")+ ";");
	} else {
	    writer.println("        Object __old_value = getProperty(\"" +slot+ "\");");
	    writer.println("        setProperty(\"" +slot+ "\", "
		    +objectToType(slotType, javaType, "__new_value")+
	    ");");
	}
	writer.println("        slotModified(\"" +slot+ 
		"\", __old_value, __new_value, "
		+notify_listeners_p+ ", " +notify_blackboard_p
		+ ");");
	writer.println("    }");
    }

    private void writeRemover(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String type) {
	if (isReadOnly(prototype, slot)) return;
	boolean memberp = isMember(prototype, slot);
	if (memberp) return;

	String accessor_name = fixName(slot, true);
	boolean notify_listeners_p = notifyListeners(prototype, slot);
	boolean notify_blackboard_p = notifyBlackboard(prototype, slot);
	writer.println("\n\n    protected void remove" +accessor_name+ "() {");
	writer.println("        Object __old_value = getProperty(\"" 
		+slot+ "\");");
	writer.println("        removeProperty(\"" +slot+ "\");");
	writer.println("        slotModified(\"" 
		+slot+ "\", __old_value, get" +accessor_name+ "(), "
		+notify_listeners_p+ ", " +notify_blackboard_p
		+");");
	writer.println("    }");
    }

    private void writeInitializer(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String type) {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot);

	writer.println("\n\n    protected void initialize" +accessor_name+
		"("+type+" new_value) {");
	if (memberp)
	    writer.println("        this." +fixed_name+ " = new_value;");
	else
	    writer.println("        setProperty(\"" +slot+ "\", new_value);");
	writer.println("        slotInitialized(\"" +slot+ "\", "
		+typeToObject(type, "new_value")+ ");");
	writer.println("    }");
    }

    private void writeInitializerAsObject(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs,
	    String slotType,
	    String javaType) {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot);

	writer.println("\n\n    void initialize" +accessor_name+
		AsObject+ "(Object new_value) {");
	if (memberp) {
	    writer.println("        this." +fixed_name+ " = "
		    +objectToType(slotType, javaType, "new_value")+ ";");
	} else {
	    writer.println("        setProperty(\"" +slot+ "\", "
		    +objectToType(slotType, javaType, "new_value")+ 
	    ");");
	}
	writer.println("        slotInitialized(\"" +slot+ "\", new_value);");
	writer.println("    }");
    }
    
    private void writeSlotList(PrintWriter writer,
	    String prototype,
	    String container,
	    Map<String,Attributes> slots,
	    Set<String> container_slots) {
	writer.println("\n\n    protected void collectSlotNames(java.util.Set<String> slots) {");
	writer.println("        super.collectSlotNames(slots);");
	for (String slot : slots.keySet()) {
	    writer.println("        slots.add(\"" +slot+ "\");");
	}
	for (String slot : container_slots) {
	    writer.println("        slots.add(\"" +slot+ "\");");
	}
	writer.println("    }");
	
    }

    private String slotDescriptionMethod(String slot) {
	String accessor = fixName(slot, true);
	return "slotMetaData__" +accessor;
    }

    private void writeDescriptionGetters(PrintWriter writer,
	    String prototype,
	    String container,
	    Map<String,Attributes> slots,
	    Set<String> container_slots,
	    Map<String,String> units_override_slots) {
	for (Map.Entry<String,Attributes> entry : slots.entrySet()) {
	    String slot = entry.getKey();
	    Attributes attrs = entry.getValue();
	    writeDescriptionGetter(writer, prototype, slot, attrs);
	}

	for (Map.Entry<String,String> entry : units_override_slots.entrySet()) {
	    String slot = entry.getKey();
	    String units = entry.getValue();
	    writeUnitsOverrideDescriptionGetter(writer, prototype, slot, units);
	}


	for (String slot : container_slots) {
	    writeContainerDescriptionGetter(writer, prototype, slot);
	}

	if (slots.isEmpty() && container_slots.isEmpty()) return;

	writer.println("\n\n    protected void collectSlotDescriptions(java.util.Map<String,SlotDescription> map) {");
	writer.println("        super.collectSlotDescriptions(map);");
	for (String slot : slots.keySet()) {
	    writer.println("        map.put(\""  +slot+ "\", "
		    +slotDescriptionMethod(slot)+
	    "());");
	}
	for (String slot : container_slots) {
	    writer.println("        map.put(\"" +slot+ "\", "
		    +slotDescriptionMethod(slot)+
	    "());");
	}
	writer.println("    }");
    }

    private void writeDescriptionGetter(PrintWriter writer, 
	    String prototype,
	    String slot,
	    Attributes attrs) {
	String fixed_name = fixName(slot, false);
	String default_value = attrs.getValue("default-value");
	String path = attrs.getValue("path");
	String doc = attrs.getValue("doc");
	String units = attrs.getValue("units");
	boolean memberp = isMember(prototype, slot);
	boolean immutablep = isReadOnly(prototype, slot);
	String type = getSlotType(prototype, slot);
	writer.print("\n\n");
	writer.println("    public SlotDescription " 
		+slotDescriptionMethod(slot)+
	"() {");
	writer.println("        SlotDescription __desc = new SlotDescription();");
	writer.println("        __desc.name = \"" +slot+ "\";");
	writer.println("        __desc.prototype = \"" +prototype+ "\";");
	if (doc != null) writer.println("        __desc.doc = \"" +doc+ "\";");
	if (units != null)
	    writer.println("        __desc.units = \"" +units+ "\";");
	writer.println("        __desc.is_writable = " 
		+!immutablep+ ";");
	writer.println("        Object __value;");
	if (memberp) {
	    writer.println("        __value = " 
		    +typeToObject(type,fixed_name)+ 
	    ";");
	} else {
	    writer.println("        __value = getProperty(\""  +slot+ "\");");
	}
	writer.println("        if (__value != null) {");
	writer.println("            __desc.is_overridden = true;");
	writer.println("            __desc.value = __value;");
	writer.println("        } else {");
	writer.println("            __desc.is_overridden = false;");
	if (path == null) {
	    if (default_value != null) {
		writer.println("            __desc.value = " 
			+literalToObject(type, default_value)+ ";");
	    }
	} else {
	    writer.println("            __desc.value = getInheritedValue(this, \"" 
		    +slot+ "\");");
	}
	writer.println("        }");
	writer.println("        return __desc;");
	writer.println("    }");
    }

    private void writeUnitsOverrideDescriptionGetter(PrintWriter writer, 
	    String prototype,
	    String slot,
	    String units) {
	String method = slotDescriptionMethod(slot);
	writer.print("\n\n");
	writer.println("    public SlotDescription " +method+ "() {");
	writer.println("        SlotDescription __desc = super."+method+"();");
	writer.println("        __desc.units = \"" +units+ "\";");
	writer.println("        return __desc;");
	writer.println("    }");
    }



    private void writeContainerDescriptionGetter(PrintWriter writer, 
	    String prototype,
	    String slot) {
	String accessor_name = fixName(slot, true);
	String owner = null;
	while (owner == null) {
	    Attributes attrs = proto_attrs.get(prototype);
	    prototype = attrs.getValue("container");
	    if (prototype == null) break;
	    owner = ancestorForSlot(prototype, slot);
	}
	writer.println("\n\n    public SlotDescription " 
		+slotDescriptionMethod(slot)+ "() {");
	writer.println("        SlotDescription __desc = new SlotDescription();");
	writer.println("        __desc.name = \"" +slot+ "\";");
	writer.print("        __desc.prototype = ");
	if (owner == null) {
	    writer.println("null;");
	} else
	    writer.println("\"" +owner+ "\";");
	writer.println("        __desc.value = get" +accessor_name+ AsObject+
	"();");
	writer.println("        __desc.is_overridden = false;");
	writer.println("        __desc.is_writable = false;");
	writer.println("        return __desc;");
	writer.println("    }");
    }



    private void writeDynamicAccessors(PrintWriter writer, 
	    String prototype,
	    String parent,
	    Map<String,Attributes> local_slots,
	    String container) {
	boolean is_root = parent == null;
	Map<String,Attributes> all_slots = null;
	if (container != null) {
	    all_slots = collectContainerSlots(container);
	    for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
		String key = entry.getKey();
		Attributes value = entry.getValue();
		all_slots.put(key, value);
	    }
	    if (!all_slots.isEmpty()) {
		writeDynamicGetter(writer, prototype, all_slots, is_root);
	    }
	}
	if (!local_slots.isEmpty()) {
	    if (container == null) {
		writeDynamicGetter(writer, prototype, local_slots, is_root);
	    }
	    writeDynamicSetter(writer, prototype, local_slots, is_root);
	    writeDynamicInitializer(writer, prototype, local_slots, is_root);
	}

    }

    private void writeDynamicGetter(PrintWriter writer, 
	    String prototype,
	    Map<String,Attributes> slots,
	    boolean is_root) {
	writer.println("\n\n    protected Object getLocalValue(String __slot) {");
	writer.println("       String __key = __slot.intern();");
	writer.print("      ");
	for(Map.Entry<String,Attributes> entry : slots.entrySet()) {
	    String slot = entry.getKey();
	    String method = "get" + fixName(slot, true) + AsObject;
	    writer.println(" if (\"" +slot+ "\" == __key)");
	    writer.println("            return " +method+ "();");
	    writer.print("       else");
	}
	writer.println("");
	writer.print("           return ");
	if (is_root)
	    writer.println("null;");
	else
	    writer.println("super.getLocalValue(__slot);");
	writer.println("    }");
    }

    private void writeDynamicSetter(PrintWriter writer, 
	    String prototype,
	    Map<String,Attributes> slots,
	    boolean is_root) {
	writer.println("\n\n    protected void setLocalValue(String __slot,");
	writer.println("                                 Object __value) {");
	boolean first = true;
	for(Map.Entry<String,Attributes> entry : slots.entrySet()) {
	    String slot = entry.getKey();
	    if (isReadOnly(prototype, slot)) continue;
	    String method = "set" + fixName(slot, true) + AsObject;
	    writer.print("      ");
	    if (first) {
		writer.println(" String __key = __slot.intern();");
		writer.print("      ");
	    } else {
		writer.print(" else");
	    }
	    writer.println(" if (\"" +slot+ "\" == __key)");
	    writer.println("            " +method+ "(__value);");
	    first = false;
	}
	if (!is_root) {
	    if (first) {
		// no settable slots
		writer.println("       super.setLocalValue(__slot, __value);");
	    } else {
		writer.println("       else");
		writer.println("            super.setLocalValue(__slot, __value);");
	    }
	}
	writer.println("    }");
    }

    private void writeDynamicInitializer(PrintWriter writer, 
	    String prototype,
	    Map<String,Attributes> slots,
	    boolean is_root) {
	writer.println("\n\n    protected void initializeLocalValue(String __slot,");
	writer.println("                                 Object __value) {");
	writer.println("       String __key = __slot.intern();");
	boolean first = true;
	for(Map.Entry<String,Attributes> entry : slots.entrySet()) {
	    String slot = entry.getKey();
	    String method = "initialize" + fixName(slot, true) +AsObject;
	    writer.print("      ");
	    if (!first) writer.print(" else");
	    writer.println(" if (\"" +slot+ "\" == __key)");
	    writer.println("            " +method+ "(__value);");
	    first = false;
	}
	if (!is_root) {
	    writer.println("       else");
	    writer.println("            super.initializeLocalValue(__slot, __value);");
	}
	writer.println("    }");
    }


    private void writeContainerReaders(PrintWriter writer, 
	    String container,
	    Set<String> container_slots) {
	if (container == null) return;
	for (String slot : container_slots) {
	    writeContainerReader(writer, container, slot);
	    writeContainerReaderAsObject(writer, container, slot);
	}
    }
    
    private String getDefaultValue(String container, String slot) {
	String type = getSlotType(container, slot);
	String defaultValue = getSlotValue(container, slot);
	if (type.equals("java.lang.String") || type.equals("String")) {
	    defaultValue = "\"" + defaultValue+ "\""; 
	}
	return defaultValue;
    }

    private void writeContainerReader(PrintWriter writer, 
	    String container,
	    String slot) {
	String reader_name = "get"+fixName(slot, true);
	String container_class = fixName(container, true);
	String type = getSlotType(container, slot);
	String raw_container_var = "__raw_container";
	String container_var = "__container";
	writer.println("\n\n    public "+type+ " " +reader_name+ "() {");
	writer.println("       Object " +raw_container_var+ " = containerFrame();");
	writer.print("       if ( " + raw_container_var + 
	" == null)");
	String defaultValue = getDefaultValue(container, slot);
	writer.println(" return " +defaultValue+ ";");
	writer.println("       if (!("  +raw_container_var+ " instanceof "
		+container_class+ "))");
	writer.println("            throw new RuntimeException(\"Bogus container!\");");
	writer.println("       " +container_class+  " " +container_var+ " = ("
		+container_class+ ") " +raw_container_var+ ";");
	writer.println("       return " +container_var+ "." +reader_name+ "();");
	writer.println("    }");
    }

    private void writeContainerReaderAsObject(PrintWriter writer, 
	    String container,
	    String slot) {
	String reader_name = "get"+fixName(slot, true)+AsObject;
	String container_class = fixName(container, true);
	String raw_container_var = "__raw_container";
	String container_var = "__container";
	writer.println("\n\n    Object " +reader_name+ "() {");
	writer.println("       Object " +raw_container_var+ " = containerFrame();");
	String defaultValue = getDefaultValue(container, slot);
	writer.println("       if ( " + raw_container_var + " == null) return " +defaultValue+ ";");
	writer.println("       if (!("  +raw_container_var+ " instanceof "
		+container_class+ ")) {");
	writer.println("            getLogger().warn(\"Container of \" +this+ \" is not a " +container_class+ ": \" + " +raw_container_var+ ");");
	writer.println("            return null;");
	writer.println("       }");
	writer.println("       " +container_class+  " " +container_var+ " = ("
		+container_class+ ") " +raw_container_var+ ";");
	writer.println("       return " +container_var+ "." +reader_name+ "();");
	writer.println("    }");
    }


    private void writeUpdaters(PrintWriter writer, 
	    String prototype,
	    Set<String> container_slots,
	    String container) {
	if (container == null) return;

	String old_arg = "__old_frame";
	String new_arg = "__new_frame";
	String old_var = "__old";
	String new_var = "__new";
	String classname = fixName(container, true);

	writer.println("\n\n    protected void fireContainerChanges(" +
	"DataFrame __raw_old, DataFrame __raw_new) {");
	writer.println("        if (!(__raw_old instanceof " +classname+ ")) {");
	writer.println("            getLogger().warn(\"Container of \" +this+ \" is not a " +classname+ ": \" + __raw_old);");
	writer.println("            return;");
	writer.println("        }");
	writer.println("        if (!(__raw_new instanceof " +classname+ ")) {");
	writer.println("            getLogger().warn(\"Container of \" +this+ \" is not a " +classname+ ": \" + __raw_new);");
	writer.println("            return;");
	writer.println("        }");
	writer.println("        " +classname+ " " +old_arg+ " = ("
		+classname+ ") __raw_old;");
	writer.println("        " +classname+ " " +new_arg+ " = ("
		+classname+ ") __raw_new;");
	writer.println("        Object " +old_var+ ";");
	writer.println("        Object " +new_var+ ";");
	for (String slot : container_slots) {
	    String fixed_name = fixName(slot, true);
	    String bean_name = fixName(slot, true, true);
	    String reader = "get" +fixed_name+ AsObject+ "();";
	    writer.println("        " +old_var+ " = " +old_arg+ "." + reader);
	    writer.println("        " +new_var+ " = " +new_arg+ "." + reader);
	    writer.println("        if (" +new_var+ " != null) {");
	    writer.println("            if (" +old_var+ " == null || !"
		    +old_var+ ".equals(" +new_var+ ")) {");
	    writer.println("                fireChange(\"" +bean_name+ "\", "
		    +old_var+ ", " +new_var+ ");");
	    writer.println("            }");
	    writer.println("        }");
	}
	writer.println("    }");

	writer.println("\n\n    protected void fireContainerChanges(DataFrame __raw) {");
	writer.println("        if (!(__raw instanceof " +classname+ ")) {");
	writer.println("            getLogger().warn(\"Container of \" +this+ \" is not a " +classname+ ": \" + __raw);");
	writer.println("            return;");
	writer.println("        }");
	writer.println("        " +classname+ " " +new_arg+ " = ("
		+classname+ ") __raw;");
	writer.println("        Object " +new_var+ ";");
	for (String slot : container_slots) {

	    String fixed_name = fixName(slot, true);
	    String bean_name = fixName(slot, true, true);
	    String reader = "get" +fixed_name+ AsObject+ "();";
	    writer.println("        " +new_var+ " = " +new_arg+ "." + reader);
	    writer.println("        if (" +new_var+ " != null) {");
	    writer.println("            fireChange(\"" +bean_name+ "\", "
		    +"null"+ ", " +new_var+ ");");
	    writer.println("        }");
	}
	writer.println("    }");

    }
    
    
    
    private void writePostInitializer(PrintWriter writer,
	    String prototype,
	    Map<String,Attributes> local_slots) {
	boolean has_metrics = false;
	for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
	    Attributes attrs = entry.getValue();
	    String path = attrs.getValue(MetricPath);
	    if (path != null) {
		has_metrics = true;
		break;
	    }
	}
	if (!has_metrics) return;

	writer.println("\n\n    protected void postInitialize() {");
	writer.println("        super.postInitialize();");
	String obs = "__observer";
	writer.println("        java.util.Observer " +obs+ ";");
	String old_value = "__old";
	String new_value = "__new";
	for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
	    String slot = entry.getKey();
	    Attributes attrs = entry.getValue();
	    String sname = fixName(slot, false);
	    boolean notify_listeners_p = notifyListeners(prototype, slot);
	    boolean notify_blackboard_p = notifyBlackboard(prototype, slot);
	    String path = attrs.getValue(MetricPath);
	    if (path != null) {
		writer.println("        " +obs+" = new java.util.Observer() {");
		writer.println("            public void update(java.util.Observable __xxx, Object "+new_value+ ") {");
		writer.println("                Object "+old_value+" = " +sname+ ";");
		writer.println("                " +sname+ " = (Metric) "
			+new_value+ ";");

		writer.println("                slotModified(\"" +slot+ "\", "
			+old_value+ ", "+new_value+", "
			+notify_listeners_p+ ", " +notify_blackboard_p
			+ ");");
		writer.println("            }");
		writer.println("        };");
		writer.println("        " +sname+ " = " +
			"getFrameSet().getMetricValue(this, \""
			+path+ "\");");
		writer.println("        getFrameSet().subscribeToMetric(this, "
			+obs+ ", \""  +path+ "\");");
	    }
	}
	writer.println("    }");
    }


    // Utilities

    private boolean inheritsSlot(String proto, String slot) {
	Attributes attrs = proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	if (parent == null) return false;
	Map<String,Attributes> pslots =  proto_slots.get(parent);
	if (pslots != null && pslots.containsKey(slot)) return true;
	return inheritsSlot(parent, slot);
    }

    private Attributes slotDefinition(String proto, String slot) {
	Attributes definition = null;
	while (proto != null) {
	    Map<String,Attributes> slots = proto_slots.get(proto);
	    Attributes def = slots.get(slot);
	    if (def != null) definition = def;
	    Attributes attrs = proto_attrs.get(proto);
	    proto = attrs.getValue("prototype");
	}
	return definition;
    }

    private String ancestorForSlot(String proto, String slot) {
	if (proto == null) return null;

	Map<String,Attributes> pslots = proto_slots.get(proto);
	if (pslots != null && pslots.containsKey(slot)) return proto;

	Attributes attrs = proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	return ancestorForSlot(parent, slot);
    }

    private Map<String,Attributes> collectContainerSlots(String proto) {
	Map<String,Attributes> slots = new HashMap<String,Attributes>();
	collectContainerSlots(proto, slots, true);
	return slots;
    }

    private void collectContainerSlots(String proto, Map<String,Attributes> slots, boolean filter) {
	Map<String,Attributes> local_slots = proto_slots.get(proto);
	Attributes attrs = proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	String container = attrs.getValue("container");
	if (local_slots != null) {
	    // if filter is true, don't include uninheritable slots
	    if (filter) {
		for (Map.Entry<String,Attributes> entry : local_slots.entrySet()) {
		    Attributes value = entry.getValue();
		    String prot = value.getValue("inheritable-through");
		    if (prot == null || !prot.equalsIgnoreCase("prototype-only"))
			slots.put(entry.getKey(), value);
		}
	    } else {
		slots.putAll(local_slots);
	    }
	}
	if (parent != null) collectContainerSlots(parent, slots, false);
	if (container != null) collectContainerSlots(container, slots, true);
    }

    static String fixName(String name, boolean is_class) {
	return fixName(name, is_class, false);
    }

    static String fixName(String name, boolean is_class, boolean is_bean) {
	if (is_class) {
	    // camelize
	    StringBuffer buf = new StringBuffer();
	    char[] name_chars = name.toCharArray();
	    boolean capitalize_next = !is_bean;
	    for (int i=0; i<name_chars.length; i++) {
		char next = name_chars[i];
		if (next == '-') {
		    capitalize_next = true;
		    continue;
		}
		if (capitalize_next) {
		    buf.append(Character.toUpperCase(next));
		} else {
		    buf.append(next);
		}
		capitalize_next = false;
	    }
	    return buf.toString();
	} else {
	    return name.replaceAll("-", "_");
	}
    }


    // The boolean slot attributes (member, warn, immutable,
    // transient, notify-blackboard, notify-listeners) MUST be
    // specified in the definition. 
    private boolean getBooleanAttribute(String prototype,
	    String slot,
	    String attr,
	    boolean default_value) {
	Attributes attrs = slotDefinition(prototype, slot);
	if (attrs == null) return default_value;
	String attrstr = attrs.getValue(attr);
	if (attrstr == null) return default_value;
	return attrstr.equalsIgnoreCase("true");

//	HashMap slots = (HashMap) proto_slots.get(prototype);
//	Attributes attrs = (Attributes) slots.get(slot);

//	String attrstr = attrs != null ? attrs.getValue(attr) : null;
//	if (attrstr == null) {
//	Attributes p_attrs = (Attributes) proto_attrs.get(prototype);
//	String parent = p_attrs.getValue("prototype");
//	if (parent != null) {
//	return getBooleanAttribute(parent, slot, attr, default_value);
//	} else {
//	return default_value;
//	}
//	} else {
//	return attrstr.equalsIgnoreCase("true");
//	}
    }

    private String getPathType(String path, String slot) {
	List<Attributes> forks = path_forks.get(path);
	if (forks == null || forks.isEmpty()) return null;
	String override_slot = path_slots.get(path);
	if (override_slot != null) slot = override_slot;

	Attributes fork_attrs = forks.get(forks.size()-1);
	if (fork_attrs == null) return null;
	String relation = fork_attrs.getValue("relation");
	String role = fork_attrs.getValue("role");
	if (relation == null || role == null) return null;
	Attributes p_attrs = proto_attrs.get(relation);
	if (p_attrs == null) return null;
	String attribute = role+"-prototype";

	String prototype = p_attrs.getValue(attribute); 
	while (prototype == null) {
	    relation = p_attrs.getValue("prototype");
	    if (relation == null) return null;
	    p_attrs = proto_attrs.get(relation);
	    if (p_attrs == null) return null;
	    prototype = p_attrs.getValue(attribute); 
	}
//	System.out.println("### Looking for type of " +slot+ " in " +prototype);
	String type = getSlotType(prototype, slot);
//	System.out.println("### Got " + type);
	return coerceToObjectType(type);
    }

    private String getPath(String prototype, String slot) {
	Map<String,Attributes> slots = proto_slots.get(prototype);
	Attributes attrs = slots.get(slot);
	String path = attrs != null ? attrs.getValue("path") : null;
	if (path != null) return path;
	Attributes p_attrs = proto_attrs.get(prototype);
	String parent = p_attrs.getValue("prototype");
	if (parent != null) {
	    return getPath(parent, slot);
	} else {
	    return null;
	}
    }

    private String getSlotTypeFromPrototypeTree(String prototype, String slot) {
	Map<String,Attributes> slots = proto_slots.get(prototype);
	Attributes attrs = slots.get(slot);
	String metric = attrs != null ? attrs.getValue(MetricPath) : null;
	if (metric != null) return Metric_Type;
	String attrstr = attrs != null ? attrs.getValue("type") : null;
	if (attrstr == null) {
	    String path = attrs != null ? attrs.getValue("path") : null;
	    if (path != null) {
		String ptype = getPathType(path, slot);
		if (ptype != null) return ptype;
	    }
	    Attributes p_attrs = proto_attrs.get(prototype);
	    String parent = p_attrs.getValue("prototype");
	    if (parent != null) {
		return getSlotTypeFromPrototypeTree(parent, slot);
	    } else {
		return null;
	    }
	} else {
	    return attrstr;
	}
    }

    private String getSlotType(String prototype, String slot) {
	String type = getSlotTypeFromPrototypeTree(prototype, slot);
	if (type != null) return type;

	// Try the containment hierarchy
	Attributes p_attrs = proto_attrs.get(prototype);
	String container = p_attrs.getValue("container");
	if (container != null) {
	    return getSlotType(container, slot);
	} else {
	    return "String";
	}
    }
    
    
    private String getSlotValueFromPrototypeTree(String prototype, String slot) {
	Map<String,Attributes> slots = proto_slots.get(prototype);
	Attributes attrs = slots.get(slot);
	String attrstr = attrs != null ? attrs.getValue("default-value") : null;
	if (attrstr == null) {
	    Attributes p_attrs = proto_attrs.get(prototype);
	    String parent = p_attrs.getValue("prototype");
	    if (parent != null) {
		return getSlotValueFromPrototypeTree(parent, slot);
	    } else {
		return null;
	    }
	} else {
	    return attrstr;
	}
    }

    private String getSlotValue(String prototype, String slot) {
	String type = getSlotValueFromPrototypeTree(prototype, slot);
	if (type != null) return type;

	// Try the containment hierarchy
	Attributes p_attrs = proto_attrs.get(prototype);
	String container = p_attrs.getValue("container");
	if (container != null) {
	    return getSlotValue(container, slot);
	} else {
	    return null;
	}
    }

    private boolean isMetric(String prototype, String slot) {
	return getSlotType(prototype, slot).equals(Metric_Type);
    }

    private boolean isTransient(String prototype, String slot) {
	// Metric slots are always members
	if (isMetric(prototype, slot)) 
	    return true;
	else
	    return getBooleanAttribute(prototype, slot, "transient", false);
    }

    private boolean isMember(String prototype, String slot) {
	// Path-valued slots can't be members
	String path = getPath(prototype, slot);
	if (path != null) return false;

	// Non-Object types must be members
	String type = getSlotType(prototype, slot);
	if (!isObjectType(type)) return true;

	// Transient slots must be members
	if (isTransient(prototype, slot)) return true;

	// Otherwise return the value of member attribute, 
	// defaulting to true.
	return getBooleanAttribute(prototype, slot,  "member", true);
    }

    private boolean isReadOnly(String prototype, String slot) {
	return isMetric(prototype, slot) ||
	getBooleanAttribute(prototype, slot, "immutable", false);
    }

    private boolean notifyListeners(String prototype, String slot) {
	return getBooleanAttribute(prototype, slot, "notify-listeners", true);
    }

    private boolean notifyBlackboard(String prototype, String slot) {
	return getBooleanAttribute(prototype, slot, "notify-blackboard", true);
    }

    private String coerceToObjectType(String type) {
	if (isObjectType(type))
	    return type;
	else if (type.equals("int"))
	    return "Integer";
	else if (type.equals("long"))
	    return "Long";
	else if (type.equals("float"))
	    return "Float";
	else if (type.equals("double"))
	    return "Double";
	else
	    return null;
    }

    private boolean isObjectType(String type) {
	return 
	!type.equals("double") &&
	!type.equals("float") &&
	!type.equals("long") &&
	!type.equals("int") &&
	!type.equals("boolean");
    }
    
    private boolean isCoercibleType(String type) {
	return 
	type.equalsIgnoreCase("Double") ||
	type.equalsIgnoreCase("Float") ||
	type.equalsIgnoreCase("Long") ||
	type.equals("Integer") ||
	type.equals("int") ||
	type.equalsIgnoreCase("Boolean") ||
	type.equals("String") ||
	type.equals(Metric_Type);
    }

    private String objectToType(String slotType, String javaType, String var) {
	if (isCoercibleType(slotType)) {
	    return "force_"+slotType+"(" +var+ ")";
	} else {
	    return "(" + javaType+ ") " + var;
	}
    }


    private String literalToObject(String type, String var) {
	if (type.equals("String"))
	    return "\"" +var+ "\"";
	else if (type.equalsIgnoreCase("double"))
	    return "new Double(" +var+ ")";
	else if (type.equalsIgnoreCase("float"))
	    return "new Float(" +var+ ")";
	else if (type.equalsIgnoreCase("long"))
	    return "new Long(" +var+ ")";
	else if (type.equals("int") || type.equals("Integer"))
	    return "new Integer(" +var+ ")";
	else if (type.equalsIgnoreCase("boolean"))
	    return "new Boolean(" +var+ ")";
	else
	    return var;
    }

    private String typeToObject(String type, String var) {
	if (isObjectType(type)) {
	    return var;
	} else {
	    return literalToObject(type, var);
	}
    }
    
    private String simpleValue(String type, String value) {
	if (isObjectType(type))
	    return literalToObject(type, value);
	else 
	    return value;
    }


    // Driver

    public static void main(String[] args) {
	String java_path = null;
	String clp_path = null;
	String dtd_path = null;
	String structs = null;
	int i = 0;
	while (i < args.length) {
	    String arg = args[i];
	    if (arg.equals("-java")) {
		java_path = args[++i];
		System.out.println("Java root path is " + java_path);
	    } else if (arg.equals("-clp")) {
		clp_path = args[++i];
		System.out.println("Jess root path is " + clp_path);
	    } else if (arg.equals("-dtd")) {
		dtd_path = args[++i];
		System.out.println("DTD root path is " + dtd_path);
	    } else if (arg.equals("-structs")) {
		structs = args[++i];
	    } else if (arg.endsWith(".xml")) {
		System.out.println("XML file is " + arg);
		File file = new File(arg);
		FrameGen generator = new FrameGen(java_path, clp_path, dtd_path, structs);
		generator.parseProtoFile(file);
	    }
	    ++i;
	}
    }
}

