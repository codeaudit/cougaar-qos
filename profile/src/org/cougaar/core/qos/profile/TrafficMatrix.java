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
 * This component profiles the agent traffic matrix for all
 * local-to-local and local-to-remote agent messaging.
 * <p>
 * Example output:<pre> 
 *   tm_AgentA__to__AgentB - #count, bytes,
 *   tm_AgentA__to__AgentB - 4783.0, 6136339.0
 *   tm_NodeA__to__NodeB - 8.0, 8167.0
 *   tm_NodeA__to__NodeA - 38.0, 0.0
 * </pre>
 * <p>
 * Node traffic is usually naming service related.
 *
 * @see ProfilerCoordinator required coordinator component
 */
public class TrafficMatrix extends ProfilerBase {
  private static final String TMS_CLASS_NAME =
    "org.cougaar.core.qos.tmatrix.TrafficMatrixStatisticsService";
  private static final String[] FIELDS = new String[] {
    "count",
    "bytes",
  };
  private static final String HEADER = toHeader(FIELDS);
  private static final String ALIGN = "0, 0";
  private final Map logs = new HashMap();
  public Object tms;
  public void load() {
    super.load();
    findServiceLater(
        "tms",
        TMS_CLASS_NAME);
  }
  public void run() {
    logMatrix();
  }
  private void logMatrix() {
    try {
      if (tms == null) {
        throw new RuntimeException(
            "Unable to obtain "+TMS_CLASS_NAME+" service");
      }
      Class tmsCl = Class.forName(TMS_CLASS_NAME);
      Method snapM = tmsCl.getMethod("snapshotMatrix", null);
      Object tm = snapM.invoke(tms, null);

      Class tmCl = Class.forName(
          "org.cougaar.core.qos.tmatrix.TrafficMatrix");
      Method iterM = tmCl.getMethod("getIterator", null);
      Iterator itr = (Iterator) iterM.invoke(tm, null);

      Class tiCl = Class.forName(
          "org.cougaar.core.qos.tmatrix.TrafficMatrix$TrafficIterator");
      Method origM = tiCl.getMethod("getOrig", null);
      Method targetM = tiCl.getMethod("getTarget", null);

      Class trCl = Class.forName(
          "org.cougaar.core.qos.tmatrix.TrafficMatrix$TrafficRecord");
      Method msgCountM = trCl.getMethod("getMsgCount", null);
      Method byteCountM = trCl.getMethod("getByteCount", null);

      while (itr.hasNext()) {
        Object o = itr.next();
        Object orig = origM.invoke(itr, null);
        Object target = targetM.invoke(itr, null);
        Object msgCount = msgCountM.invoke(o, null);
        Object byteCount = byteCountM.invoke(o, null);
        getLog(orig, target).shout(
            msgCount+", "+
            byteCount);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private LoggingService getLog(Object orig, Object target) {
    LoggingService log;
    synchronized (logs) {
      Map m = (Map) logs.get(orig);
      if (m == null) {
        m = new HashMap();
        logs.put(orig, m);
      }
      log = (LoggingService) m.get(target);
      if (log == null) {
        String os = orig.toString().replace('.', '_');
        String ts = target.toString().replace('.', '_');
        String key = "tm_"+os+"__to__"+ts;
        log = (LoggingService)
          sb.getService(
              "org.cougaar.core.qos.profile.traffic_matrix."+key,
              LoggingService.class, null);
        m.put(target, log);
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
