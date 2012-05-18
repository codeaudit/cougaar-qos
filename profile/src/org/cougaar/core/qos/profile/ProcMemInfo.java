package org.cougaar.core.qos.profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This component profiles system memory usage from "/proc/meminfo".
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class ProcMemInfo extends ProfilerBase {
  //            total:    used:    free:  shared: buffers:  cached:
  // Mem:  2114187264 1731149824 383037440     0 135684096 1517056000
  // Swap: 2146787328         0 2146787328
  private static final String[] FIELDS = new String[] {
    "mem_total",
    "mem_used",
    "mem_free",
    "mem_shared",
    "mem_buff",
    "mem_cached",
    "swap_total",
    "swap_used",
    "swap_free",
  };
  private static final String HEADER = toHeader(FIELDS);

  @Override
public void run() {
    log("org.cougaar.core.qos.profile.proc.meminfo",
        HEADER, getMemInfo());
  }
  private String getMemInfo() {
    StringBuffer buf = new StringBuffer();
    try {
      BufferedReader in =
        new BufferedReader(
            new FileReader("/proc/meminfo"));
      String mem = "";
      String swap = "";
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        String sp =
          "^\\s*"+
          "(Mem|Swap):"+
          "\\s*"+
          "(.*)"+
          "\\s*$";
        Pattern p = Pattern.compile(sp);
        Matcher m = p.matcher(line);
        if (!m.matches()) {
          continue;
        }
        String name = m.group(1);
        String value = m.group(2);
        if ("Mem".equals(name)) {
          mem = value;
        } else if ("Swap".equals(name)) {
          swap = value;
        }
      }
      in.close();
      String s = mem+" "+swap;
      String[] sa = s.trim().split("\\s+");
      for (int i = 0; i < sa.length; i++) {
        for (int j = 11 - sa[i].length(); j > 0; j--) {
          buf.append(" ");
        }
        buf.append(sa[i]).append(", ");
      }
    } catch (Exception e) {
    }
    return buf.toString();
  }
}
