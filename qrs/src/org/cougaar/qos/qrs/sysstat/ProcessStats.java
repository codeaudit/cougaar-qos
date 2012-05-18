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
import java.util.StringTokenizer;

import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.Logging;
import org.cougaar.util.log.Logger;

public class ProcessStats extends SysStatHandler {
    private static final String CommandPrefix = "/bin/ps -o pcpu,pmem,rss,time -p ";

    private String cpu_key, mem_key, rsz_key, cputime_key;
    private String command;

    public static String makePCPUKey(String host, int pid) {
        return "Host" + KEY_SEPR + host + KEY_SEPR + "Process" + KEY_SEPR + pid + KEY_SEPR + "%CPU";
    }

    public static String makePMEMKey(String host, int pid) {
        return "Host" + KEY_SEPR + host + KEY_SEPR + "Process" + KEY_SEPR + pid + KEY_SEPR + "%MEM";
    }

    public static String makeRSZKey(String host, int pid) {
        return "Host" + KEY_SEPR + host + KEY_SEPR + "Process" + KEY_SEPR + pid + KEY_SEPR + "RSZ";
    }

    public static String makeCPUTIMEKey(String host, int pid) {
        return "Host" + KEY_SEPR + host + KEY_SEPR + "Process" + KEY_SEPR + pid + KEY_SEPR
                + "CPUTIME";
    }

    @Override
   public void initialize(String host, int pid) {
        cpu_key = makePCPUKey(host, pid);
        mem_key = makePMEMKey(host, pid);
        rsz_key = makeRSZKey(host, pid);
        cputime_key = makeCPUTIMEKey(host, pid);
        command = CommandPrefix + pid;
    }

    // Units = hundredths of a second
    private static final int UNITS_PER_SEC = 100;
    private static final int UNITS_PER_MIN = UNITS_PER_SEC * 60;
    private static final int UNITS_PER_HOUR = UNITS_PER_MIN * 60;
    private static final int UNITS_PER_DAY = UNITS_PER_HOUR * 24;

    private int parseTime(String cput) {
        // [[days-]hour:]minutes:seconds[.hundredths-of-a-second]
        int core_start = 0;
        int core_end = cput.length();

        int dash = cput.indexOf('-');
        int result = 0;
        if (dash > 0) {
            String days = cput.substring(0, dash);
            result = Integer.parseInt(days) * UNITS_PER_DAY;
            core_start = dash + 1;
        }

        int dot = cput.indexOf('.');
        if (dot > 0) {
            String hsecs = cput.substring(dot + 1);
            result += Integer.parseInt(hsecs);
            core_end = dot;
        }

        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        String core = cput.substring(core_start, core_end);
        StringTokenizer tk = new StringTokenizer(core, ":");
        int tokens = tk.countTokens();
        switch (tokens) {
            case 1:
                seconds = Integer.parseInt(tk.nextToken());
                break;

            case 2:
                minutes = Integer.parseInt(tk.nextToken());
                seconds = Integer.parseInt(tk.nextToken());
                break;

            case 3:
                hours = Integer.parseInt(tk.nextToken());
                minutes = Integer.parseInt(tk.nextToken());
                seconds = Integer.parseInt(tk.nextToken());
                break;
        }

        result += hours * UNITS_PER_HOUR + minutes * UNITS_PER_MIN + seconds * UNITS_PER_SEC;

        return result;

    }

    @Override
   public void getData(Map<String, DataValue> map) {
        Logger logger = Logging.getLogger(ProcessStats.class);
        String line = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream stdOut = process.getInputStream();
            InputStreamReader rdr = new InputStreamReader(stdOut);
            BufferedReader bufferedStdOut = new BufferedReader(rdr);
            line = bufferedStdOut.readLine(); // ignore this one
            line = bufferedStdOut.readLine();
            bufferedStdOut.close();
            process.destroy();
        } catch (java.io.IOException ioex) {
            logger.warn("Error running ps: " + ioex.getMessage());
            return;
        }

        if (line != null) {
            StringTokenizer tk = new StringTokenizer(line, " ");
            String pcpu = tk.nextToken();
            String pmem = tk.nextToken();
            String rsz = tk.nextToken();
            String cput = tk.nextToken();
            double percent_cpu = Double.parseDouble(pcpu);
            double percent_mem = Double.parseDouble(pmem);
            int res_size = Integer.parseInt(rsz);
            int cpu_time = parseTime(cput);
            map.put(cpu_key, new DataValue(percent_cpu, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
            map.put(mem_key, new DataValue(percent_mem, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
            map.put(rsz_key, new DataValue(res_size, SECOND_MEAS_CREDIBILITY, "KB", PROVENANCE));
            map.put(cputime_key, new DataValue(cpu_time, SECOND_MEAS_CREDIBILITY, "KB", PROVENANCE));
        }
    }

}
