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
 * This component profiles the aggregate "messages per second"
 * throughput for all agents on the local node. 
 * <p>
 * Example output:<pre>
 *   throughput - #agent_send_count_per_second, total_send_count_per_second,
 *     agent_send_count, total_send_count,
 *   throughput - 160.89, 161.11,
 *     2913, 2929
 * </pre> 
 * <p> 
 * For more detailed message traffic profiling, see the {@link
 * NodeTraffic} profiler.
 * <p>
 * This profiler is useful in ping testing, where the first field will
 * show the total ping throughput in messages per second (assuming all
 * ping agents are non-node agents, to exclude naming service
 * traffic).
 *
 * @see ProfilerCoordinator required coordinator component
 */
public class Throughput extends ProfilerBase {
  private static final DecimalFormat FORMAT_DECIMAL = new DecimalFormat("#0.00");
  private static final String[] FIELDS = new String[] {
    "agent_send_count_per_second",
    "total_send_count_per_second",
    "agent_send_count",
    "total_send_count",
  };
  private static final String HEADER = toHeader(FIELDS);

  private MessageAddress localNode;
  public MessageStatisticsService mss;
  public AgentStatusService as;

  private long lastTime;
  private long lastAgent;
  private long lastTotal;

  public void load() {
    super.load();
    localNode = findLocalNode();
    findServiceLater(
        "mss",
        "org.cougaar.core.service.MessageStatisticsService");
    findServiceLater(
        "as",
        "org.cougaar.core.mts.AgentStatusService");
  }
  public void run() {
    log("org.cougaar.core.qos.profile.throughput",
        HEADER, getThroughput());
  }
  private String getThroughput() {
    long totalSendCount = getTotalSendCount();
    long nodeSendCount = getNodeSendCount();
    long agentSendCount = totalSendCount - nodeSendCount;

    long now = System.currentTimeMillis();

    double agentSendRate = 0.0;
    double totalSendRate = 0.0;
    if (lastTime > 0) {
      double t = (long) ((now - lastTime) / 1000);
      if (t < 1.0) {
        t = 1.0;
      }
      agentSendRate = ((double) (agentSendCount - lastAgent)/t);
      totalSendRate = ((double) (totalSendCount - lastTotal)/t);
    }

    lastTime = now;
    lastAgent = agentSendCount;
    lastTotal = totalSendCount;

    return
      FORMAT_DECIMAL.format(agentSendRate)+", "+
      FORMAT_DECIMAL.format(totalSendRate)+", "+
      agentSendCount+", "+
      totalSendCount;
  }
  private long getTotalSendCount() {
    MessageStatistics.Statistics stats = 
      mss.getMessageStatistics(false);
    return (stats == null ? 0 : stats.totalSentMessageCount);
  }
  private long getNodeSendCount() {
    AgentStatusService.AgentState state = 
      as.getRemoteAgentState(localNode);
    return (state == null ? 0 : state.deliveredCount);
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
}
