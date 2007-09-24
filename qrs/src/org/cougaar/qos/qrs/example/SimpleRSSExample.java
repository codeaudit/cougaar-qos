/*
 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
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
        if (gui) {
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
