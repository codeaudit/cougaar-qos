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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;


/**
 * This class parses frame and protocol xml files and creates the
 * corresponding {@link PrototypeFrame}s and {@link DataFrame}s .  The
 * parsing is SAX.
 */
public class FrameSetParser
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




    // Helper structs
    abstract private class FrameSpec
    {
	Properties props;
	String prototype;
	FrameSpec()
	{
	    props = new Properties();
	}

	void put(String attr, Object value)
	{
	    props.put(attr, value);
	}
    }

    private class DataFrameSpec
	extends FrameSpec
    {
	String reference;

	DataFrameSpec(Attributes attrs)
	{
	    super();
	    prototype = attrs.getValue("prototype");
	    reference = attrs.getValue("reference");
	}

	Frame makeFrame(FrameSet frameSet)
	{
	    Frame frame = frameSet.makeFrame(prototype, props);
	    if (reference != null) {
		synchronized (references) {
		    references.put(reference, frame);
		}
	    }
	    return frame;
	}
    }

    private class PrototypeSpec
	extends FrameSpec
    {
	String name;

	PrototypeSpec(Attributes attrs)
	{
	    super();
	    name = attrs.getValue("name");
	    prototype = attrs.getValue("prototype");
	}

	Frame makePrototype(FrameSet frameSet)
	{
	    PrototypeFrame frame =
		frameSet.makePrototype(name, prototype, props);
	    return frame;
	}

    }

    private class PathSpec
    {
	String name;
	ArrayList path;
	String slot;

	PathSpec(String name)
	{
	    this.name = name;
	    path = new ArrayList();
	}

	void addToPath(String role, String relation)
	{
	    path.add(new Path.Fork(role, relation));
	}

	void setSlot(String slot)
	{
	    this.slot = slot;
	}

	Path makePath(FrameSet frameSet)
	{
	    Path.Fork[] array = new Path.Fork[path.size()];
	    for (int i=0; i<path.size(); i++)
		array[i] = (Path.Fork) path.get(i);
	    return frameSet.makePath(name, array, slot);
	}

    }


    private String frame_set_name;
    private FrameSet frame_set;
    private DataFrameSpec frame_spec;
    private PrototypeSpec proto_spec;
    private PathSpec path_spec;
    private String current_slot;
    private HashMap path_specs;
    private HashMap references;

    private ServiceBroker sb;
    private BlackboardService bbs;
    private LoggingService log;

    public FrameSetParser(ServiceBroker sb, BlackboardService bbs)
    {
	this.sb = sb;
	this.bbs = bbs;
	this.log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	path_specs = new HashMap();
    }

    public FrameSet parseFrameSetFiles(String name, String[] xml_filenames)
    {
	if (xml_filenames == null || xml_filenames.length == 0)  return null;

	FrameSet fset = parseFrameSetFile(name, xml_filenames[0], null);
	for (int i=1; i<xml_filenames.length; i++)
	    parseFrameSetFile(name, xml_filenames[i], fset);
	return fset;
    }

    public FrameSet parseFrameSetFile(String name, String xml_filename)
    {
	return parseFrameSetFile(name, xml_filename, null);
    }

    public FrameSet parseFrameSetFile(String name,
				      String xml_filename,
				      FrameSet frameSet)
    {
	if (log.isInfoEnabled())
	    log.info("FrameSet " +name+ " file " +xml_filename);
		
	this.frame_set = frameSet;
	this.frame_set_name = name;
	File xml_file = ConfigFinder.getInstance().locateFile(xml_filename);
	if (xml_file == null) {
	    if (log.isWarnEnabled())
		log.warn("Can't find FrameSet file " + xml_filename);
	    return null;
	}
	try {
	    XMLReader producer = XMLReaderFactory.createXMLReader();
	    DefaultHandler consumer = this; 
	    producer.setContentHandler(consumer);
	    producer.setErrorHandler(consumer);
	    URL url = xml_file.toURL();
	    producer.parse(url.toString());
	} catch (Throwable ex) {
	    log.error("Error parsing FrameSet file " + xml_file, ex);
	}
	return frame_set;
    }


    public void startElement(String uri, String local, String name, 
			     Attributes attrs)
    {
	try {
	    if (frame_spec != null) {
		// inner structures are always slot values
		current_slot = name;
	    } else  if (name.equals("frameset")) {
		startFrameset(attrs);
	    } else if (name.equals("prototypes")) {
		// no-op
	    } else if (name.equals("prototype")) {
		startPrototype(attrs);
	    } else if (name.equals("relation-prototype")) {
		startPrototype(attrs);
	    } else if (name.equals("slot")) {
		slot(attrs);
	    } else if (name.equals("fork")) {
		fork(attrs);
	    } else if (name.equals("frames")) {
		references = new HashMap();
	    } else if (name.equals("frame")) {
		startFrame(attrs);
	    } else if (name.equals("path")) {
		startPath(attrs);
	    } 
	} catch (Exception ex) {
	    log.error("startElement " +name, ex);
	}
    }

    public void endElement(String uri, String local, String name)
    {
	try {
	    if (name.equals("frameset")) {
		endFrameset();
	    } else if (name.equals("prototypes")) {
		// no-op
	    } else if (name.equals("prototype")) {
		endPrototype();
	    } else if (name.equals("relation-prototype")) {
		endPrototype();
	    } else if (name.equals("frames")) {
		references = null;
	    } else if (name.equals("frame")) {
		endFrame();
	    } else if (name.equals("path")) {
		endPath();
	    } 
	} catch (Exception ex) {
	    log.error("endElement " +name, ex);
	}
    }

    // Not using this yet
    public void characters(char buf[], int offset, int length)
    {
	try {
	    if (current_slot != null) {
		String value = new String(buf, offset, length);
		if (value.startsWith("?")) {
		    // Better to look up the slot in the prototype to see
		    // if its type is 'reference'.  Do that later.
		    Frame ref;
		    synchronized (references) {
			ref = (Frame) references.get(value);
		    }
		    log.shout("Resolved " +value+ " to " +ref);
		    if (ref != null) value = ref.getUID().toString();
		}
		frame_spec.put(current_slot, value);
		current_slot = null;
	    }
	} catch (Exception ex) {
	    log.error(null, ex);
	}
    }




    private void startFrameset(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startFrameset");
	

	if (frame_set != null) return;

	String pkg = attrs.getValue("package");
	String container_relation = attrs.getValue("container-relation");
	String inheritance = attrs.getValue("frame-inheritance");
	if (!inheritance.equals("single")) {
	    throw new RuntimeException("Only single-inheritance FrameSets are supported!");
	}

	frame_set = new SingleInheritanceFrameSet(pkg, sb, bbs, frame_set_name,
						  container_relation);
    }


    private void startPrototype(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startPrototype");

	proto_spec = new PrototypeSpec(attrs);
    }

    private void endPrototype()
    {
	if (log.isDebugEnabled())
	    log.debug("endPrototype");

	proto_spec.makePrototype(frame_set);
	proto_spec = null;
    }

    private void startFrame(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startFrame");

	frame_spec = new DataFrameSpec(attrs);
    }

    private void endFrame()
    {
	if (log.isDebugEnabled())
	    log.debug("endFrame");

	frame_spec.makeFrame(frame_set);
	frame_spec = null;
    }



    private void startPath(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startPath");

	String name = attrs.getValue("name");
	path_spec = new PathSpec(name);
    }

    private void fork(Attributes attrs)
    {
	String role = attrs.getValue("role");
	String relation = attrs.getValue("relation");
	path_spec.addToPath(role, relation);
    }

    private void endPath()
    {
	if (log.isDebugEnabled())
	    log.debug("endPath");

	path_specs.put(path_spec.name, path_spec.makePath(frame_set));
	path_spec = null;
    }




    private void slot(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("slot");

	String slot = attrs.getValue("name");
	if (path_spec != null) {
	    path_spec.setSlot(slot);
	} else if (proto_spec != null) {
	    proto_spec.put(slot, new AttributesImpl(attrs));
	} else {
	    // log
	}
    }

    private void endFrameset()
    {
	// no-op
    }

}
