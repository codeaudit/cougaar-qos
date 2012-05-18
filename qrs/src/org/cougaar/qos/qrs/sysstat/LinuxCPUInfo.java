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
import java.io.FileReader;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

public class LinuxCPUInfo extends SysStatHandler {
    private static String CacheLineKey, ClockLineKey, BogomipsLineKey;

    static {
        BogomipsLineKey = "bogomips";
        String arch = System.getProperty("os.arch");
        if (arch.equals("ppc")) {
            CacheLineKey = "L2 cache";
            ClockLineKey = "clock";
        } else if (arch.equals("i386") || arch.equals("amd64")) {
            CacheLineKey = "cache size";
            ClockLineKey = "cpu MHz";
        }

    }

    private FileReader fr;
    private BufferedReader br;
    private String bogomips_key, cache_key, clock_key;

    @Override
   public void initialize(String host, int pid) {
        String prefix = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR;
        bogomips_key = prefix + "bogomips";
        cache_key = prefix + "cache";
        clock_key = prefix + "MHz";
    }

    private void close() {
        try {
            br.close();
        } catch (java.io.IOException ioe_ex) {
        }
    }

    @Override
   public void getData(Map<String, DataValue> map) {
        fr = null;
        try {
            fr = new FileReader("/proc/cpuinfo");
        } catch (java.io.FileNotFoundException fnf_ex) {
            return;
        }

        br = new BufferedReader(fr);
        String line = null;
        while (true) {
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(BogomipsLineKey)) {
                    map.put(bogomips_key, extractBogomips(line));
                } else if (line.startsWith(CacheLineKey)) {
                    map.put(cache_key, extractCache(line));
                } else if (line.startsWith(ClockLineKey)) {
                    map.put(clock_key, extractClock(line));
                }
            } catch (java.io.IOException io_ex) {
                close();
                return;
            }
        }
        close();
    }

    private DataValue extractBogomips(String line) {
        int index = line.indexOf(':');
        if (index == -1) {
            return DataValue.NO_VALUE;
        }
        String doub = line.substring(index + 2);
        try {
            return new DataValue(parseValue(doub), SECOND_MEAS_CREDIBILITY, "", PROVENANCE);
        } catch (NumberFormatException nf_ex) {
            return DataValue.NO_VALUE;
        }
    }

    private DataValue extractCache(String line) {
        int index = line.indexOf(':');
        if (index == -1) {
            return DataValue.NO_VALUE;
        }
        // String doub = line.substring(index+2);
        int start_index = index + 2;
        int end_index = start_index;
        try {
            while (Character.isDigit(line.charAt(end_index))) {
                ++end_index;
            }
            double cache = Double.parseDouble(line.substring(start_index, end_index));

            return new DataValue(cache, SECOND_MEAS_CREDIBILITY, "", PROVENANCE);
        } catch (NumberFormatException nf_ex) {
            return DataValue.NO_VALUE;
        }
    }

    private DataValue extractClock(String line) {
        int index = line.indexOf(':');
        if (index == -1) {
            return DataValue.NO_VALUE;
        }
        double clock = 0.0;
        String arch = System.getProperty("os.arch");
        try {
            if (arch.equals("ppc")) {
                // get rid of the MHz at the end...
                int start_index = index + 2;
                int end_index = line.indexOf("M");
                clock = Double.parseDouble(line.substring(start_index, end_index));
            } else {
                String doub = line.substring(index + 2);
                clock = parseValue(doub);
            }

            return new DataValue(clock, SECOND_MEAS_CREDIBILITY, "", PROVENANCE);
        } catch (NumberFormatException nf_ex) {
            return DataValue.NO_VALUE;
        }
    }

}
