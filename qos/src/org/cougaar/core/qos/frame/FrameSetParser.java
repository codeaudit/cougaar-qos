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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * This class parses frame and protocol xml files and creates the
 * corresponding {@link PrototypeFrame}s and {@link DataFrame}s .  The
 * parsing is SAX.
 */
public class FrameSetParser
    extends DefaultHandler {
    private static final String DRIVER_PROPERTY = "org.xml.sax.driver";
    private static final String DRIVER_DEFAULT =
	"org.apache.crimson.parser.XMLReaderImpl";

    static {
	String driver = System.getProperty(DRIVER_PROPERTY);
	if (driver == null) 
	    System.setProperty(DRIVER_PROPERTY, DRIVER_DEFAULT);
    }

    private String frame_set_name;
    private FrameSet frame_set;
    private PrototypeSpec proto_spec;
    private PathSpec path_spec;
    private String slot_def; // slot being defined when we're parsing prototypes xml
    private ServiceBroker sb;
    private BlackboardService bbs;
    private Logger log;
    
    public FrameSetParser() {
	this.log = Logging.getLogger(getClass().getName());
    }

    public FrameSetParser(ServiceBroker sb, BlackboardService bbs) {
	this();
	this.sb = sb;
	this.bbs = bbs;
    }

    public FrameSet parseFrameSetFiles(String name, String[] xml_filenames) {
	if (xml_filenames == null || xml_filenames.length == 0)  return null;

	FrameSet fset = parseFrameSetFile(name, xml_filenames[0], null);
	for (int i=1; i<xml_filenames.length; i++)
	    parseFrameSetFile(name, xml_filenames[i], fset);
	return fset;
    }

    public FrameSet parseFrameSetFile(String name, String xml_filename) {
	return parseFrameSetFile(name, xml_filename, null);
    }

    public FrameSet parseFrameSetFile(String name,
				      String xml_filename,
				      FrameSet frameSet) {
	ClassLoader loader = FrameSetParser.class.getClassLoader();
	URL url = loader.getResource(xml_filename);
	if (url == null) {
	    // Try it as a file
	    File file = new File(xml_filename);
	    try {
		url = file.toURL();
	    } catch (MalformedURLException e) {
		log.error("Bogus FrameSet data location " + xml_filename, e);
		return frameSet;
	    }
	}
	return parseFrameSetData(name, url, frameSet);
    }
    
    public FrameSet parseFrameSetData(String name, final URL url, FrameSet frameSet) {
	if (log.isInfoEnabled())
	    log.info("Loading FrameSet" +name+ " from " +url);
		
	this.frame_set = frameSet;
	this.frame_set_name = name;
	try {
	    final XMLReader producer = XMLReaderFactory.createXMLReader();
	    producer.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", 
		    false);
	    DefaultHandler consumer = this; 
	    producer.setContentHandler(consumer);
	    producer.setErrorHandler(consumer);
	    if (frame_set == null) {
		producer.parse(url.toString());
	    } else {
		// run in a transaction
		Runnable r = new Runnable() {
		    public void run() {
			try {
			    producer.parse(url.toString());
			} catch (Throwable ex) {
			    log.error("Error parsing FrameSet file " + url, ex);
			}
		    }
		};
		frame_set.runInTransaction(r);
	    }
	} catch (Throwable ex) {
	    log.error("Error parsing FrameSet file " + url, ex);
	}
	return frame_set;
    }


    public void startElement(String uri, String local, String name, 
			     Attributes attrs) {
	try {
	    if (name.equals("frameset")) {
		startFrameset(attrs);
	    } else if (name.equals("prototypes")) {
		// no-op
	    } else if (name.equals("prototype")) {
		startPrototype(attrs);
	    } else if (name.equals("relation-prototype")) {
		startPrototype(attrs);
	    } else if (name.equals("slot")) {
		startSlot(attrs);
	    } else if (name.equals("slot-reference")) {
		slot_reference(attrs);
	    } else if (name.equals("aggregate-by")) {
		String relatedSlot = attrs.getValue("related-slot");
		String relation = attrs.getValue("relation");
		String cname = attrs.getValue("aggregator");
		String role = attrs.getValue("role");
		frame_set.addAggregator(slot_def, relatedSlot, relation, role, cname);
	    } else if (name.equals("fork")) {
		fork(attrs);
	    } else if (name.equals("path")) {
		startPath(attrs);
	    } else if (name.equals("copyright")) {
		// no-op
	    } else {
		// implicit data frame
		makeFrame(name, attrs);
	    }
	} catch (Exception ex) {
	    log.error("startElement " +name, ex);
	}
    }

    public void endElement(String uri, String local, String name) {
	try {
	    if (name.equals("frameset")) {
		endFrameset();
	    } else if (name.equals("prototypes")) {
		// no-op
	    } else if (name.equals("prototype")) {
		endPrototype();
	    } else if (name.equals("relation-prototype")) {
		endPrototype();
	    } else if (name.equals("path")) {
		endPath();
	    } else if (name.equals("fork")) {
		// no-op
	    } else if (name.equals("slot")) {
		endSlot();
	    } else if (name.equals("slot-reference")) {
		// no-op
	    } else if (name.equals("aggregate-by")) {
		// no-op
	    } else if (name.equals("copyright")) {
		// no-op
	    } else {
		// implicit data frame
	    }
	} catch (Exception ex) {
	    log.error("endElement " +name, ex);
	}
    }

    public void characters(char buf[], int offset, int length) {
	// no-op
    }




    private void startFrameset(Attributes attrs) {
	if (log.isDebugEnabled())
	    log.debug("startFrameset");
	

	if (frame_set != null) return;

	String pkg = attrs.getValue("package");
	String container_relation = attrs.getValue("container-relation");
	String inheritance = attrs.getValue("frame-inheritance");
	String indexSlot = attrs.getValue("index-slot");
	if (!inheritance.equals("single")) {
	    throw new RuntimeException("Only single-inheritance FrameSets are supported!");
	}
	String domain = attrs.getValue("domain");

	frame_set = new SingleInheritanceFrameSet(pkg, sb, bbs, domain, frame_set_name,
						  container_relation, indexSlot);
    }


    private void startPrototype(Attributes attrs) {
	if (log.isDebugEnabled())
	    log.debug("startPrototype");

	proto_spec = new PrototypeSpec(attrs);
    }

    private void endPrototype() {
	if (log.isDebugEnabled())
	    log.debug("endPrototype");

	proto_spec.makePrototype(frame_set);
	proto_spec = null;
    }

    private void makeFrame(String name, Attributes attrs) {
	if (log.isDebugEnabled())
	    log.debug("startFrame");

	DataFrameSpec frame_spec = new DataFrameSpec(name, attrs);
	frame_spec.makeFrame(frame_set);
    }

    private void startPath(Attributes attrs) {
	if (log.isDebugEnabled())
	    log.debug("startPath");

	String name = attrs.getValue("name");
	path_spec = new PathSpec(name);
    }

    private void fork(Attributes attrs) {
	String role = attrs.getValue("role");
	String relation = attrs.getValue("relation");
	path_spec.addToPath(role, relation);
    }

    private void endPath() {
	if (log.isDebugEnabled())
	    log.debug("endPath");
	path_spec.makePath(frame_set);
	path_spec = null;
    }




    private void startSlot(Attributes attrs) {
	if (log.isDebugEnabled())
	    log.debug("slot");

	if (proto_spec != null) {
	    slot_def = attrs.getValue("name");
	    proto_spec.put(slot_def, new AttributesImpl(attrs));
	} else {
	    // log
	}
    }
    
    private void endSlot() {
	slot_def = null;
    }

    private void slot_reference(Attributes attrs) {
	if (log.isDebugEnabled())
	    log.debug("slot-reference");

	if (path_spec != null) {
	    String slot = attrs.getValue("name");
	    path_spec.setSlot(slot);
	} else {
	    // log
	}
    }

    private void endFrameset() {
	// no-op
    }
    
    
    

    // Helper structs
    private class DataFrameSpec {
	String prototype;
	Properties slotValues;
	
	DataFrameSpec(String name, Attributes attrs)	{
	    slotValues = new Properties();
	    prototype = name;
	    for (int i=0; i<attrs.getLength(); i++) {
		String pname = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		slotValues.put(pname, value);
	    }
	}

	Frame makeFrame(FrameSet frameSet) {
	    PrototypeFrame pframe = frameSet.findPrototypeFrame(prototype);
	    if (pframe == null) return null;
	    
	    Frame frame = frameSet.makeFrame(prototype, slotValues);
	    return frame;
	}
    }

    private class PrototypeSpec	{
	String name;
	Attributes attrs;
	Map<String,Attributes> slots;
	String prototype;
	
	PrototypeSpec(Attributes attrs) {
	    slots = new HashMap<String,Attributes>();
	    name = attrs.getValue("name");
	    prototype = attrs.getValue("prototype");
	    this.attrs = new AttributesImpl(attrs);
	}
	
	void put(String slot, Attributes attrs) {
	    slots.put(slot, attrs);
	}

	Frame makePrototype(FrameSet frameSet) {
	    PrototypeFrame frame =
		frameSet.makePrototype(name, prototype, attrs, slots);
	    return frame;
	}

    }

    private class PathSpec {
	String name;
	ArrayList path;
	String slot;

	PathSpec(String name) {
	    this.name = name;
	    path = new ArrayList();
	}

	void addToPath(String role, String relation) {
	    path.add(new Path.Fork(role, relation));
	}

	void setSlot(String slot) {
	    this.slot = slot;
	}

	Path makePath(FrameSet frameSet) {
	    Path.Fork[] array = new Path.Fork[path.size()];
	    for (int i=0; i<path.size(); i++)
		array[i] = (Path.Fork) path.get(i);
	    return frameSet.makePath(name, array, slot);
	}

    }


}
