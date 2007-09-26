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

package org.cougaar.qos.qrs.sysstat;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Map;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;
import org.cougaar.util.log.Logger;

abstract public class SysStatHandler implements Constants {
    static final String PROVENANCE = "ProcessStats";
    
    // As of 1.5, the various MXBeans might help with system status.
    // For now we only use the OS bean, and only for load-average.
    private static final OperatingSystemMXBean OSMXBean = ManagementFactory.getOperatingSystemMXBean();
    
    //  getSystemLoadAverage() is new in 1.6, so use reflection for now
    private static final Object[] NO_ARGS = {};
    private static Method getLoadAvg;
    static {
        try {
            Class<?>[] NO_ARG_PTYPES = {};
            getLoadAvg = OSMXBean.getClass().getMethod("getSystemLoadAverage", NO_ARG_PTYPES);
        } catch (Exception e) {
            // ignore
        }
    }
    
    abstract protected void initialize(String host, int pid);
    abstract protected void getData(Map<String, DataValue> map);


    protected double parseValue(String string) throws NumberFormatException {
        return Double.parseDouble(string);
    }

    public Double getLoadAvgFromOS() {
        if (getLoadAvg != null) {
            Logger logger = Logging.getLogger(getClass());
            logger.info("OperatingSystemMXBean#getSystemLoadAverage() is available");
            try {
                return (Double) getLoadAvg.invoke(OSMXBean, NO_ARGS);
            } catch (Exception e) {
                logger.error("OperatingSystemMXBean#getSystemLoadAverage() failed: " +e.getMessage());
            }
        }
        return null;
    }
    
    // TODO: Make more use of MXBeans!
    
    public static SysStatHandler getHandler(String kind, String host, int pid)
            throws NoSysStatHandler {
        SysStatHandler handler = null;
        String name = System.getProperty("os.name");
        if (kind.equals("Jips")) {
            handler = new Jips();
        } else if (kind.equals("CPUCount")) {
            handler = new CPUCount();
        } else if (name.equals("Windows NT")) {
            // NT handlers
            // None so far
        } else if (name.equals("Windows XP")) {
            if (kind.equals("LoadAverage")) {
                handler = new XPLoadAverage();
            } else if (kind.equals("Sockets")) {
                handler = new XPSockStat();
            } else if (kind.equals("Memory")) {
                handler = new XPMemory();
            }
        } else if (name.equals("Mac OS X")) {
            // MacOSX handlers
            if (kind.equals("LoadAverage")) {
                handler = new MacOSXLoadAverage();
            } else if (kind.equals("ProcessStats")) {
                handler = new ProcessStats();
            }
        } else if (name.equals("Linux")) {
            // Linux handlers
            if (kind.equals("CPU")) {
                handler = new LinuxCPUInfo();
            } else if (kind.equals("Memory")) {
                handler = new LinuxMemory();
            } else if (kind.equals("LoadAverage")) {
                handler = new LinuxLoadAverage();
            } else if (kind.equals("Sockets")) {
                handler = new LinuxSockStat();
            } else if (kind.equals("ProcessStats")) {
                handler = new ProcessStats();
            }
        } else if (name.equals("SunOS")) {
            // Solaris handlers
            if (kind.equals("LoadAverage")) {
                handler = new SunOSLoadAverage();
            } else if (kind.equals("ProcessStats")) {
                handler = new ProcessStats();
            }
        }

        if (handler == null) {
            throw new NoSysStatHandler("No handler for " + kind + " on platform " + name);
        }

        handler.initialize(host, pid);
        return handler;
    }
    
    public static class NoSysStatHandler extends Exception {
        NoSysStatHandler(String message) {
            super(message);
        }
    }
}
