package org.cougaar.core.qos.profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This component profiles the JVM's process size from
 * "/proc/self/status" (VmSize &amp; VmRSS).
 * <p>
 * Example output:<pre>
 *   pid_status - #VmSize_kB, VmRSS_kB,
 *   pid_status - 267184, 43204
 * </pre>
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class ProcStatus extends ProfilerBase {
  private static final String[] FIELDS = new String[] {
    "VmSize_kB",
    "VmRSS_kB",
  };
  private static final String HEADER = toHeader(FIELDS);
  @Override
public void run() {
    log("org.cougaar.core.qos.profile.proc.pid_status",
        HEADER, getProc());
  }
  private String getProc() {
    StringBuffer buf = new StringBuffer();
    try {
      BufferedReader in =
        new BufferedReader(
            new FileReader("/proc/self/status"));
      String vmSize = "-1";
      String vmRSS = vmSize;
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        String sp =
          "^\\s*"+
          "(VmSize|VmRSS):"+
          "\\s*"+
          "(\\S+)"+
          "\\s*"+
          "kB"+
          "\\s*$";
        Pattern p = Pattern.compile(sp);
        Matcher m = p.matcher(line);
        if (!m.matches()) {
          continue;
        }
        String name = m.group(1);
        String value = m.group(2);
        if ("VmSize".equals(name)) {
          vmSize = value;
        } else if ("VmRSS".equals(name)) {
          vmRSS = value;
        }
      }
      in.close();
      buf.append(vmSize+", "+vmRSS);
    } catch (Exception e) {
    }
    return buf.toString();
  }
}
