/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

public class LinuxMemory extends SysStatHandler {
    private static final String FreeLineKey = "MemFree";
    private static final String TotalLineKey = "MemTotal";

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

    public void getData(Map<String, DataValue> map) {
        double free = -1;
        double total = -1;
        FileReader fr;
        try {
            fr = new FileReader("/proc/meminfo");
        } catch (java.io.FileNotFoundException fnf_ex) {
            return;
        }

        BufferedReader br = new BufferedReader(fr);
        while (true) {
            try {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(FreeLineKey)) {
                    free = parse_one(line);
                    DataValue free_dv = new DataValue(free, SECOND_MEAS_CREDIBILITY, "KB", PROVENANCE);
                    map.put(free_key, free_dv);
                } else if (line.startsWith(TotalLineKey)) {
                    total = parse_one(line);
                    DataValue total_dv = new DataValue(total, SECOND_MEAS_CREDIBILITY, "KB", PROVENANCE);
                    map.put(total_key, total_dv);
                }
            } catch (java.io.IOException io_ex) {
                break;
            }
        }
        try {
            fr.close();
        } catch (IOException e) {
            //  don't care
        }
        if (free >= 0 && total > 0) {
            double util = (total - free) / total;
            DataValue util_dv = new DataValue(util, SECOND_MEAS_CREDIBILITY, "", PROVENANCE);
            map.put(util_key, util_dv);
        }
    }
}
