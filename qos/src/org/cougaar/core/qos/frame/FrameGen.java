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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    static {
	String driver = System.getProperty(DRIVER_PROPERTY);
	if (driver == null) 
	    System.setProperty(DRIVER_PROPERTY, DRIVER_DEFAULT);
    }

    private String package_name;
    private String dir_name;
    private String path;
    private HashMap slots;
    private HashMap proto_attrs;
    private HashMap proto_slots;
    private String current_prototype;
    private String container_relation, root_relation;


    public FrameGen(String path)
    {
	this.path = path;
    }

    public void parseProtoFile(File xml_file)
    {
	proto_slots = new HashMap();
	proto_attrs = new HashMap();
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

	generateCode();

    }


    // SAX
    public void startElement(String uri, String local, String name, 
			     Attributes attrs)
    {
	if (name.equals("frameset")) {
	    startFrameset(attrs);
	} else if (name.equals("prototypes")) {
	    // no-op
	} else if (name.equals("prototype")) {
	    startPrototype(attrs);
	} else if (name.equals("slot")) {
	    slot(attrs);
	} 
    }

    public void endElement(String uri, String local, String name)
    {
	if (name.equals("frameset")) {
	    endFrameset();
	} else if (name.equals("prototypes")) {
	    // no-op
	} else if (name.equals("prototype")) {
	    endPrototype();
	} 
    }

    // Not using this yet
    public void characters(char buf[], int offset, int length)
    {
    }






    // Utilities
    private boolean descendsFrom(String child_proto, String parent_proto)
    {
	if (child_proto == null || parent_proto == null) {
	    return false;
	} else if (child_proto.equals(parent_proto)) {
	    return true;
	} else {
	    Attributes attrs = (Attributes) proto_attrs.get(child_proto);
	    return descendsFrom(attrs.getValue("prototype"), parent_proto);
	}
    }


    private boolean inheritsSlot(String proto, String slot)
    {
	Attributes attrs = (Attributes) proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	if (parent == null) return false;
	HashMap pslots = (HashMap) proto_slots.get(parent);
	if (pslots != null && pslots.containsKey(slot)) return true;
	return inheritsSlot(parent, slot);
    }

    private String ancestorForSlot(String proto, String slot)
    {
	Attributes attrs = (Attributes) proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	if (parent == null) {
	    return null;
	}
	HashMap pslots = (HashMap) proto_slots.get(parent);
	if (pslots != null && pslots.containsKey(slot)) {
	    return parent;
	}
	return ancestorForSlot(parent, slot);
    }

    private HashSet collectSlots(String proto)
    {
	HashSet slots = new HashSet();
	collectSlots(proto, slots);
	return slots;
    }

    private void collectSlots(String proto, HashSet slots)
    {
	HashMap local_slots = (HashMap) proto_slots.get(proto);
	Attributes attrs = (Attributes) proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	String container = attrs.getValue("container");
	if (local_slots != null) slots.addAll(local_slots.keySet());
	if (parent != null) collectSlots(parent, slots);
	if (container != null) collectSlots(container, slots);
    }

    static String fixName(String name, boolean is_class)
    {
	return fixName(name, is_class, false);
    }

    static String fixName(String name, boolean is_class, boolean is_bean)
    {
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


    private boolean getBooleanAttribute(String prototype,
					String slot,
					Attributes attrs,
					String attr,
					boolean default_value)
    {
	String attrstr = attrs.getValue(attr);
	if (attrstr == null) {
	    String ancestor = ancestorForSlot(prototype, slot);
	    if (ancestor != null) {
		HashMap slots = (HashMap) proto_slots.get(ancestor);
		Attributes inherited = (Attributes) slots.get(slot);
		return getBooleanAttribute(ancestor, slot, inherited, attr, 
					   default_value);
	    } else {
		return default_value;
	    }
	} else {
	    return attrstr.equalsIgnoreCase("true");
	}
    }

    private boolean isStatic(String prototype, String slot, Attributes attrs)
    {
	return getBooleanAttribute(prototype, slot, attrs, "static", true);
    }


    private boolean isMember(String prototype, String slot, Attributes attrs)
    {
	return getBooleanAttribute(prototype, slot, attrs, "member", true);
    }

    // Parsing 

    private void startFrameset(Attributes attrs)
    {
	package_name = attrs.getValue("package");

	container_relation = attrs.getValue("container-relation");
	String inheritance = attrs.getValue("frame-inheritance");
	if (!inheritance.equals("single")) {
	    throw new RuntimeException("Only single-inheritance FrameSets are supported!");
	}

	dir_name = path + File.separator+ 
	    package_name.replaceAll("\\.", File.separator);
    }

    private void startPrototype(Attributes attrs)
    {
	current_prototype = attrs.getValue("name");
	proto_attrs.put(current_prototype, new AttributesImpl(attrs));
	slots = new HashMap();
    }

    private void endPrototype()
    {
	proto_slots.put(current_prototype, slots);
	current_prototype = null;
	slots = null;
    }

    private void slot(Attributes attrs)
    {
	if (slots != null) {
	    String slot = attrs.getValue("name");
	    slots.put(slot, new AttributesImpl(attrs));
	}
    }

    private void endFrameset()
    {
    }



    // Code Generation 

    private void generateCode()
    {
	Iterator itr;

	// Find root_relation
	String relation = container_relation;
	while (relation != null) {
	    Attributes attrs = (Attributes) proto_attrs.get(relation);
	    String proto = attrs.getValue("prototype");
	    if (proto == null) root_relation = relation;
	    relation = proto;
	}
	

	itr = proto_slots.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String prototype = (String) entry.getKey();
	    Attributes attrs = (Attributes) proto_attrs.get(prototype);
	    String parent = attrs.getValue("prototype");
	    String container = attrs.getValue("container");
	    HashMap slots = (HashMap) entry.getValue();
	    HashMap override_slots = new HashMap();
	    HashMap local_slots = new HashMap();

	    Iterator itr2 = slots.entrySet().iterator();
	    while (itr2.hasNext()) {
		Map.Entry entry2 = (Map.Entry) itr2.next();
		String slot = (String) entry2.getKey();
		Attributes slot_attrs = (Attributes) entry2.getValue();
		if (inheritsSlot(prototype, slot)) {
		    override_slots.put(slot, slot_attrs);
		} else {
		    local_slots.put(slot, slot_attrs);
		}
	    }


	    generateCode(prototype, parent, container, 
			 local_slots, override_slots);
	}
    }

    private void generateCode(String prototype, 
			      String parent, 
			      String container,
			      HashMap local_slots,
			      HashMap override_slots)
    {
	String name = fixName(prototype, true);
	File out = new File(dir_name, name+".java");
	PrintWriter writer = null;
	try {
	    FileWriter fw = new FileWriter(out);
	    writer = new PrintWriter(fw);
	} catch (java.io.IOException iox) {
	    iox.printStackTrace();
	    System.exit(-1);
	}

	writeDecl(writer, prototype, parent);
	writer.println("{");
	writeSlots(writer, prototype, local_slots);
	writeConstructors(writer, prototype);
	writeCollector(writer, prototype, local_slots);
	writeAccessors(writer, prototype, local_slots, override_slots);
	writeContainerReaders(writer, prototype, local_slots, container);
	writeUpdaters(writer, prototype, local_slots, container);
	writeDynamicAccessors(writer, prototype, parent,local_slots, container);
	writer.println("}");

	writer.close();
	System.out.println("Wrote " +out);
    }
    
    private void writeDecl(PrintWriter writer,
			   String prototype, 
			   String parent)
    {
	boolean is_root_relation = 
	    parent == null &&
	    root_relation != null && 
	    prototype.equalsIgnoreCase(root_relation);
	String name = fixName(prototype, true);
	writer.println("package " +package_name+ ";\n");
	writer.println("import org.cougaar.core.util.UID;");
	writer.println("import org.cougaar.core.qos.frame.FrameSet;");
	writer.println("import org.cougaar.core.qos.frame.DataFrame;");
	if (is_root_relation) {
	    writer.println("import org.cougaar.core.qos.frame.RelationFrame;");
	}
	writer.println("\npublic class " +name);
	if (is_root_relation) {
	    writer.println("    extends RelationFrame");
	} else if (parent != null) {
	    writer.println("    extends " +fixName(parent, true));
	} else {
	    writer.println("    extends DataFrame");
	}
    }


    private void writeSlots(PrintWriter writer,
			    String prototype, 
			    HashMap local_slots)
    {
	Iterator itr = local_slots.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String slot = (String) entry.getKey();
	    Attributes attrs = (Attributes) entry.getValue();
	    writeSlot(writer, prototype, slot, attrs);
	}
    }

    private void writeSlot(PrintWriter writer, 
			   String prototype,
			   String  slot, 
			   Attributes attrs)
    {
	String value = attrs.getValue("value");
	boolean memberp = isMember(prototype, slot, attrs);
	boolean staticp = isStatic(prototype, slot, attrs);
	if (memberp) {
	    String fixed_name = fixName(slot, false);
	    writer.println("    protected Object " +fixed_name+ ";");
	}
    }

    private void writeConstructors(PrintWriter writer,
				   String name)
    {
	// Define values for inherited slots!
	String cname = fixName(name, true);

	writer.println("\n\n    public " +cname + "(UID uid)");
	writer.println("    {");
	writer.println("        super(null, \"" +name+ "\", uid);");
	writer.println("    }");

	writer.println("\n\n    public " +cname + "(FrameSet frameSet,");
	int spaces = 12 + cname.length();
	for (int i=0; i<spaces; i++) writer.print(' ');
	writer.println("UID uid)");
	writer.println("    {");
	writer.println("        super(frameSet, \"" +name+ "\", uid);");
	writer.println("    }");

	writer.println("\n\n    public " +cname + "(FrameSet frameSet,");
	for (int i=0; i<spaces; i++) writer.print(' ');
	writer.println("String kind,");
	for (int i=0; i<spaces; i++) writer.print(' ');
	writer.println("UID uid)");
	writer.println("    {");
	writer.println("        super(frameSet, kind, uid);");
	writer.println("    }");
    }

    private void writeCollector(PrintWriter writer,
				String prototype, 
				HashMap local_slots)
    {
	Iterator itr = local_slots.entrySet().iterator();
	if (!itr.hasNext()) return; // no local slots

	String props = "__props";
	String val = "__value";
	writer.println("\n\n    protected void collectSlotValues(java.util.Properties "
		       +props+ ")");
	writer.println("    {");
	writer.println("        super.collectSlotValues(__props);");
	writer.println("        Object " +val+ ";");
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String slot = (String) entry.getKey();
	    String getter = "get" + fixName(slot, true);
	    writer.println("        " +val+ " = " +getter+ "();");
	    writer.print("        if (" +val+ " != null)");
	    writer.println(props+ ".put(\"" +slot+ "\", " +val+ ");");
	}
	writer.println("    }");
    }

    private void writeAccessors(PrintWriter writer,
				String prototype,
				HashMap local_slots,
				HashMap override_slots)
    {
	Iterator itr = local_slots.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String slot = (String) entry.getKey();
	    Attributes attrs = (Attributes) entry.getValue();
	    writeGetter(writer, prototype, slot, attrs);
	    writeSetter(writer, prototype, slot, attrs);
	    writeInitializer(writer, prototype, slot, attrs);
	    writeRemover(writer, prototype, slot, attrs);
	}
	itr = override_slots.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String slot = (String) entry.getKey();
	    Attributes attrs = (Attributes) entry.getValue();
	    writeGetter(writer, prototype, slot, attrs);
	}
    }


    private void writeGetter(PrintWriter writer, 
			     String prototype,
			     String slot,
			     Attributes attrs)
    {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	String default_value = attrs.getValue("value");
	String path = attrs.getValue("path");
	boolean memberp = isMember(prototype, slot, attrs);
	boolean staticp = path == null && isStatic(prototype, slot, attrs);
	writer.println("\n\n    public Object get" +accessor_name+ "()");
	writer.println("    {");
	if (memberp) {
	    writer.println("        if (" +fixed_name+ " != null) return "
			   +fixed_name+ ";");
	} else {
	    String result_var = "__result";
	    writer.println("        Object " +result_var+ " = getProperty(\"" 
			   +slot+ "\");");
	    writer.println("        if (" +result_var+ " != null) return "
			   +result_var+ ";");
	}
	if (staticp) {
	    if (default_value != null) {
		// Zinky suggestion: NIL -> null
		if (default_value.equals("NIL")) {
		    writer.println("        return null;");
		} else {
		    writer.println("        return \"" +default_value+ "\";");
		}
	    } else {
		writer.println("        getLogger().warn(this + \" has no value for " 
			       +accessor_name+
			       "\");");
		writer.println("        return null;");
	    }
	} else {
	    writer.println("        return getInheritedValue(this, \"" 
			   +slot+ "\");");
	}

	writer.println("    }");
    }

    private void writeSetter(PrintWriter writer,
			     String prototype,
			     String slot,
			     Attributes attrs)
    {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot, attrs);

	writer.println("\n\n    public void set" +accessor_name+
		       "(Object __new_value)");
	writer.println("    {");
	if (memberp) {
	    writer.println("        Object __old_value = " +fixed_name+ ";");
	    writer.println("        this." +fixed_name+ " = __new_value;");
	} else {
	    writer.println("        Object __old_value = getProperty(\"" +slot+ "\");");
	    writer.println("        setProperty(\"" +slot+ "\", __new_value);");
	}
	writer.println("        slotModified(\"" +slot+ "\", __old_value, __new_value);");
	writer.println("    }");
    }

    private void writeRemover(PrintWriter writer, 
			      String prototype,
			      String slot,
			      Attributes attrs)
    {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot, attrs);
	writer.println("\n\n    public void remove" +accessor_name+ "()");
	writer.println("    {");
	if (memberp) {
	    writer.println("        Object __old_value = " +fixed_name+ ";");
	    writer.println("        " +fixed_name+ " = null;");
	} else {
	    writer.println("        Object __old_value = getProperty(\"" 
			   +slot+ "\");");
	    writer.println("        removeProperty(\"" +slot+ "\");");
	}
	writer.println("        slotModified(\"" 
		       +slot+ "\", __old_value, get" +accessor_name+ "());");
	writer.println("    }");
    }

    private void writeInitializer(PrintWriter writer, 
				  String prototype,
				  String slot,
				  Attributes attrs)
    {
	String accessor_name = fixName(slot, true);
	String fixed_name = fixName(slot, false);
	boolean memberp = isMember(prototype, slot, attrs);

	writer.println("\n\n    public void initialize" +accessor_name+
		       "(Object new_value)");
	writer.println("    {");
	if (memberp)
	    writer.println("        this." +fixed_name+ " = new_value;");
	else
	    writer.println("        setProperty(\"" +slot+ "\", new_value);");
	writer.println("        slotInitialized(\"" +slot+ "\", new_value);");
	writer.println("    }");
    }

    private void writeDynamicAccessors(PrintWriter writer, 
				       String prototype,
				       String parent,
				       HashMap local_slots,
				       String container)
    {
	HashSet slots = new HashSet();
	slots.addAll(local_slots.keySet());
	boolean is_root = parent == null;
	if (!slots.isEmpty()) {
	    generateHashConstants(writer, slots);
	    writeDynamicSetter(writer, slots, is_root);
	    writeDynamicInitializer(writer, slots, is_root);
	    writeDynamicRemover(writer, slots, is_root);
	}
	if (container != null) {
	    Set pslots = collectSlots(container);
	    pslots.removeAll(slots);
	    generateHashConstants(writer, pslots);
	    slots.addAll(pslots);
	}
	if (!slots.isEmpty())
	    writeDynamicGetter(writer, slots, is_root);
    }

    private String slotHashVar(String slot)
    {
	return fixName(slot, false) +"__HashVar__";
    }

    private void generateHashConstants(PrintWriter writer, Set slots)
    {
	Iterator itr = slots.iterator();
	while (itr.hasNext()) {
	    String slot = (String) itr.next();
	    String slot_hash_var = slotHashVar(slot);
	    writer.println("    private static final int " +slot_hash_var+
			   " = \"" +slot+ "\".hashCode();");
	}
    }

    private void writeDynamicGetter(PrintWriter writer, 
				    Set slots,
				    boolean is_root)
    {
	writer.println("\n\n    protected Object getLocalValue(String __slot)");
	writer.println("    {");
	writer.println("       int __key = __slot.hashCode();");
	Iterator itr = slots.iterator();
	writer.print("      ");
	while(itr.hasNext()) {
	    String slot = (String) itr.next();
	    String slot_hash_var = slotHashVar(slot);
	    String method = "get" + fixName(slot, true);
	    writer.println(" if (" +slot_hash_var+ " == __key)");
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
				    Set slots,
				    boolean is_root)
    {
	writer.println("\n\n    protected void setLocalValue(String __slot,");
	writer.println("                                 Object __value)");
	writer.println("    {");
	writer.println("       int __key = __slot.hashCode();");
	Iterator itr = slots.iterator();
	boolean first = true;
	while(itr.hasNext()) {
	    String slot = (String) itr.next();
	    String slot_hash_var = slotHashVar(slot);
	    String method = "set" + fixName(slot, true);
	    writer.print("      ");
	    if (!first) writer.print(" else");
	    writer.println(" if (" +slot_hash_var+ " == __key)");
	    writer.println("            " +method+ "(__value);");
	    first = false;
	}
	if (!is_root) {
	    writer.println("       else");
	    writer.println("            super.setLocalValue(__slot, __value);");
	}
	writer.println("    }");
    }

    private void writeDynamicInitializer(PrintWriter writer, 
					 Set slots,
					 boolean is_root)
    {
	writer.println("\n\n    protected void initializeLocalValue(String __slot,");
	writer.println("                                 Object __value)");
	writer.println("    {");
	writer.println("       int __key = __slot.hashCode();");
	Iterator itr = slots.iterator();
	boolean first = true;
	while(itr.hasNext()) {
	    String slot = (String) itr.next();
	    String slot_hash_var = slotHashVar(slot);
	    String method = "initialize" + fixName(slot, true);
	    writer.print("      ");
	    if (!first) writer.print(" else");
	    writer.println(" if (" +slot_hash_var+ " == __key)");
	    writer.println("            " +method+ "(__value);");
	    first = false;
	}
	if (!is_root) {
	    writer.println("       else");
	    writer.println("            super.initializeLocalValue(__slot, __value);");
	}
	writer.println("    }");
    }

    private void writeDynamicRemover(PrintWriter writer, 
				     Set slots,
				     boolean is_root)
    {
	writer.println("\n\n    protected void removeLocalValue(String __slot)");
	writer.println("    {");
	writer.println("       int __key = __slot.hashCode();");
	Iterator itr = slots.iterator();
	boolean first = true;
	while(itr.hasNext()) {
	    String slot = (String) itr.next();
	    String slot_hash_var = slotHashVar(slot);
	    String method = "remove" + fixName(slot, true);
	    writer.print("      ");
	    if (!first) writer.print(" else");
	    writer.println(" if (" +slot_hash_var+ " == __key)");
	    writer.println("            " +method+ "();");
	    first = false;
	}
	if (!is_root) {
	    writer.println("       else");
	    writer.println("            super.removeLocalValue(__slot);");
	}
	writer.println("    }");
    }

    private void writeContainerReaders(PrintWriter writer, 
				       String prototype,
				       HashMap local_slots,
				       String container)
    {
	if (container == null) return;
	HashSet container_accessors = collectSlots(container);
	Iterator itr = container_accessors.iterator();
	while (itr.hasNext()) {
	    String slot = (String) itr.next();
	    if(!local_slots.containsKey(slot) && !inheritsSlot(prototype, slot))
		writeContainerReader(writer, container, slot);
	}
    }

    private void writeContainerReader(PrintWriter writer, 
				      String container,
				      String slot)
    {
	String reader_name = "get"+fixName(slot, true);
	String container_class = fixName(container, true);
	String raw_container_var = "__raw_container";
	String container_var = "__container";
	writer.println("\n\n    public Object " +reader_name+ "()");
	writer.println("    {");
	writer.println("       Object " +raw_container_var+ " = containerFrame();");
	writer.println("       if ( " + raw_container_var + " == null) return null;");
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
			       HashMap local_slots,
			       String container)
    {
	if (container == null) return;
	HashSet container_accessors = collectSlots(container);
	Iterator itr = container_accessors.iterator();

	String old_arg = "__old_frame";
	String new_arg = "__new_frame";
	String old_var = "__old";
	String new_var = "__new";
	String classname = fixName(container, true);

	writer.println("\n\n    protected void fireContainerChanges(" +
		       "DataFrame __raw_old, DataFrame __raw_new)");
	writer.println("    {");
	writer.println("        " +classname+ " " +old_arg+ " = ("
		       +classname+ ") __raw_old;");
	writer.println("        " +classname+ " " +new_arg+ " = ("
		       +classname+ ") __raw_new;");
	writer.println("        Object " +old_var+ ";");
	writer.println("        Object " +new_var+ ";");
	while (itr.hasNext()) {
	    String slot = (String) itr.next();
	    if(local_slots.containsKey(slot) || inheritsSlot(prototype, slot))
		continue;

	    String fixed_name = fixName(slot, true);
	    String bean_name = fixName(slot, true, true);
	    String reader = "get" +fixed_name+ "();";
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

	itr = container_accessors.iterator();
	writer.println("\n\n    protected void fireContainerChanges(DataFrame __raw)");
	writer.println("    {");
	writer.println("        " +classname+ " " +new_arg+ " = ("
		       +classname+ ") __raw;");
	writer.println("        Object " +new_var+ ";");
	while (itr.hasNext()) {
	    String slot = (String) itr.next();
	    if(local_slots.containsKey(slot) || inheritsSlot(prototype, slot))
		continue;

	    String fixed_name = fixName(slot, true);
	    String bean_name = fixName(slot, true, true);
	    String reader = "get" +fixed_name+ "();";
	    writer.println("        " +new_var+ " = " +new_arg+ "." + reader);
	    writer.println("        if (" +new_var+ " != null) {");
	    writer.println("            fireChange(\"" +bean_name+ "\", "
			   +"null"+ ", " +new_var+ ");");
	    writer.println("        }");
	}
	writer.println("    }");

   }

    // Driver

    public static void main(String[] args)
    {
	String path = args[0];
	FrameGen generator = new FrameGen(path);
	for (int i=1; i<args.length; i++) {
	    File file = new File(args[i]);
	    generator.parseProtoFile(file);
	}
    }

	    




}

