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
 * This component profiles the per-agent blackboard size and
 * add/change/remove counts for standard blackboard object types
 * (UniqueObject, Task, etc).
 * <p>
 * Example output:<pre> 
 *   bb_AgentA__Task - #current, added, changed, removed
 *   bb_AgentA__Task - 5807, 7615, 3514, 1806
 *   bb_AgentA__UniqueObject - 11853, 18841, 46569, 6988
 *   bb_AgentA__transactions - 192, 192, 0, 0
 * </pre> 
 * In this example, 5807 tasks are on the blackboard. 
 * <p>
 * "transactions" is used to measure the number of plugin
 * "execute()" cycles. 
 * 
 * @see ProfilerCoordinator required coordinator component
 * @see BlackboardSizeInAgent required per-agent component 
 */
public class BlackboardSize extends ProfilerBase {
  private /*static*/ final String HEADER =
    "#"+
    "current, "+
    "added, "+
    "changed, "+
    "removed";
  private ServiceBroker rootsb;
  private ServiceProvider sp;
  private Map logs = new HashMap();
  private Map clients = new HashMap();
  public void load() {
    super.load();
    NodeControlService ncs = (NodeControlService)
      sb.getService(this, NodeControlService.class, null);
    rootsb = ncs.getRootServiceBroker();
    sb.releaseService(this, NodeControlService.class, ncs);

    sp = new BlackboardSizeSP();
    rootsb.addService(BlackboardSizeService.class, sp);
  }
  public void run() {
    logBB();
  }
  private void logBB() {
    synchronized (clients) {
      for (Iterator iter = clients.entrySet().iterator();
          iter.hasNext();
          ) {
        Map.Entry me = (Map.Entry) iter.next();
        String name = (String) me.getKey();
        BlackboardSizeService.Client c =
          (BlackboardSizeService.Client) me.getValue();
        Map m = c.getData();
        String[] keys = (String[]) 
          m.keySet().toArray(
              new String[m.size()]);
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++) {
          int[] values = (int[]) m.get(keys[i]);
          getLog(name, keys[i]).shout(
              values[0]+", "+
              values[1]+", "+
              values[2]+", "+
              values[3]);
        }
      }
    }
  }
  private LoggingService getLog(String name, String id) {
    LoggingService log;
    synchronized (logs) {
      Map m = (Map) logs.get(name);
      if (m == null) {
        m = new HashMap();
        logs.put(name, m);
      }
      log = (LoggingService) m.get(id);
      if (log == null) {
        String sn = name.replace('.', '_');
        String key = sn+"__"+id;
        log = (LoggingService)
          sb.getService(
              "org.cougaar.core.qos.profile.bb.bb_"+key,
              LoggingService.class, null);
        m.put(id, log);
        if (header) {
          log.shout(HEADER);
        }
        if (align) {
          for (int i = 0, n = getRunCount(); i < n; i++) {
            log.shout("0, 0, 0, 0");
          }
        }
      }
    }
    return log;
  }
  private void register(BlackboardSizeService.Client c) {
    synchronized (clients) {
      clients.put(c.getName(), c);
    }
  }
  private void unregister(BlackboardSizeService.Client c) {
    synchronized (clients) {
      clients.remove(c.getName());
    }
  }
  private class BlackboardSizeSP implements ServiceProvider {
    private final BlackboardSizeService SVC = new BlackboardSizeService() {};
    public Object getService(
        ServiceBroker sb, Object requestor, Class serviceClass) {
      if (!BlackboardSizeService.class.isAssignableFrom(serviceClass)) {
        return null;
      }
      register((BlackboardSizeService.Client) requestor);
      return SVC;
    }
    public void releaseService(
        ServiceBroker sb, Object requestor,
        Class serviceClass, Object service) {
      unregister((BlackboardSizeService.Client) requestor);
    }
  }
}
