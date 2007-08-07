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

public class XPMemory extends SysStatHandler {
    private static final String command = 
	"typeperf \"\\Memory\\Available KBytes\" -sc 1";

    private String free_key;

    public void initialize(String host, int pid) {
	free_key =
            "Host" + KEY_SEPR + host + KEY_SEPR + "Memory" + KEY_SEPR + "Physical" + KEY_SEPR
                    + "Free";
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
            String connectionCount = csv[1];
            // remove the extra pair of surrounding quotes
            connectionCount = connectionCount.substring(1, connectionCount.length()-2);
            double count = Double.parseDouble(connectionCount); 
            map.put(free_key, new DataValue(count, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
        }
    }
}
