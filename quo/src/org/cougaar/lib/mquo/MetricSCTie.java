/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import java.rmi.RemoteException;
import org.cougaar.core.qos.metrics.MetricsService;

public class MetricSCTie 
    extends com.bbn.quo.rmi.ReadOnlyValueSCTie
    implements MetricSC
{

    public MetricSCTie(MetricSCOperations delegate) 
	throws RemoteException
    {
	super(delegate);
    }

    public void init(MetricsService svc) 
	throws RemoteException
    {
	((MetricSCOperations) _delegate()).init(svc);
    }

    public void newPath(String path) 
	throws RemoteException
    {
	((MetricSCOperations) _delegate()).newPath(path);
    }

}
