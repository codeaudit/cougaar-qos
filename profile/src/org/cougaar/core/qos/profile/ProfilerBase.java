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

  public void load() {
    super.load();

    ps = (ProfilerService)
      sb.getService(this, ProfilerService.class, null);
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
      log = (LoggingService) 
        sb.getService(
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
