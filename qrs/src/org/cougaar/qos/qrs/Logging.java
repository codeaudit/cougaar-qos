// $Id: Logging.java,v 1.4 2007-09-24 21:34:56 jzinky Exp $
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
