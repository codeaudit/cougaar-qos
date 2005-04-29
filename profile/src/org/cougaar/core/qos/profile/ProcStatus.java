package org.cougaar.core.qos.profile;

import java.lang.reflect.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.component.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.node.*;
import org.cougaar.core.qos.metrics.*;
import org.cougaar.core.service.*;
import org.cougaar.core.service.wp.*;
import org.cougaar.core.thread.*;
import org.cougaar.core.wp.resolver.*;
import org.cougaar.util.*;

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
