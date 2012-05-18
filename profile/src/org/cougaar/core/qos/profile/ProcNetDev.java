package org.cougaar.core.qos.profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.service.LoggingService;

/**
 * This component profiles system network activity from
 * "/proc/net/dev".
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class ProcNetDev extends ProfilerBase {
  //"cat /proc/net/dev", example output:
  //Inter-|   Receive                                                |  Transmit
  // face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
  //    lo:768602893 9086982    0    0    0     0          0         0 768602893 9086982    0    0    0     0       0          0
  //  eth0:572864334 2350534    0    0    0     0          0         0 93901494  622384    0    0    0 47833       0          0
  //cipsec0:       0  527870    0    0    0     0          0         0        0   96869    0    0    0     0       0          0
  private static final String[] FIELDS = new String[] {
    "recv_bytes",
    "recv_packets",
    "recv_errs",
    "recv_drop",
    "recv_fifo",
    "recv_frame",
    "recv_compr",
    "recv_mcast",
    "send_bytes",
    "send_packets",
    "send_errs",
    "send_drop",
    "send_fifo",
    "send_colls",
    "send_carrier",
    "send_compr",
  };
  private static final String HEADER = toHeader(FIELDS);
  private static final String ALIGN;
  static {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < FIELDS.length; i++) {
      buf.append("0, ");
    }
    ALIGN = buf.toString();
  }
  private Map logs = new HashMap();
  @Override
public void run() {
    logNet();
  }
  private void logNet() {
    StringBuffer buf = new StringBuffer();
    try {
      BufferedReader in =
        new BufferedReader(
            new FileReader("/proc/net/dev"));
      String h1 = in.readLine();
      String h2 = in.readLine();
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        line = line.trim();
        int sep = line.indexOf(':');
        String key = line.substring(0, sep);
        String value = line.substring(sep+1);
        record(key, value);
      }
    } catch (Exception e) {
    }
  }
  private void record(String key, String value) {
    StringBuffer buf = new StringBuffer();
    String[] sa = value.trim().split("\\s+");
    for (int i = 0; i < sa.length; i++) {
      for (int j = 8 - sa[i].length(); j > 0; j--) {
        buf.append(" ");
      }
      buf.append(sa[i]).append(", ");
    }
    getLog(key).shout(buf.toString());
  }
  private LoggingService getLog(String key) {
    LoggingService log;
    synchronized (logs) {
      log = (LoggingService) logs.get(key);
      if (log == null) {
        log = sb.getService(
           "org.cougaar.core.qos.profile.proc.net.dev."+key,
           LoggingService.class, null);
        logs.put(key, log);
        if (header) {
          log.shout(HEADER);
        }
        if (align) {
          for (int i = 0, n = getRunCount(); i < n; i++) {
            log.shout(ALIGN);
          }
        }
      }
    }
    return log;
  }
}
