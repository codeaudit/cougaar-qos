package org.cougaar.core.qos.profile;

import java.lang.reflect.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
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
 * A utility class to simplify late-binding service lookup.
 * <p>
 * This could move into org.cougaar.core.component.
 */
public class ServiceFinder {

  private ServiceFinder() {}

  public interface Callback {
    void foundService(Service s);
  }

  public static boolean findServiceLater(
      String fieldName,
      String serviceClassName,
      ServiceBroker sb,
      Object requestor) {
    return findServiceLater(
        sb,
        serviceClassName,
        requestor,
        requestor,
        fieldName);
  }
  public static boolean findServiceLater(
      final ServiceBroker sb,
      final String cname,
      final Object requestor,
      final Object settable,
      final String fname) {
    Class cl;
    try {
      cl = Class.forName(cname);
    } catch (Exception e) {
      throw new RuntimeException("reflect", e);
    }
    final Field f;
    try {
      f = settable.getClass().getField(fname);
    } catch (Exception e) {
      throw new RuntimeException("reflect", e);
    }
    ServiceFinder.Callback sfc = 
      new ServiceFinder.Callback() {
        public void foundService(Service s) {
          try {
            f.set(settable, s);
          } catch (Exception e) {
          }
        }
      };
    return findServiceLater(sb, cl, requestor, sfc);
  }
  public static boolean findServiceLater(
      final ServiceBroker sb,
      final Class cl,
      final Object requestor,
      final Callback cb) {
    final Object req = 
      (requestor == null ?
       ServiceFinder.class :
       requestor);
    if (sb.hasService(cl)) {
      Service s = (Service) sb.getService(req, cl, null);
      cb.foundService(s);
      return true;
    }
    ServiceAvailableListener sal =
      new ServiceAvailableListener() {
        public void serviceAvailable(ServiceAvailableEvent ae) {
          if (cl.isAssignableFrom(ae.getService())) {
            Service s = (Service) sb.getService(req, cl, null);
            cb.foundService(s);
            //sb.removeServiceListener(this);
          }
        }
      };
    sb.addServiceListener(sal);
    return false;
  }
}
