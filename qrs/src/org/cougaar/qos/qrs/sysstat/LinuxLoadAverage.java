/*
 * =====================================================================
 * (c) Copyright 2004 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

public class LinuxLoadAverage extends SysStatHandler {
    private FileReader fr;
    private BufferedReader br;
    private String key;

    public void initialize(String host, int pid) {
        key = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR + "loadavg";
    }

    private void close() {
        try {
            br.close();
        } catch (java.io.IOException ioe_ex) {
        }
    }

    public void getData(Map<String, DataValue> map) {
        String line = null;
        fr = null;
        try {
            fr = new FileReader("/proc/loadavg");
        } catch (java.io.FileNotFoundException fnf_ex) {
            return;
        }

        br = new BufferedReader(fr);
        try {
            line = br.readLine();
        } catch (java.io.IOException io_ex) {
            return;
        } finally {
            close();
        }

        int index = line.indexOf(' ');
        String doub = null;
        if (index == -1) {
            doub = line;
        } else {
            doub = line.substring(0, index);
        }
        try {
            map.put(key, new DataValue(Double.parseDouble(doub),
                                       SECOND_MEAS_CREDIBILITY,
                                       "",
                                       PROVENANCE));
        } catch (NumberFormatException nf_ex) {
            ;
        }

    }

}
