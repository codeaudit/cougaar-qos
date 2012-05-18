package org.cougaar.core.qos.profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.service.LoggingService;

/**
 * This component profiles system activity from "/proc/stat".
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class ProcStat extends ProfilerBase {
  //From periodic "cat /proc/stat", example output:
  //  cpu  1800570 485 1156647 295121622
  //  cpu0 939814 240 583264 147516343
  //  cpu1 860756 245 573383 147605278
  //  page 128169 13690722
  //  swap 1 0
  //  intr 778253440 763083074 ..
  //  disk_io: (8,0):(606661,3534,47026,603127,16947752) (8,1):(652067,12102,208134,639965,10433680)
  //  ctxt 35696933
  //  btime 1103025654
  //  processes 586574
  private /*static*/ final String[][] FIELDS = new String[][] {
    {"cpu",
      "user_100th_sec",
      "nice_100th_sec",
      "system_100th_sec",
      "idle_100th_sec",
    },
    {"page",
      "pages_in",
      "pages_out",
    },
    {"intr",
      "num_interrupts",
      "etc",
    },
    {"swap", 
      "pages_in",
      "pages_out",
    },
    {"disk_io",
      "id_major",
      "id_minor",
      "total_ops",
      "read_io_ops",
      "read_blocks",
      "write_io_ops",
      "write_blocks",
    },
    {"ctxt",
      "context_switches",
    },
    {"btime",
      "boot_time_since_1970_sec",
    },
    {"processes",
      "fork_counter",
    },
  };
  private /*static*/ final Map HEADERS = initHeaders();
  private Map initHeaders() /*static */{
    Map m = new HashMap();
    for (int i = 0; i < FIELDS.length; i++) {
      String[] sa = FIELDS[i];
      StringBuffer buf = new StringBuffer();
      buf.append("#");
      for (int j = 1; j < sa.length; j++) {
        buf.append(sa[j]).append(", ");
      }
      m.put(sa[0], buf.toString());
    }
    return m;
  }
  private Map logs = new HashMap();
  @Override
public void run() {
    logStat();
  }
  private void logStat() {
    // The "cpu" numbers are (from the "proc" man page):
    //  The  number  of  jiffies  (1/100ths of a second) that the
    //  system spent in user mode, user mode  with  low  priority
    //  (nice),  system  mode,  and  the idle task, respectively
    // "disc_io" fields are (from the "proc" man page):
    //  (major,minor):(noinfo, read_io_ops, blks_read,
    //                 write_io_ops, blks_written)
    // where "noinfo" is "read_io_ops + write_io_ops
    StringBuffer buf = new StringBuffer();
    try {
      BufferedReader in =
        new BufferedReader(
            new FileReader("/proc/stat"));
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        int sep = line.indexOf(' ');
        String key = line.substring(0, sep);
        if (key.equals("intr")) {
          continue;
        }
        String value = line.substring(sep+1);
        if (key.equals("disk_io:")) {
          key = "disk_io";
          if (value.indexOf(" (") < 0) {
            recordDiskIO(value);
          } else {
            String[] sa = value.trim().split(" \\(");
            for (int i = 0; i < sa.length; i++) {
              recordDiskIO(sa[i]);
            }
          }
          continue;
        }
        record(key, value);
      }
    } catch (Exception e) {
    }
  }
  private void recordDiskIO(String value) {
    //  (8,0):(606661,3534,47026,603127,16947752)
    String s = 
      value.replace('(', ' '
          ).replace(',', ' '
            ).replace(':', ' '
              ).replace(')',' '
                ).trim();
    String[] sa = s.split("\\s+");
    String key = "disk_io_"+sa[0]+"_"+sa[1];
    record(key, s);
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
    String s = buf.toString();
    log(key, s);
  }
  private void log(String key, String value) {
    LoggingService log;
    synchronized (logs) {
      log = (LoggingService) logs.get(key);
      if (log == null) {
        log = sb.getService(
           "org.cougaar.core.qos.profile.proc.stat."+key,
           LoggingService.class, null);
        logs.put(key, log);
        if (header) {
          log.shout(getHeader(key));
        }
        if (align) {
          // ALIGN as current value
          for (int i = 0, n = getRunCount(); i < n; i++) {
            log.shout(value);
          }
        }
      }
    }
    log.shout(value);
  }
  private String getHeader(String key) {
    String s = (String) HEADERS.get(key);
    if (s != null) {
      return s;
    }
    if (key.startsWith("cpu")) {
      return (String) HEADERS.get("cpu");
    }
    if (key.startsWith("disk_io")) {
      return (String) HEADERS.get("disk_io");
    }
    return "unknown";
  }
}
