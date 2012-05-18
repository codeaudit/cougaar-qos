package org.cougaar.core.qos.profile;

import org.cougaar.core.component.Component;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.GenericStateModelAdapter;

/**
 * This component is the common base class for profiler components.
 */
public abstract class ProfilerBase
extends GenericStateModelAdapter
implements Component, ProfilerService.Client
{

  protected ServiceBroker sb;
  private ProfilerService ps;

  protected boolean header;
  protected boolean align;

  private LoggingService log;

  public void setServiceBroker(ServiceBroker sb) {
    this.sb = sb;
  }

  @Override
public void load() {
    super.load();

    ps = sb.getService(this, ProfilerService.class, null);
    if (ps == null) {
      throw new RuntimeException(
          "Unable to obtain the ProfilerService");
    }
    header = ps.logHeader();
    align = ps.logAlign();
  }

  protected int getRunCount() {
    return ps.getRunCount();
  }

  public abstract void run();

  public static final String toHeader(String[] fields) {
    StringBuffer buf = new StringBuffer();
    buf.append("#");
    for (int i = 0; i < fields.length; i++) {
      buf.append(fields[i]).append(", ");
    }
    return buf.toString();
  }

  protected void log(String cat, String h, String value) {
    if (log == null) {
      log = sb.getService(
            cat, LoggingService.class, null);
      if (header) {
        log.shout(h);
      }
      if (align) {
        // ALIGN as current value
        for (int i = 0, n = getRunCount(); i < n; i++) {
          log.shout(value);
        }
      }
    }
    log.shout(value);
  }

  protected void findServiceLater(
      String fname, String cname) {
    ServiceFinder.findServiceLater(fname, cname, sb, this);
  }
}
