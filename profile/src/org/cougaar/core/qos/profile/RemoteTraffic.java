package org.cougaar.core.qos.profile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.cougaar.core.mts.AgentStatusService;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MulticastMessageAddress;
import org.cougaar.core.service.LoggingService;

/**
 * This component profiles the message traffic (message count and
 * byte count) from the local aggregated agents to each specific
 * remote target agent.
 * <p>
 * I.e. FROM (this node and it's agents) TO (a specific remote target) 
 * <p>
 * Example output:<pre> 
 *   tr_AgentB - #count, bytes,
 *   tr_AgentB - 4783, 6136339
 *   tr_NodeA - 38, 0
 *   tr_NodeB - 8, 8167
 * </pre>
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class RemoteTraffic extends ProfilerBase {
  private static final String[] FIELDS = new String[] {
    "count",
    "bytes",
  };
  private static final String HEADER = toHeader(FIELDS);
  private static final String ALIGN = "0, 0";
  private static final MulticastMessageAddress MMA = 
    MulticastMessageAddress.getMulticastMessageAddress("dummy");
  private static final Comparator MESSAGE_ADDRESS_COMPARATOR =
    new Comparator() {
      public int compare(Object a, Object b){
        String sa = ((MessageAddress) a).getAddress();
        String sb = ((MessageAddress) b).getAddress();
        return sa.compareTo(sb);
      }
    };
  private final Map logs = new HashMap();

  public AgentStatusService as;
  public Object dqms;

  @Override
public void load() {
    super.load();
    findServiceLater(
        "as",
        "org.cougaar.core.mts.AgentStatusService");
    findServiceLater(
        "dqms",
        "org.cougaar.mts.base.DestinationQueueMonitorService");
  }
  @Override
public void run() {
    logTraffic();
  }

  private void logTraffic() {
    MessageAddress[] targets = getDestinations();
    for (int i = 0; i < targets.length; i++) {
      MessageAddress t = targets[i];
      AgentStatusService.AgentState state = 
        as.getRemoteAgentState(t);
      if (state != null) {
        getLog(t).shout(
            state.deliveredCount+", "+
            state.deliveredBytes);
      }
    }
  }

  private LoggingService getLog(MessageAddress addr) {
    LoggingService log;
    synchronized (logs) {
      log = (LoggingService) logs.get(addr);
      if (log == null) {
        String as = addr.getAddress().replace('.', '_');
        String key = "tr_"+as;
        log = sb.getService(
           ("org.cougaar.core.qos.profile.remote_traffic."+
            key),
           LoggingService.class,
           null);
        logs.put(addr, log);
        if (header) {
          log.shout(HEADER);
        }
        if (align) {
          for (int i = 0, n = getRunCount(); i < n; i++) {
            log.shout("0, 0");
          }
        }
      }
    }
    return log;
  }

  private MessageAddress[] getDestinations() {
    MessageAddress[] ret;
    try {
      Class cl = Class.forName(
          "org.cougaar.mts.base.DestinationQueueMonitorService");
      Method m = cl.getMethod("getDestinations", null);
      ret = (MessageAddress[]) m.invoke(dqms, null);
    } catch (Exception e) {
      System.err.println("getDestinations failed: "+e);
      ret = null;
    }
    if (ret == null || ret.length == 0) {
      return new MessageAddress[0];
    }
    Arrays.sort(ret, MESSAGE_ADDRESS_COMPARATOR);
    return ret;
  }
}
