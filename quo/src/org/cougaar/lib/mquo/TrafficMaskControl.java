// Copyright blah blah

package org.cougaar.lib.mquo;


import org.cougaar.lib.quo.*;

import com.bbn.quo.rmi.Callback;

public interface TrafficMaskControl extends Callback
{
    public void turnOn() throws java.rmi.RemoteException;

    public void turnOff() throws java.rmi.RemoteException;
}

