package org.cougaar.core.qos.frame.visualizer.util;


import java.util.*;
import java.io.*;
import java.net.URL;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public abstract class XMLParser extends DefaultHandler {

    public final static String EMPTY_STR_ARR[]      = new String[0];
    public final static Properties EMPTY_PROPERTIES = new Properties();

    private static final Object UNDEFINED = new Object();
    private static final String DRIVER_PROPERTY = "org.xml.sax.driver";
    private static final String DRIVER_DEFAULT ="org.apache.crimson.parser.XMLReaderImpl";

    static {
	String driver = System.getProperty(DRIVER_PROPERTY);
	if (driver == null)
	    System.setProperty(DRIVER_PROPERTY, DRIVER_DEFAULT);
    }


    protected XMLParser() {
    }

    public void parse(URL url) {
        try {
            XMLReader producer = XMLReaderFactory.createXMLReader();
            DefaultHandler consumer = this;
            producer.setContentHandler(consumer);
            producer.setErrorHandler(consumer);
            producer.parse(url.toString());
        } catch (Throwable ex) {
            ex.printStackTrace();
            return;
        }
    }

    public abstract void startElement(String uri, String local, String name, Attributes attrs);
    public abstract void endElement(String uri, String local, String name);


    // Not using this yet
    public void characters(char buf[], int offset, int length){
    }

}