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
 * This component profiles persistence activity for each
 * agent and the aggegate node "sum".
 * <p>
 * Persistence must be enabled with<br>
 * &nbsp;&nbsp;-Dorg.cougaar.core.persistence.enable=true<br>
 * otherwise this profiler does nothing.
 * <p>
 * Example output:<pre> 
 *   pdir_AgentA - #sumActive, mostRecent, sumAll, timestamp,
 *     time_delta, max, first, last, numFiles,
 *   pdir_AgentA - 11461496, 2809731, 11461531, 1103647670000,
 *     7143025, 3286631, 0, 5, 8, 
 *   psum_NodeA - #sumActive, sumAll
 *   psum_NodeA - 63742420, 63742665
 * <pre> 
 *
 * @see ProfilerCoordinator required coordinator component
 * @see org.cougaar.core.qos.metrics.PersistenceAdapterPlugin
 *   required per-agent component
 */ 
public class PersistSize extends ProfilerBase {
  private static final DecimalFormat INT_FORMAT =
    new DecimalFormat("########0");
  private static final DecimalFormat LONG_FORMAT =
    new DecimalFormat("################0");
  private static final DecimalFormat DELTA_FORMAT = new DecimalFormat("_00000");
  private boolean enabled;
  private MessageAddress nodeId;
  private AgentContainer ac;
  private String baseDir;
  private final Map logs = new HashMap();

  public void load() {
    super.load();

    enabled = 
      "true".equals(
          System.getProperty(
            "org.cougaar.core.persistence.enable"));
    if (!enabled) {
      return;
    }

    NodeIdentificationService nis = (NodeIdentificationService)
      sb.getService(this, NodeIdentificationService.class, null);
    nodeId = nis.getMessageAddress();
    sb.releaseService(this, NodeIdentificationService.class, nis);

    NodeControlService ncs = (NodeControlService)
      sb.getService(this, NodeControlService.class, null);
    ac = ncs.getRootContainer();
    sb.releaseService(this, NodeControlService.class, ncs);

    String installPath = System.getProperty("org.cougaar.install.path", "/tmp");
    String workspaceDir=
      System.getProperty("org.cougaar.workspace", installPath + "/workspace");
    baseDir = workspaceDir+"/P";
  }
  public void run() {
    if (enabled) {
      logP();
    }
  }
  private void logP() {
    long sumActive = 0;
    long sumAll = 0;
    Set agents = ac.getAgentAddresses();
    for (Iterator iter = agents.iterator();
        iter.hasNext();
        ) {
      String agent = ((MessageAddress) iter.next()).getAddress();
      PersistenceData p = readP(agent);
      if (p == null) {
        continue;
      }
      sumActive += p.sumActive;
      sumAll += p.sumAll;
      getLog("pdir_"+agent).shout(p.toString());
    }
    getLog("psum_"+nodeId).shout(
        formatLong(sumActive)+", "+
        formatLong(sumAll));
  }
  private String formatLong(long l) {
    return LONG_FORMAT.format(l);
  }
  private PersistenceData readP(String agent) {
    String dir = baseDir+"/"+agent;
    File f = new File(dir);
    if (!f.isDirectory()) {
      return null;
    }
    PersistenceData p = new PersistenceData();
    // read sequence numbers
    File sequenceFile = new File(dir, "sequence");
    if (sequenceFile.exists()) {
      try {
        DataInputStream sequenceStream =
          new DataInputStream(
              new BufferedInputStream(
                new FileInputStream(sequenceFile)));
        p.first = sequenceStream.readInt();
        p.last = sequenceStream.readInt();
        p.timestamp = sequenceFile.lastModified();
        sequenceStream.close();
        // most recent delta
        p.mostRecent = getDeltaFile(dir, p.last-1).length();
        // total for rehydrate
        for (int i = p.first; i < p.last; i++) {
          p.sumActive += getDeltaFile(dir, i).length();
        } 
      } catch (Exception e) {
      }
    }
    // sum disk usage (includes ancient deltas)
    File[] fa = f.listFiles();
    p.numFiles = fa.length;
    for (int i = 0; i < fa.length; i++) {
      long len = fa[i].length();
      if (len > p.max) {
        p.max = len;
      }
      p.sumAll += len;
    }
    return p;
  }
  private LoggingService getLog(String key) {
    key = key.replace('.', '_');
    LoggingService log;
    synchronized (logs) {
      log = (LoggingService) logs.get(key);
      if (log == null) {
        log = (LoggingService)
          sb.getService(
              "org.cougaar.core.qos.profile.persistence."+key,
              LoggingService.class,
              null);
        logs.put(key, log);
        log.shout(getHeader(key));
        if (align) {
          String s = getAlign(key);
          for (int i = 0, n = getRunCount(); i < n; i++) {
            log.shout(s);
          }
        }
      }
    }
    return log;
  }
  private String getHeader(String key) {
    return 
      (key.startsWith("psum") ?
       "#"+
       "sumActive, "+
       "sumAll" :
       PersistenceData.HEADER);
  }
  private String getAlign(String key) {
    return 
      (key.startsWith("psum") ?
       "0, 0" :
       PersistenceData.ALIGN);
  }
  private File getDeltaFile(String dir, int num) {
    return new File(dir, "delta"+DELTA_FORMAT.format(num));
  }

  private static final class PersistenceData {
    public static final String HEADER =
        "#"+
        "sumActive, "+
        "mostRecent, "+
        "sumAll, "+
        "timestamp, "+
        "time_delta, "+
        "max, "+
        "first, "+
        "last, "+
        "numFiles, ";
    public static final String ALIGN =
        "0, "+
        "0, "+
        "0, "+
        "0, "+ // timestamp?
        "0, "+
        "0, "+
        "0, "+
        "0, "+
        "0, ";
    public long sumActive;
    public long mostRecent;
    public long sumAll;
    public long timestamp;
    public long max;
    public int first;
    public int last;
    public int numFiles;
    public String toString() {
      long now = System.currentTimeMillis();
      // 0, 0, 8588, 1103326510000, 14395, 8553, 0, 1, 0,
      return 
        formatLong(sumActive)+", "+
        formatLong(mostRecent)+", "+
        formatLong(sumAll)+", "+
        formatLong(timestamp)+", "+
        formatLong(timestamp > 0 ? now - timestamp : 0)+", "+
        formatLong(max)+", "+
        formatInt(first)+", "+
        formatInt(last)+", "+
        formatInt(numFiles)+", ";
    }
    private String formatInt(int i) {
      return INT_FORMAT.format(i);
    }
    private String formatLong(long l) {
      return LONG_FORMAT.format(l);
    }
  }
}
