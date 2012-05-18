package org.cougaar.core.qos.profile;

import java.lang.reflect.Field;

import org.cougaar.core.component.Service;
import org.cougaar.core.component.ServiceAvailableEvent;
import org.cougaar.core.component.ServiceAvailableListener;
import org.cougaar.core.component.ServiceBroker;

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
