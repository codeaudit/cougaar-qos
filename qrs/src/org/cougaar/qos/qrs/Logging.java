// $Id: Logging.java,v 1.3 2007-08-07 14:56:14 rshapiro Exp $
/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;

public class Logging {
    private static String dprefix = null;
    private static String eprefix = null;
    private static String junk_prefix = null;

    private static Map<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();
    private static Map<Class<?>, Logger> eventLoggers = new HashMap<Class<?>, Logger>();

    public static void configure(String debugPrefix, String eventPrefix, String junkPrefix) {
        dprefix = debugPrefix;
        eprefix = eventPrefix;
        junk_prefix = junkPrefix;
    }

    private static String hack_classname(String prefix, Class<?> klass) {
        String classname = klass.getName();
        if (junk_prefix != null && classname.startsWith(junk_prefix)) {
            classname = classname.substring(junk_prefix.length());
        }
        if (prefix != null) {
            return prefix + classname;
        } else {
            return classname;
        }
    }

    public static Logger getLogger(Class<?> klass) {
        Logger result;
        synchronized (loggers) {
            result = loggers.get(klass);
            if (result == null) {
                String tag = hack_classname(dprefix, klass);
                result = Logger.getLogger(tag);
                loggers.put(klass, result);
            }
        }
        return result;
    }

    public static Logger getEventLogger(Class<?> klass) {
        Logger result;
        synchronized (eventLoggers) {
            result = eventLoggers.get(klass);
            if (result == null) {
                String tag = hack_classname(eprefix, klass);
                result = Logger.getLogger(tag);
                eventLoggers.put(klass, result);
            }
        }
        return result;
    }

}
