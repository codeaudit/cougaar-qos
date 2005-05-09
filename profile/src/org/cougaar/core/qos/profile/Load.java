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
 * This component profiles the {@link MetricsService}'s load
 * metrics for the cpu, messaging, and persistence load of the
 * node aggegate, each agent, and core services (mts &amp; metrics).
 * <p>
 * Note that these are the metrics service's thread-based CPU
 * metrics, as opposed to the operating system's {@link ProcLoadAvg}
 * "loadavg" metrics.
 * <p>
 * This is the same output as the "/metrics/agent/load" servlet
 * generated by {@link org.cougaar.core.qos.metrics.AgentLoadServlet}.
 * <p>
 * Example output:<pre>
 *   load_node_NodeA - #cpu_load, cpu_load_jips, 
 *     recv_message_count, sent_message_count,
 *     recv_message_bytes, sent_message_bytes,
 *     persist_size_in_bytes,
 *   load_node_NodeA - 0.88, 1270.32,
 *     146.00, 145.90,
 *     132248.03, 186308.48,
 *     10518528.00,
 *   load_agent_AgentA - 0.31, 183.71,
 *     145.15, 145.05,
 *     132090.91, 186105.49,
 *     0.00,
 *   load_agent_NodeA - 0.01, 19.82,
 *     0.80, 0.80,
 *     117.49, 147.17,
 *     0.00,
 *   load_service_MTS - #cpu_load, cpu_load_jips,
 *   load_service_MTS - 0.49, 533.80,
 *   load_service_Metrics - 0.08, 99.95,
 *   load_service_NodeRoot - 0.00, 1.54,
 * </pre>
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class Load extends ProfilerBase {
  private static final DecimalFormat FORMAT_DECIMAL = new DecimalFormat("#0.00");
  private static final String[] FIELDS = new String[] {
    "cpu_load",
    "cpu_load_jips",
    "recv_message_count",
    "sent_message_count",
    "recv_message_bytes",
    "sent_message_bytes",
    "persist_size_in_bytes",
  };
  private static final String HEADER = toHeader(FIELDS);
  private static final String[] KEYS = new String[] {
    Constants.CPU_LOAD_AVG,
    Constants.CPU_LOAD_MJIPS,
    Constants.MSG_IN,
    Constants.MSG_OUT,
    Constants.BYTES_IN,
    Constants.BYTES_OUT,
  };

  private static final String[] SERVICE_FIELDS = new String[] {
    "cpu_load",
    "cpu_load_jips",
  };
  private static final String SERVICE_HEADER =
    toHeader(SERVICE_FIELDS);
  private static final String[] SERVICE_KEYS = new String[] {
    Constants.CPU_LOAD_AVG,
    Constants.CPU_LOAD_MJIPS,
  };

  // dummy logLoad keys
  private static final Object LOAD_NODE = new Object();
  private static final Object LOAD_MTS = "MTS";
  private static final Object LOAD_METRICS = "Metrics";
  private static final Object LOAD_NODE_ROOT = "NodeRoot";

  private final Map logs = new HashMap();

  private MessageAddress localNode;
  private MetricsService ms;
  public AgentStatusService as;

  public void load() {
    super.load();
    localNode = findLocalNode();
    ms = (MetricsService)
      sb.getService(this, MetricsService.class, null);
    findServiceLater(
        "as",
        "org.cougaar.core.mts.AgentStatusService");
  }

  public void run() {
    logLoads();
  }
  private void logLoads() {
    logLoad(LOAD_NODE);
    for (Iterator iter = getLocalAgents().iterator();
        iter.hasNext();
        ) {
      MessageAddress addr = (MessageAddress) iter.next();
      logLoad(addr);
    }
    logLoad(LOAD_MTS);
    logLoad(LOAD_METRICS);
    logLoad(LOAD_NODE_ROOT);
  }
  private void logLoad(Object o) {
    String s = getLoad(o);
    getLog(o).shout(s);
  }

  private boolean isService(Object o) {
    return
      (o == LOAD_MTS ||
       o == LOAD_METRICS ||
       o == LOAD_NODE_ROOT);
  }
  private boolean isNode(Object o) {
    return o == LOAD_NODE;
  }

  private String getLoad(Object o) {

    if (isService(o)) {
      String path = "Service("+o+")"+Constants.PATH_SEPR;
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < SERVICE_KEYS.length; i++) {
        String key =
          path + 
          SERVICE_KEYS[i]+"("+Constants._10_SEC_AVG+")";
        String s = getMetric(key);
        buf.append(s).append(", ");
      }
      return buf.toString();
    }

    String path = 
      (isNode(o) ?
       "Node("+localNode+")" :
       "Agent("+o+")")+
      Constants.PATH_SEPR;

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i <= KEYS.length; i++) {
      String key =
        path +
        (i < KEYS.length ?
         KEYS[i]+"("+Constants._10_SEC_AVG+")" :
         (isNode(o) ? "VMSize" : Constants.PERSIST_SIZE_LAST));
      String s = getMetric(key);
      buf.append(s).append(", ");
    }
    return buf.toString();
  }

  private String getMetric(String key) {
    Metric metric = ms.getValue(key);
    String s = 
      (metric == null ?
       "-1" :
       FORMAT_DECIMAL.format(metric.doubleValue()));
    return s;
  }

  private LoggingService getLog(Object o) {
    LoggingService log;
    synchronized (logs) {
      log = (LoggingService) logs.get(o);
      if (log == null) {
        String key =
          "load_"+
          (isService(o) ? "service_"+o :
           isNode(o) ? "node_"+localNode :
           "agent_"+o);
        key = key.replace('.', '_');
        log = (LoggingService)
          sb.getService(
              "org.cougaar.core.qos.profile.agent_load."+key,
              LoggingService.class,
              null);
        logs.put(o, log);
        if (header) {
          log.shout(isService(o) ? SERVICE_HEADER : HEADER);
        }
        if (align) {
          int num_fields =
            (isService(o) ? SERVICE_FIELDS.length : FIELDS.length);
          for (int i = 0, n = getRunCount(); i < n; i++) {
            StringBuffer buf = new StringBuffer();
            for (int j = 0; j < num_fields; j++) {
              buf.append("0, ");
            }
            log.shout(buf.toString());
          }
        }
      }
    }
    return log;
  }

  private MessageAddress findLocalNode() {
    NodeIdentificationService nis = (NodeIdentificationService)
      sb.getService(this, NodeIdentificationService.class, null);
    if (nis == null) {
      return null;
    }
    MessageAddress ret = nis.getMessageAddress();
    sb.releaseService(
        this, NodeIdentificationService.class, nis);
    return ret;
  }

  private Set getLocalAgents() {
    return as.getLocalAgents();
  }
}