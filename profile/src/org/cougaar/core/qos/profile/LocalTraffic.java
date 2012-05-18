package org.cougaar.core.qos.profile;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cougaar.core.mts.AgentStatusService;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MulticastMessageAddress;
import org.cougaar.core.service.LoggingService;

/**
 * This component profiles the message traffic (message count and
 * byte count) of each local agents to any target.
 * <p> 
 * I.e. FROM (a specific local agent) TO (any remote target) 
 * <p> 
 * For example, the messaging for AgentA would look like:<pre>
 *   tl_AgentA - #count, bytes
 *   tl_AgentA - 15802, 20273716
 * </pre> 
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class LocalTraffic extends ProfilerBase {
  private static final String[] FIELDS = new String[] {
    "count",
    "bytes",
  };
  private static final String HEADER = toHeader(FIELDS);
  private static final String ALIGN = "0, 0";
  private final Map logs = new HashMap();

  public AgentStatusService as;
  public Object mtrs;

  @Override
public void load() {
    super.load();
    findServiceLater(
        "as",
        "org.cougaar.core.mts.AgentStatusService");
    findServiceLater(
        "mtrs",
        "org.cougaar.mts.base.MessageTransportRegistryService");
  }
  @Override
public void run() {
    logTraffic();
  }
  private void logTraffic() {
    for (Iterator iter = getLocalSenders(); iter.hasNext(); ) {
      MessageAddress t = (MessageAddress) iter.next();
      AgentStatusService.AgentState state = 
        as.getLocalAgentState(t);
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
        String key = "tl_"+as;
        log = sb.getService(
           "org.cougaar.core.qos.profile.local_traffic."+key,
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

  private Iterator getLocalSenders() {
    try {
      Class cl = Class.forName(
          "org.cougaar.mts.base.MessageTransportRegistryService");
      Method m = 
        cl.getMethod(
            "findLocalMulticastReceivers",
            new Class[] {MulticastMessageAddress.class});
      final MulticastMessageAddress mma = 
        MulticastMessageAddress.getMulticastMessageAddress("dummy");
      return (Iterator) 
        m.invoke(
            mtrs,
            new Object[] {mma});
    } catch (Exception e) {
      System.err.println("findLocalMulticastReceivers failed: "+e);
      return Collections.EMPTY_SET.iterator();
    }
  }
}
