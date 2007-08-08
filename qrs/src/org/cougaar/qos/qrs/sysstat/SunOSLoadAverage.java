/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import org.apache.log4j.Logger;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SunOSLoadAverage extends SysStatHandler {
    // Use the bean if getSystemLoadAverage() is available (java 1.6+)
    private static final OperatingSystemMXBean OSMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static Method getLoadAvg;
    private static final Object[] NO_ARGS = {};
    
    // Otherwise exec uptime and extract the load average string
    private static final String command = "/usr/bin/uptime";
    private static final Pattern pattern = Pattern.compile(": \\d*\\.\\d\\d\\D");
    
    static {
        try {
            Class<?>[] NO_ARG_PTYPES = {};
            getLoadAvg = OSMXBean.getClass().getMethod("getSystemLoadAverage", NO_ARG_PTYPES);
        } catch (Exception e) {
            // ignore
        }
    }

    // private static final DataValue ClockSpeed =
    // new DataValue(Hardware.getClockSpeed(), SECOND_MEAS_CREDIBILITY);

    private String key;

    public void initialize(String host, int pid) {
        key = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR + "loadavg";
        // clock_key = "Host" + KEY_SEPR + host +
        // KEY_SEPR + "CPU" + KEY_SEPR + "clockspeed";
    }
    
    /**
     * Use the OperatingSystemMXBean if possible.
     */
    protected Double getFromOS() {
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

    public void getData(Map<String, DataValue> map) {
        Double load = getFromOS();
        if (load != null) {
            map.put(key, new DataValue(load, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
            return;
        }
        
        // If we get here, either getSystemLoadAverage is unavailable, or the call failed.
        // Exec 'uptime' instead and parse the string.
        String line = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream stdOut = process.getInputStream();
            InputStreamReader rdr = new InputStreamReader(stdOut);
            BufferedReader bufferedStdOut = new BufferedReader(rdr);
            line = bufferedStdOut.readLine();
            bufferedStdOut.close();
            process.destroy();
        } catch (java.io.IOException ioex) {
            Logger logger = Logging.getLogger(getClass());
            logger.warn("Error running uptime: " + ioex.getMessage());
            return;
        }

        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            Logger logger = Logging.getLogger(getClass());
            logger.warn("Matcher failed on " + line);
            return;
        }

        String match = matcher.group();
        // ": xx.xx,"
        String la = match.substring(2, match.length() - 1);
        double loadavg = Double.parseDouble(la);
        map.put(key, new DataValue(loadavg, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));

        // put the "speed" too
        // map.put(clock_key, ClockSpeed);
    }

}
