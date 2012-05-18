package org.cougaar.core.qos.frame.visualizer.util;


import java.net.URL;
import java.util.Properties;

import org.cougaar.bootstrap.XMLReaderUtils;
import org.cougaar.util.log.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public abstract class XMLParser extends DefaultHandler {

    public final static String EMPTY_STR_ARR[]      = new String[0];
    public final static Properties EMPTY_PROPERTIES = new Properties();

    protected XMLParser() {
    }

    public void parse(URL url) {
        try {
            XMLReader producer = XMLReaderUtils.createXMLReader();
            DefaultHandler consumer = this;
            producer.setContentHandler(consumer);
            producer.setErrorHandler(consumer);
            producer.parse(url.toString());
        } catch (SAXParseException sax) {
            Logging.defaultLogger().error("Frame View File Parser Error+" +
                    " lineNumber=" +sax.getLineNumber()+
                    " columnNumber="+ sax.getColumnNumber() +
                    " publicId="+ sax.getPublicId() +
                    " systemId="+ sax.getSystemId() );
            //useless, but makes error visable
            sax.printStackTrace();
        }
        catch (Throwable ex) {
            Logging.defaultLogger().error("Frame View File Error " + url+ " " + ex.getMessage());
            ex.printStackTrace();
        }
        return;
    }

    @Override
   public abstract void startElement(String uri, String local, String name, Attributes attrs);
    @Override
   public abstract void endElement(String uri, String local, String name);


    // Not using this yet
    @Override
   public void characters(char buf[], int offset, int length){
    }

}