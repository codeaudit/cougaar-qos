/*
 * =====================================================================
 * (c) Copyright 2007 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cougaar.qos.qrs.DataValue;

public class XPLoadAverage extends SysStatHandler {
    private static final String command = 
	"typeperf \"\\Processor(_total)\\% Processor Time\" -sc 1";

    private String key;

    public void initialize(String host, int pid) {
        key = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR + "loadavg";
    }

    public void getData(Map<String, DataValue> map) {
        Logger logger = org.cougaar.qos.qrs.Logging.getLogger(XPLoadAverage.class);
        String line = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream stdOut = process.getInputStream();
            InputStreamReader rdr = new InputStreamReader(stdOut);
            BufferedReader bufferedStdOut = new BufferedReader(rdr);
            line = bufferedStdOut.readLine(); // handy blank line -- ignore
            line = bufferedStdOut.readLine(); //headers --- ignore
            line = bufferedStdOut.readLine();
            bufferedStdOut.close();
            process.destroy();
        } catch (java.io.IOException ioex) {
            logger.warn("Error running typeperf: " + ioex.getMessage());
            return;
        }

        String[] csv = line.split(",");
        if (csv.length == 2) {
            String percentUtil = csv[1];
            // remove the extra pair of surrounding quotes
            percentUtil = percentUtil.substring(1, percentUtil.length()-2);
            double loadavg = Double.parseDouble(percentUtil) / 50.0; // XXX: Phony load avg
            map.put(key, new DataValue(loadavg, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
        }
    }
}
