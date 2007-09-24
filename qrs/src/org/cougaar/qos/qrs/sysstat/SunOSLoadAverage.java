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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;

public class SunOSLoadAverage extends SysStatHandler {
    private static final String command = "/usr/bin/uptime";
    private static final Pattern pattern = Pattern.compile(": \\d*\\.\\d\\d\\D");
    
    // private static final DataValue ClockSpeed =
    // new DataValue(Hardware.getClockSpeed(), SECOND_MEAS_CREDIBILITY);

    private String key;

    public void initialize(String host, int pid) {
        key = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR + "loadavg";
        // clock_key = "Host" + KEY_SEPR + host +
        // KEY_SEPR + "CPU" + KEY_SEPR + "clockspeed";
    }
    
    public void getData(Map<String, DataValue> map) {
        // Use the OperatingSystemMXBean if possible.
        Double load = getLoadAvgFromOS();
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
