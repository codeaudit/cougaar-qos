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
