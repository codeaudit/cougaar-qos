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
 * This component profiles the output of the "/usr/bin/vmstat"
 * command, which summarizes the system performance.
 *
 * @see ProfilerCoordinator required coordinator component
 */
public class VmStat extends ProfilerBase {
  // procs     memory                     swap      io         system    cpu
  // r  b  w   swpd   free   buff  cache  si  so    bi    bo   in    cs  us  sy  id
  // 0  0  0      0 374080 132504 1481500  0   0     0     5    3    12   1   0   4
  private static final String[] FIELDS = new String[] {
    "proc_runnable",
    "proc_blocked",
    "proc_waiting",
    "mem_swapped_kB",
    "mem_free_kB",
    "mem_buff_kB",
    "mem_cache_kB",
    "swapped_in_kB_per_sec",
    "swapped_out_kB_per_sec",
    "io_blocks_in_per_sec",
    "io_blocks_out_per_sec",
    "interrupts_per_sec",
    "context_switches_per_sec",
    "cpu_user",
    "cpu_system",
    "cpu_idle",
  };
  private static final String HEADER = toHeader(FIELDS);

  public void run() {
    log("org.cougaar.core.qos.profile.vmstat", HEADER, getVmStat());
  }
  private String getVmStat() {
    StringBuffer buf = new StringBuffer();
    try {
      Process proc =
        Runtime.getRuntime().exec(
            "/usr/bin/vmstat");
      BufferedReader br =
        new BufferedReader(
            new InputStreamReader(
              proc.getInputStream()));
      String h1 = br.readLine();
      String h2 = br.readLine();
      String data = br.readLine();
      br.close();
      String[] sa = data.trim().split("\\s+");
      for (int i = 0; i < sa.length; i++) {
        for (int j = 6 - sa[i].length(); j > 0; j--) {
          buf.append(" ");
        }
        buf.append(sa[i]).append(", ");
      }
    } catch (Exception e) {
    }
    return buf.toString();
  }
}
