package org.cougaar.core.qos.profile;

import java.util.Map;
import org.cougaar.core.component.Service;

/**
 * This service registers agents with the node-level {@link
 * BlackboardSize} profiler.
 */
public interface BlackboardSizeService extends Service {
  interface Client {
    String getName();
    // String type => int[] { current, added, changed, removed };
    Map getData(); 
  }
}

