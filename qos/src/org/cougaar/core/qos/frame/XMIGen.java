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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author rshapiro
 *
 */
public class XMIGen extends DefaultHandler {
    
    private static final String DRIVER_PROPERTY = "org.xml.sax.driver";
    private static final String DRIVER_DEFAULT =
	"org.apache.crimson.parser.XMLReaderImpl";
    
    static {
	String driver = System.getProperty(DRIVER_PROPERTY);
	if (driver == null) 
	    System.setProperty(DRIVER_PROPERTY, DRIVER_DEFAULT);
    }
    
    private long uidRef;
    private String package_name;
    private String domain;
    private String current_prototype;
    private Map<String,Attributes> slots;
    private Map<String,Attributes> proto_attrs; 
    private Map<String,Map<String,Attributes>> proto_slots; 
    private Set<String> relation_prototypes;
    private Map<String,String> class_uids;
    private Set<String> processedProtos;
    private Map<String,Type> data_types;
    private PrintWriter writer;
    
    public void parseProtoFile(File xml_file, File output) {
	proto_slots = new HashMap<String,Map<String,Attributes>>();
	proto_attrs = new HashMap<String,Attributes>();
	relation_prototypes = new HashSet<String>();
	class_uids = new HashMap<String,String>();
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
	
	FileWriter fw;
	try {
	    fw = new FileWriter(output);
	    writer = new PrintWriter(fw);
	    write();
	    writer.close();
	    System.out.println("Wrote " + output);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    //  SAX
    
    public void startElement(String uri, String local, String name, Attributes attrs) {
	if (name.equals("frameset")) {
	    startFrameset(attrs);
	} else if (name.equals("prototype")) {
	    startPrototype(attrs);
	} else if (name.equals("relation-prototype")) {
	    startRelationPrototype(attrs);
	} else if (name.equals("slot")) {
	    slot(attrs);
	}
    }

    public void endElement(String uri, String local, String name) {
	if (name.equals("frameset")) {
	    endFrameset();
	} else if (name.equals("prototype")) {
	    endPrototype();
	} else if (name.equals("relation-prototype")) {
	    endRelationPrototype();
	}
    }

    public void characters(char buf[], int offset, int length) {
	// no-op so far
    }
    
    
    private void startFrameset(Attributes attrs) {
	package_name = attrs.getValue("package");
	domain = attrs.getValue("domain");
    }
    
    private void endFrameset() {
    }

    private void startPrototype(Attributes attrs) {
	current_prototype = attrs.getValue("name");
	proto_attrs.put(current_prototype, new AttributesImpl(attrs));
	slots = new HashMap();
    }

    private void endPrototype() {
	class_uids.put(current_prototype, nextUID());
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
	class_uids.put(current_prototype, nextUID());
	proto_slots.put(current_prototype, slots);
	current_prototype = null;
	slots = null;
    }
    
    private void slot(Attributes attrs) {
	if (slots != null) {
	    String slot = attrs.getValue("name");
	    slots.put(slot, new AttributesImpl(attrs));
	}
    }
    
    
    private class Type {
	final String name;
	final String uid;
	
	Type(String name) {
	    this.name = name;
	    this.uid = nextUID();
	    data_types.put(name, this);
	}
    }
    
    // Output
    
    private String nextUID() {
	String uidString = Long.toString(uidRef++);
	return domain +"-"+ package_name +"-" +uidString;
    }
    
    private String classElement(String proto) {
	if (relation_prototypes.contains(proto)) {
	    return "UML:AssociationClass";
	} else {
	    return "UML:Class";
	}
    }
    
    private void writeXMLVersion() {
	writer.println("<?xml version = '1.0' encoding = 'UTF-8' ?>");
    }
    
    private void writeXMIStart() {
	writer.println("<XMI xmi.version = '1.2' xmlns:UML = 'org.omg.xmi.namespace.UML'>");
    }
    
    private void writeXMIEnd() {
	writer.println("</XMI>");
    }
    
    private void writeHeader() {
	writer.println("  <XMI.header>");
	writer.println("    <XMI.metamodel xmi.name=\"UML\" xmi.version=\"1.4\"/>");
	writer.println("  </XMI.header>");
    }
    
    private void writeContentStart() {
	writer.println("  <XMI.content>");
	writer.println("    <UML:Model xmi.id='" +nextUID()+ "' name='" +domain+ "'>");
	writer.println("      <UML:Namespace.ownedElement>");
    }
    
    private void writeContentEnd() {
	writer.println("      </UML:Namespace.ownedElement>");
	writer.println("    </UML:Model>");
	writer.println("  </XMI.content>");
    }
    
    private void writeGeneralization(String id, String parent, String child) {
	String parent_id = class_uids.get(parent);
	String child_id = class_uids.get(child);
	writer.println("        <UML:Generalization xmi.id='" +id+ "'>");
	writer.println("          <UML:Generalization.child>");
	writer.println("             <" +classElement(child)+ " xmi.idref='" +child_id+ "'/>");
	writer.println("          </UML:Generalization.child>");
	writer.println("          <UML:Generalization.parent>");
	writer.println("             <" +classElement(parent)+ " xmi.idref='" +parent_id+ "'/>");
	writer.println("          </UML:Generalization.parent>");
	writer.println("        </UML:Generalization>");
    }
    
    private void writeMultiplicity(String tag, int min, int max, int indent) {
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("<" +tag+ ".multiplicity>");
	
	indent += 2;
	String mid = nextUID();
	String sid = nextUID();
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("<UML:Multiplicity xmi.id='" +mid+ "'>");
	indent += 2;
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("<UML:Multiplicity.range>");
	indent += 2;
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("<UML:MultiplicityRange xmi.id='" +sid+ "' lower='" +min+ "' upper='"
		+max+ "'/>");
	indent -= 2;
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("</UML:Multiplicity.range>");
	indent -= 2;
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("</UML:Multiplicity>");
	indent -= 2;
	
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("</" +tag+ ".multiplicity>");
    }
    
    private void writeType(Type type, int indent) {
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("<UML:StructuralFeature.type>");
	indent += 2;
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("<UML:DataType xmi.idref='" +type.uid+ "'/>");
	indent -= 2;
	for (int i=0; i<indent; i++) writer.print(' ');
	writer.println("</UML:StructuralFeature.type>");
    }
    
    private void writeAttributes(String proto) {
	writer.println("          <UML:Classifier.feature>");
	Map<String,Attributes> slots = proto_slots.get(proto);
	for (Map.Entry<String,Attributes> entry : slots.entrySet()) {
	    String id = nextUID();
	    String name = entry.getKey();
	    Attributes attrs = entry.getValue();
	    writer.println("            <UML:Attribute xmi.id='" +id+ "' name='" +name+ "'>");
	    writeMultiplicity("UML:StructuralFeature", 1, 1, 14);
	    String typeName = attrs.getValue("type");
	    if (typeName != null) {
		Type type = data_types.get(typeName);
		if (type == null) {
		    type = new Type(typeName);
		}
		writeType(type, 14);
	    }
	    // etc
	    writer.println("            </UML:Attribute>");
	}
	writer.println("          </UML:Classifier.feature>");
    }
    
    private void writeAssociationConnection(String proto) {
	Attributes attrs = proto_attrs.get(proto);
	String parent = attrs.getValue("parent-prototype");
	String parentUID = class_uids.get(parent);
	String child = attrs.getValue("child-prototype");
	String childUID = class_uids.get(child);
	String pid = nextUID();
	String cid = nextUID();
	
	writer.println("      <UML:Association.connection>");
	
	writer.println("         <UML:AssociationEnd xmi.id='" +pid+ "' name='parent-prototype'>");
	writeMultiplicity("UML:AssociationEnd", 1, 1, 10);
	writer.println("         <UML:AssociationEnd.participant>");
	writer.println("              <UML:Class xmi.idref='" +parentUID+ "'/>");
	writer.println("         </UML:AssociationEnd.participant>");
	writer.println("         </UML:AssociationEnd>");
	
	writer.println("         <UML:AssociationEnd xmi.id='" +cid+ "' name='child-prototype'>");
	writeMultiplicity("UML:AssociationEnd", 1, 1, 10);
	writer.println("         <UML:AssociationEnd.participant>");
	writer.println("              <UML:Class xmi.idref='" +childUID+ "'/>");
	writer.println("         </UML:AssociationEnd.participant>");
	writer.println("         </UML:AssociationEnd>");
	
	writer.println("      </UML:Association.connection>");
    }
    
    private void writeClass(String proto) {
	if (processedProtos.contains(proto)) {
	    // already been here
	    return;
	}
	Attributes attrs = proto_attrs.get(proto);
	String parent = attrs.getValue("prototype");
	String generalizationId = null;
	if (parent != null) {
	    writeClass(parent);
	    // write the generalization
	    generalizationId = nextUID();
	    writeGeneralization(generalizationId, parent, proto);
	}
	// write myself
	String elt = classElement(proto);
	String id = class_uids.get(proto);
	writer.println("        <" + elt+ " xmi.id='" +id+ "' name='" +proto+ "'>");
	if (generalizationId != null) {
	    writer.println("          <UML:GeneralizableElement.generalization>");
	    writer.println("            <UML:Generalization xmi.idref ='" +generalizationId+ "'/>");
	    writer.println("          </UML:GeneralizableElement.generalization>");
	}
	writeAttributes(proto);
	
	if (relation_prototypes.contains(proto)) {
	    writeAssociationConnection(proto);
	}
	
	writer.println("        </" +elt+ ">");
	processedProtos.add(proto);
    }
    
    private void writeType(Type type) {
	writer.println("        <UML:DataType xmi.id='" +type.uid+
		"' name='" +type.name+ "'/>");
    }
    
    private void writeContentBody() {
	processedProtos = new HashSet<String>();
	data_types = new HashMap<String,Type>();
	for (String proto : proto_attrs.keySet()) {
	    writeClass(proto);
	}
	for (Type type : data_types.values()) {
	    writeType(type);
	}
    }
    
    private void write() throws IOException {
	writeXMLVersion();
	writeXMIStart();
	writeHeader();
	writeContentStart();
	writeContentBody();
	writeContentEnd();
	writeXMIEnd();
    }
    
    public static void main(String[] args) {

	File inputFile = null, outputFile = null;
	if (args.length == 2) {
	    inputFile = new File(args[0]);
	    outputFile = new File(args[1]);
	} else {
	    JFileChooser chooser = new JFileChooser();
	    int option = chooser.showOpenDialog(null);
	    if (option == JFileChooser.APPROVE_OPTION) {
		inputFile = chooser.getSelectedFile();
	    } else {
		System.exit(0);
	    }

	    option = chooser.showSaveDialog(null);
	    if (option == JFileChooser.APPROVE_OPTION) {
		outputFile = chooser.getSelectedFile();
	    } else {
		System.exit(0);
	    }
	}
	XMIGen generator = new XMIGen();
	generator.parseProtoFile(inputFile, outputFile);


    }
}
