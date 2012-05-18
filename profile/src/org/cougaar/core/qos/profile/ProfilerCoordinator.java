package org.cougaar.core.qos.profile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.cougaar.core.component.Component;
import org.cougaar.core.component.Service;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;
import org.cougaar.util.Arguments;
import org.cougaar.util.GenericStateModelAdapter;
import org.cougaar.util.RarelyModifiedList;

/**
 * This component coordinates the profiler components to
 * log at the same time. 
 * <p> 
 * An optional "period=<i>LONG</i>" argument is supported,
 * which defaults to 60000 (one minute).
 * <p>
 * If the system property<br> 
 * &nbsp;&nbsp;-Dorg.cougaar.core.society.startTime=<i>LONG</i><br>
 * is set, this class will use that timestamp as the base
 * offset for the profiler logs.  This is used to align the
 * number of logged profiling rows across multiple machines
 * (e.g. if host B starts 2 minutes after host A, the alignment
 * will add 2 blank rows for each profiler output).
 */
public final class ProfilerCoordinator
extends GenericStateModelAdapter
implements Component, Runnable
{

  private boolean header = true;
  private boolean align = true;
  private long period = 60*1000;

  private ServiceBroker sb;
  private ThreadService threadService;
  private Schedulable thread;

  private ProfilerSP psp;

  private RarelyModifiedList profilers = 
    new RarelyModifiedList();

  private long startTime;
  private int runCount;

  public void setParameter(Object o) {
    Arguments args = new Arguments(o);
    period = args.getLong("period", period);
  }

  public void setServiceBroker(ServiceBroker sb) {
    this.sb = sb;
  }

  @Override
public void load() {
    super.load();

    // advertise our profiler coordination service
    psp = new ProfilerSP();
    sb.addService(ProfilerService.class, psp);
  }

  @Override
public void start() {
    super.start();

    // must wait for "start()" to make sure the ThreadService
    // is loaded, since we're loaded at HIGH priority.

    // "run()" later
    scheduleNextRun();
  }

  public void run() {
    // invoke profilers
    List l = profilers.getUnmodifiableList();
    for (int i = 0, ln = l.size(); i < ln; i++) {
      ProfilerService.Client c = (ProfilerService.Client) l.get(i);
      c.run();
    }

    // run again later
    scheduleNextRun();
  }

  private void scheduleNextRun() {
    if (startTime > 0) {
      // already initialized, schedule next run.
      //
      // adjust to fixed interval
      runCount++; 
      long nextTime = startTime + runCount * period; 
      long now = System.currentTimeMillis();
      long delta = (nextTime - now);
      if (delta <= 0) {
        thread.start();
      } else {
        thread.schedule(delta);
      }
      return;
    }

    // get thread
    ThreadService threadService = sb.getService(this, ThreadService.class, null);
    thread = threadService.getThread(
        this,
        this,
        "ProfilerCoordinator");
    sb.releaseService(this, ThreadService.class, threadService);

    // get ACME time when first node was launched
    long t = -1; 
    String value =
      System.getProperty("org.cougaar.core.society.startTime");
    if (value != null) {
      try {
        DateFormat f = new SimpleDateFormat("MM/dd/yyy H:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        t = f.parse(value).getTime();
      } catch (ParseException e) { 
      }
    }
    long now = System.currentTimeMillis(); 
    if (t <= 0 || t > now) {
      // no ACME timestamp, not aligned
      t = now;
    }
    startTime = t;

    if (startTime == now) {
      thread.start();
      return;
    }
    // align w/ society time
    runCount = (int) ((now - startTime) / period);
    long rem = (now - startTime) % period;
    if (rem > 0) {
      runCount++;
    }
    long delta = 
      (rem > 0 && rem < period ?
       (period - rem) :
       0);
    if (delta <= 0) {
      thread.start();
    } else {
      thread.schedule(delta);
    }
  }

  private final class ProfilerSP extends ServiceProviderBase {
    @Override
   protected Class getServiceClass() { return ProfilerService.class; }
    @Override
   protected Class getClientClass() { return ProfilerService.Client.class; }
    @Override
   protected void register(Object client) {
      profilers.add(client);
    }
    @Override
   protected void unregister(Object client) {
      profilers.remove(client);
    }
    @Override
   protected Service getService(Object client) { return new SI(client); }
    protected class SI extends MyServiceImpl implements ProfilerService {
      public SI(Object client) { super(client); }
      public boolean logHeader() {
        return header;
      }
      public boolean logAlign() {
        return align;
      }
      public int getRunCount() {
        return runCount;
      }
    }
  }
}
