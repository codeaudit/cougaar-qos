package org.cougaar.core.qos.profile;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * This component profiles the CPU load from "/proc/loadavg".
 * <p>
 * Example output:<pre> 
 *  loadavg - #1_min_avg, 5_min_avg, 15_min_avg,
 *    running, total, pid,
 *  loadavg - 2.80,  2.81,  2.44,
 *    4,   221,  1551,
 * </pre>
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class ProcLoadAvg extends ProfilerBase {
  // 0.07 0.36 0.37 1/262 5191
  private static final String[] FIELDS = new String[] {
    "1_min_avg",
    "5_min_avg",
    "15_min_avg",
    "running",
    "total",
    "pid",
  };
  private static final String HEADER = toHeader(FIELDS);
  @Override
public void run() {
    log("org.cougaar.core.qos.profile.proc.loadavg",
        HEADER, getLoadAvg());
  }
  private String getLoadAvg() {
    //The first 3 fields are the CPU load averages from 1, 5, and
    //  15 minutes.  The 4th field is the number of running processes,
    //  and the 5th is the most recent new PID.
    StringBuffer buf = new StringBuffer();
    try {
      BufferedReader in =
        new BufferedReader(
            new FileReader("/proc/loadavg"));
      String line = in.readLine();
      line = line.replace('/', ' ');
      String[] sa = line.trim().split("\\s+");
      for (int i = 0; i < sa.length; i++) {
        for (int j = 5 - sa[i].length(); j > 0; j--) {
          buf.append(" ");
        }
        buf.append(sa[i]).append(", ");
      }
      in.close();
    } catch (Exception e) {
    }
    return buf.toString();
  }
}
