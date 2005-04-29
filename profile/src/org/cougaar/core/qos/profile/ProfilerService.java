package org.cougaar.core.qos.profile;

/**
 * This service registers profilers with the {@link
 * ProfilerCoordinator}
 */
public interface ProfilerService {
  boolean logHeader();
  boolean logAlign();
  int getRunCount();

  interface Client {
    void run();
  }
}
