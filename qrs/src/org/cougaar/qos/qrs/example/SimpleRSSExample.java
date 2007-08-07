package org.cougaar.qos.qrs.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.qrs.BoundDataFormula;
import org.cougaar.qos.qrs.CorbaUtils;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;
import org.cougaar.qos.qrs.NullFormulaException;
import org.cougaar.qos.qrs.PathParser;
import org.cougaar.qos.qrs.RSS;

public class SimpleRSSExample {

    public DataValue blockingQuery(String rssQueryString) {
        ResourceNode[] path = null;
        try {
            path = PathParser.parsePath(rssQueryString);
        } catch (ResourceDescriptionParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return new DataValue();
        }
        BoundDataFormula bdf = null;
        try {
            bdf = new BoundDataFormula(path, false, null);
        } catch (NullFormulaException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return new DataValue();
        }
        DataValue value = (DataValue) bdf.getCurrentValue();
        return value;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        SimpleRSSExample impl = new SimpleRSSExample();
        String logging_props_file = null;
        String config = null;
        boolean gui = false;

        int i = 0;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.equals("-logging.props")) {
                logging_props_file = args[i++];
            } else if (arg.equals("-gui")) {
                gui = true;
            } else if (arg.equals("-rss.config")) {
                config = args[i++];
            } else {
                System.out.println("Do not understand switch " + arg);
            }
        }

        if (logging_props_file != null) {
            PropertyConfigurator.configure(logging_props_file);
        }

        Logger logger = Logging.getLogger(CorbaUtils.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Arg Switches " + " -logging.props=" + logging_props_file + " -gui=" + gui
                    + " -rss.config=" + config);
        }

        // make RSS, using conf
        Properties props = new Properties();
        if (gui && impl != null) {
            String gui_title = "RSS on " + CorbaUtils.hostname();
            props.setProperty(RSS.getGUI_PROPERTY(), gui_title);
        }

        if (config != null) {
            InputStream is = null;
            try {
                URL url = new URL(config);
                is = url.openStream();
            } catch (Exception ex) {
                // try it as a filename
                try {
                    is = new FileInputStream(config);
                } catch (Exception e) {
                    logger.error("Error opening " + config + ": " + ex);
                }
            }

            if (is != null) {
                try {
                    props.load(is);
                    is.close();
                } catch (java.io.IOException ex) {
                    logger.error("Error loading RSS properties from " + config + ": " + ex);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Properties=" + props);
        }

        RSS.makeInstance(props);

        CorbaUtils.registerContexts();

        System.out.println("RSS ready");

        String queryString = new String("Host(" + CorbaUtils.hostname() + "):Jips");
        System.out.println("RSS Query String" + queryString);
        DataValue bbnValueObj = impl.blockingQuery(queryString);
        System.out.println(queryString + "=" + bbnValueObj);
        bbnValueObj = impl.blockingQuery(queryString);
        System.out.println(queryString + "=" + bbnValueObj);
        bbnValueObj = impl.blockingQuery(queryString);
        System.out.println(queryString + "=" + bbnValueObj);
        bbnValueObj = impl.blockingQuery(queryString);
        System.out.println(queryString + "=" + bbnValueObj);
    }
}
