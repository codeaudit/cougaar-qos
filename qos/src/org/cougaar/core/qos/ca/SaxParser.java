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

package org.cougaar.core.qos.ca;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;


public class SaxParser
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
    private static class FrameSpec
    {
	String kind;
	Properties props;

	FrameSpec(String kind)
	{
	    this.kind = kind;
	    props = new Properties();
	}

	Frame makePrototype(FrameSet frameSet)
	{
	    return frameSet.makePrototype(kind, props);
	}

	Frame makeFrame(FrameSet frameSet)
	{
	    return frameSet.makeFrame(kind, props);
	}


	void put(String attr, String value)
	{
	    props.setProperty(attr, value);
	}
    }

    private static class FrameSetSpec
    {
	String relation_name;
	String parent_kind;
	String parent_slot;
	String parent_value;
	String child_kind;
	String child_slot;
	String child_value;

	FrameSetSpec(String relation_name)
	{
	    this.relation_name = relation_name;
	}

	FrameSet makeFrameSet(ServiceBroker sb,
			      BlackboardService bbs)
	{
	    return new SingleInheritanceFrameSet(sb, bbs,
						 relation_name,
						 parent_kind,
						 parent_slot,
						 parent_value,
						 child_kind,
						 child_slot,
						 child_value
						 );
	}

    }

    private FrameSet frame_set;
    private FrameSetSpec frame_set_spec;
    private FrameSpec frame_spec;

    private ServiceBroker sb;
    private BlackboardService bbs;
    private LoggingService log;

    public SaxParser(ServiceBroker sb, BlackboardService bbs)
    {
	this.sb = sb;
	this.bbs = bbs;
	this.log = (LoggingService)
	    sb.getService(this, LoggingService.class, null);
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
	} else if (name.equals("parent-relation")) {
	    startParentRelation(attrs);
	} else if (name.equals("parent")) {
	    parent(attrs);
	} else if (name.equals("child")) {
	    child(attrs);
	} else if (name.equals("prototypes")) {
	    // no-op
	} else if (name.equals("prototype")) {
	    startPrototype(attrs);
	} else if (name.equals("attribute")) {
	    attribute(attrs);
	} else if (name.equals("frames")) {
	    // no-op
	} else if (name.equals("frame")) {
	    startFrame(attrs);
	} 
    }

    public void endElement(String uri, String local, String name)
    {
	if (name.equals("frameset")) {
	    endFrameset();
	} else if (name.equals("parent-relation")) {
	    endParentRelation();
	} else if (name.equals("prototype")) {
	    endPrototype();
	} else if (name.equals("frame")) {
	    endFrame();
	} 
    }

    // Not using this yet
    public void characters(char buf[], int offset, int length)
    {
    }




    private void startFrameset(Attributes attrs)
    {
    }

    private void startParentRelation(Attributes attrs)
    {
	frame_set_spec = new FrameSetSpec(attrs.getValue("name"));
    }

    private void parent(Attributes attrs)
    {
	frame_set_spec.parent_kind = attrs.getValue("kind");
	frame_set_spec.parent_slot = attrs.getValue("slot");
	frame_set_spec.parent_value = attrs.getValue("value");
    }

    private void child(Attributes attrs)
    {
	frame_set_spec.child_kind = attrs.getValue("kind");
	frame_set_spec.child_slot = attrs.getValue("slot");
	frame_set_spec.child_value = attrs.getValue("value");
    }

    private void endParentRelation()
    {
	frame_set = frame_set_spec.makeFrameSet(sb, bbs);
	frame_set_spec = null;
    }


    private void startPrototype(Attributes attrs)
    {
	String kind = attrs.getValue("name");
	frame_spec = new FrameSpec(kind);
    }

    private void endPrototype()
    {
	frame_spec.makePrototype(frame_set);
	frame_spec = null;
    }

    private void startFrame(Attributes attrs)
    {
	String kind = attrs.getValue("prototype");
	frame_spec = new FrameSpec(kind);
    }

    private void endFrame()
    {
	frame_spec.makeFrame(frame_set);
	frame_spec = null;
    }

    private void attribute(Attributes attrs)
    {
	String attr = attrs.getValue("name");
	String value = attrs.getValue("value");
	frame_spec.put(attr, value);
    }

    private void endFrameset()
    {
	// no-op
    }

}
