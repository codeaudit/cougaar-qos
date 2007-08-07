/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.util.Map;

import org.cougaar.qos.qrs.Constants;
import org.cougaar.qos.qrs.DataValue;

abstract public class SysStatHandler implements Constants {
    abstract protected void initialize(String host, int pid);

    abstract protected void getData(Map<String, DataValue> map);

    static final String PROVENANCE = "ProcessStats";

    public static class NoSysStatHandler extends Exception {
        NoSysStatHandler(String message) {
            super(message);
        }
    }

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

    protected double parseValue(String string) throws NumberFormatException {
        return Double.parseDouble(string);
    }

}
