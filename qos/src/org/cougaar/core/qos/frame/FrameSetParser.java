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
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;


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
    private class FrameSpec
    {
	String kind;
	String parent;
	Properties props;
	HashMap paths;

	FrameSpec(String kind, String parent)
	{
	    this.kind = kind;
	    this.parent = parent;
	    props = new Properties();
	    paths = new HashMap();
	}

	Frame makePrototype(FrameSet frameSet)
	{
	    PrototypeFrame frame = frameSet.makePrototype(kind, parent, props);
	    if (!paths.isEmpty()) frame.addPaths(paths);
	    return frame;
	}

	Frame makeFrame(FrameSet frameSet)
	{
	    return frameSet.makeFrame(kind, props);
	}


	void put(String attr, String value)
	{
	    props.setProperty(attr, value);
	}

	void putPath(String attr, VisitorPath path)
	{
	    paths.put(attr, path);
	}
    }

    private class VisitorSpec
    {
	String name;
	ArrayList path;
	String slot;

	VisitorSpec(String name)
	{
	    this.name = name;
	    path = new ArrayList();
	}

	void addToPath(String role, String relation)
	{
	    path.add(new VisitorPath.Entry(role, relation));
	}

	void setSlot(String slot)
	{
	    this.slot = slot;
	}

	VisitorPath makePath()
	{
	    VisitorPath.Entry[] array = (VisitorPath.Entry[]) path.toArray();
	    return new VisitorPath(name, array, slot);
	}

    }



    private SingleInheritanceFrameSet frame_set;
    private FrameSpec frame_spec;
    private VisitorSpec visitor_spec;
    HashMap visitor_specs;

    private ServiceBroker sb;
    private BlackboardService bbs;
    private LoggingService log;

    public FrameSetParser(ServiceBroker sb, BlackboardService bbs)
    {
	this.sb = sb;
	this.bbs = bbs;
	this.log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
	visitor_specs = new HashMap();
    }

    public FrameSet parseFrameSetFile(String xml_filename)
    {
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
	} catch (Exception ex) {
	    log.error("Error parsing FrameSet file " + xml_file, ex);
	}
	return frame_set;
    }


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
	} else if (name.equals("visit")) {
	    visit(attrs);
	} else if (name.equals("frames")) {
	    // no-op
	} else if (name.equals("frame")) {
	    startFrame(attrs);
	} else if (name.equals("visitor-path")) {
	    startVisitor(attrs);
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
	} else if (name.equals("frames")) {
	    // no-op
	} else if (name.equals("frame")) {
	    endFrame();
	} else if (name.equals("visitor-path")) {
	    endVisitor();
	} 
    }

    // Not using this yet
    public void characters(char buf[], int offset, int length)
    {
    }




    private void startFrameset(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startFrameset");

	String inheritance = attrs.getValue("frame-inheritance");
	if (!inheritance.equals("single")) {
	    throw new RuntimeException("Only single-inheritance FrameSets are supported!");
	}

	String name = attrs.getValue("name");
	String relation_name = attrs.getValue("frame-inheritance-relation");
	String parent_proto = attrs.getValue("parent-prototype");
	String parent_slot = attrs.getValue("parent-slot");
	String parent_value = attrs.getValue("parent-value");
	String child_proto = attrs.getValue("child-prototype");
	String child_slot = attrs.getValue("child-slot");
	String child_value = attrs.getValue("child-value");

	frame_set = new SingleInheritanceFrameSet(sb, bbs,
						  name,
						  relation_name,
						  parent_proto,
						  parent_slot,
						  parent_value,
						  child_proto,
						  child_slot,
						  child_value);
    }


    private void startPrototype(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startPrototype");

	String name = attrs.getValue("name");
	String parent = attrs.getValue("prototype");
	frame_spec = new FrameSpec(name, parent);
    }

    private void endPrototype()
    {
	if (log.isDebugEnabled())
	    log.debug("endPrototype");

	frame_spec.makePrototype(frame_set);
	frame_spec = null;
    }

    private void startFrame(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startFrame");

	String prototype = attrs.getValue("prototype");
	frame_spec = new FrameSpec(prototype, null);
    }

    private void endFrame()
    {
	if (log.isDebugEnabled())
	    log.debug("endFrame");

	frame_spec.makeFrame(frame_set);
	frame_spec = null;
    }



    private void startVisitor(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startVisitor");

	String name = attrs.getValue("name");
	visitor_spec = new VisitorSpec(name);
    }

    private void visit(Attributes attrs)
    {
	String role = attrs.getValue("role");
	String relation = attrs.getValue("relation");
	visitor_spec.addToPath(role, relation);
    }

    private void endVisitor()
    {
	if (log.isDebugEnabled())
	    log.debug("endVisitor");

	visitor_specs.put(visitor_spec.name, visitor_spec.makePath());
	visitor_spec = null;
    }




    private void slot(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("slot");

	String slot = attrs.getValue("name");
	if (visitor_spec != null) {
	    visitor_spec.setSlot(slot);
	} else if (frame_spec != null) {
	    String path = attrs.getValue("path");
	    if (path != null) {
		VisitorPath vp = (VisitorPath) visitor_specs.get(path);
		frame_spec.putPath(slot, vp);
	    } else {
		String value = attrs.getValue("value");
		frame_spec.put(slot, value);
	    }
	} else {
	    // log
	}
    }

    private void endFrameset()
    {
	frame_set.setVisitors(visitor_specs);
    }

}
