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
	HashSet requiredSlots;

	FrameSpec(String kind, String parent)
	{
	    this.kind = kind;
	    this.parent = parent;
	    props = new Properties();
	    paths = new HashMap();
	    requiredSlots = new HashSet();
	}

	Frame makePrototype(FrameSet frameSet)
	{
	    PrototypeFrame frame = frameSet.makePrototype(kind, parent, props);
	    if (!paths.isEmpty()) frame.addPaths(paths);
	    if (!requiredSlots.isEmpty()) frame.addRequiredSlots(requiredSlots);
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

	void putPath(String attr, Path path)
	{
	    paths.put(attr, path);
	}

	void addRequiredSlot(String name)
	{
	    requiredSlots.add(name);
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



    private FrameSet frame_set;
    private FrameSpec frame_spec;
    private FrameSpec proto_spec;
    private PathSpec path_spec;
    private String current_slot;
    private HashMap path_specs;

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

    public FrameSet parseFrameSetFiles(String[] xml_filenames)
    {
	if (xml_filenames == null || xml_filenames.length == 0)  return null;

	FrameSet fset = parseFrameSetFile(xml_filenames[0], null);
	for (int i=1; i<xml_filenames.length; i++)
	    parseFrameSetFile(xml_filenames[i], fset);
	return fset;
    }

    public FrameSet parseFrameSetFile(String xml_filename)
    {
	return parseFrameSetFile(xml_filename, null);
    }

    public FrameSet parseFrameSetFile(String xml_filename,
				      FrameSet frameSet)
    {
	this.frame_set = frameSet;
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
	if (frame_spec != null) {
	    // inner structures are always slot values
	    current_slot = name;
	} else	if (name.equals("frameset")) {
	    startFrameset(attrs);
	} else if (name.equals("prototypes")) {
	    // no-op
	} else if (name.equals("prototype")) {
	    startPrototype(attrs);
	} else if (name.equals("slot")) {
	    slot(attrs);
	} else if (name.equals("fork")) {
	    fork(attrs);
	} else if (name.equals("frames")) {
	    // no-op
	} else if (name.equals("frame")) {
	    startFrame(attrs);
	} else if (name.equals("path")) {
	    startPath(attrs);
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
	} else if (name.equals("path")) {
	    endPath();
	} 
    }

    // Not using this yet
    public void characters(char buf[], int offset, int length)
    {
	if (current_slot != null) {
	    String value = new String(buf, offset, length);
// 	    log.shout("Setting " +current_slot+ " to " +value);
	    frame_spec.put(current_slot, value);
	    current_slot = null;
	}
    }




    private void startFrameset(Attributes attrs)
    {
	if (log.isDebugEnabled())
	    log.debug("startFrameset");
	
	String name = attrs.getValue("name");
	String pkg_prefix = attrs.getValue("package");

	if (frame_set != null) {
	    // add to existing set, as long as the name is the same
	    if (!name.equals(frame_set.getName())) {
		log.warn("Loading into FrameSet " +frame_set.getName()+
			 " not FrameSet " +name+ "!");
	    }
	    return;
	}

	String inheritance = attrs.getValue("frame-inheritance");
	if (!inheritance.equals("single")) {
	    throw new RuntimeException("Only single-inheritance FrameSets are supported!");
	}

	String relation_name = attrs.getValue("frame-inheritance-relation");
	String parent_proto = attrs.getValue("parent-prototype");
	String parent_slot = attrs.getValue("parent-slot");
	String parent_value = attrs.getValue("parent-value");
	String child_proto = attrs.getValue("child-prototype");
	String child_slot = attrs.getValue("child-slot");
	String child_value = attrs.getValue("child-value");

	frame_set = new SingleInheritanceFrameSet(pkg_prefix,
						  sb, bbs,
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
	proto_spec = new FrameSpec(name, parent);
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
	    String path = attrs.getValue("path");
	    if (path != null) {
		Path vp = (Path) path_specs.get(path);
		proto_spec.putPath(slot, vp);
	    } else {
		String value = attrs.getValue("value");
		if (value == null) {
		    proto_spec.addRequiredSlot(slot);
		} else {
		    proto_spec.put(slot, value);
		}
	    }
	} else {
	    // log
	}
    }

    private void endFrameset()
    {
	// no-op
    }

}
