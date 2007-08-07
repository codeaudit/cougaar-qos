/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

public class LinuxMemory extends SysStatHandler {
    private static final String FreeLineKey = "MemFree";
    private static final String TotalLineKey = "MemTotal";

    private FileReader fr;
    private BufferedReader br;

    private String free_key, total_key, util_key;

    public void initialize(String host, int pid) {
        free_key =
                "Host" + KEY_SEPR + host + KEY_SEPR + "Memory" + KEY_SEPR + "Physical" + KEY_SEPR
                        + "Free";
        total_key =
                "Host" + KEY_SEPR + host + KEY_SEPR + "Memory" + KEY_SEPR + "Physical" + KEY_SEPR
                        + "Total";
        util_key =
                "Host" + KEY_SEPR + host + KEY_SEPR + "Memory" + KEY_SEPR + "Physical" + KEY_SEPR
                        + "Utilization";

    }

    private void close() {
        try {
            br.close();
        } catch (java.io.IOException ioe_ex) {
        }
    }

    private double parse_one(String line) {
        int start = 8;
        while (!Character.isDigit(line.charAt(start))) {
            ++start;
        }
        int end = start;
        while (Character.isDigit(line.charAt(end))) {
            ++end;
        }
        String doub = line.substring(start, end);
        return Double.parseDouble(doub);
    }

    public void getData(Map map) {
        String line = null;
        double free = 0.0;
        double total = 0.0;
        DataValue free_dv = null;
        DataValue total_dv = null;
        fr = null;
        try {
            fr = new FileReader("/proc/meminfo");
        } catch (java.io.FileNotFoundException fnf_ex) {
            return;
        }

        br = new BufferedReader(fr);
        while (true) {
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(FreeLineKey)) {
                    free = parse_one(line);
                    free_dv = new DataValue(free, SECOND_MEAS_CREDIBILITY, "KB", PROVENANCE);
                } else if (line.startsWith(TotalLineKey)) {
                    total = parse_one(line);
                    total_dv = new DataValue(total, SECOND_MEAS_CREDIBILITY, "KB", PROVENANCE);

                }
            } catch (java.io.IOException io_ex) {
                close();
                break;
            }
        }

        close();

        if (free_dv != null) {
            map.put(free_key, free_dv);
        }
        if (total_dv != null) {
            map.put(total_key, total_dv);
        }
        if (free_dv != null && total_dv != null) {
            double util = (total - free) / total;
            DataValue util_dv = new DataValue(util, SECOND_MEAS_CREDIBILITY, "", PROVENANCE);
            map.put(util_key, util_dv);
        }
    }

}
