package org.cougaar.core.qos.frame;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;


import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.net.URL;


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
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;


/**
 * This class parses frame and protocol xml files and creates the
 * corresponding {@link PrototypeFrame}s and {@link DataFrame}s .  The
 * parsing is SAX.
 */
public class TestFrameSetParser
    extends DefaultHandler
{
    private static final String DRIVER_PROPERTY = "org.xml.sax.driver";
    private static final String DRIVER_DEFAULT ="org.apache.crimson.parser.XMLReaderImpl";

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

	FrameSpec(String kind, String parent)
	{
	    this.kind = kind;
	    this.parent = parent;
	    props = new Properties();
	}

	Frame makePrototype(FrameSet frameSet)
	{
	    PrototypeFrame frame = frameSet.makePrototype(kind, parent, props);
	    return frame;
	}

	Frame makeFrame(FrameSet frameSet)
	{
	    return frameSet.makeFrame(kind, props);
	}


	void put(String attr, Object value)
	{
	    props.put(attr, value);
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
    private FrameSpec frame_spec;
    private FrameSpec proto_spec;
    private PathSpec path_spec;
    private String current_slot;
    private HashMap path_specs;

   // private ServiceBroker sb;
    //private BlackboardService bbs;
     private transient Logger log = Logging.getLogger(getClass().getName());

    public TestFrameSetParser()
    {
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
	File xml_file = new File(xml_filename); //ConfigFinder.getInstance().locateFile(xml_filename);
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


	if (frame_set != null) return;

	String pkg_prefix = attrs.getValue("package");
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
      System.out.println("pkg_prefix="+pkg_prefix+"\ninheritance="+inheritance+"\nrelation_name="+relation_name+"\nparent_proto="+parent_proto+"\nparent_slot="+parent_slot);
        frame_set = new TestFrameSet(pkg_prefix,frame_set_name);
						  //relation_name,
						  //parent_proto,
						  //parent_slot,
						  //parent_value,
						  //child_proto,
						  //child_slot,
						  //child_value);
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
