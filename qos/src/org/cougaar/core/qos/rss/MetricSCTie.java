/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.core.qos.rss;

import java.rmi.RemoteException;
import org.cougaar.core.qos.metrics.MetricsService;

public class MetricSCTie 
    extends com.bbn.quo.rmi.impl.SysCond
    implements MetricSC
{
    private MetricSCOperations delegate;

    public MetricSCTie(MetricSCOperations delegate) 
	throws RemoteException
    {
	super();
	this.delegate = delegate;
    }

    public MetricSCOperations _delegate() {
	return delegate;
    }

    public boolean isReady() 
	throws RemoteException
    {
	return delegate.isReady();
    }
    
    public boolean booleanValue() 
	throws RemoteException
    {
	return delegate.booleanValue();
    }

    public byte octetValue() 
	throws RemoteException
    {
	return delegate.octetValue();
    }

    public char charValue () 
	throws RemoteException
    {
	return delegate.charValue();
    }

    public short shortValue() 
	throws RemoteException
    {
	return delegate.shortValue();
    }

    public int longValue() 
	throws RemoteException
    {
	return delegate.longValue();
    }

    public long longlongValue() 
	throws RemoteException
    {
	return delegate.longlongValue();
    }

    public float floatValue() 
	throws RemoteException
    {
	return delegate.floatValue();
    }

    public double doubleValue() 
	throws RemoteException
    {
	return delegate.doubleValue();
    }

    public String stringValue() 
	throws RemoteException
    {
	return delegate.stringValue();
    }

    public void init(MetricsService svc) 
	throws RemoteException
    {
	delegate.init(svc);
    }

    public void newPath(String path) 
	throws RemoteException
    {
	delegate.newPath(path);
    }

}
