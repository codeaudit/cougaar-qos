/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import com.bbn.quo.instr.corba.Trace_rec;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.cougaar.core.mts.AttributedMessage;
import org.cougaar.core.mts.MessageAddress;

public interface MTInstrumented extends Remote 
{
    MessageAddress getMessageAddress() throws RemoteException;

    Trace_rec rerouteMessage(Trace_rec record, AttributedMessage m) 
	throws RemoteException;


    Trace_rec receiveCompressedMessage(Trace_rec record,
				       Zippy compressedMessage)
	throws RemoteException;


    void receiveOnlyCompressedMessage(Zippy compressedMessage)
	throws RemoteException;

    void receiveOnlyCompressedBytes(byte[] compressedMessage)
	throws RemoteException;

    void receiveOnlyBytes(byte[] message)
	throws RemoteException;


    void ignoreCompressedMessage(Zippy compressedMessage)
	throws RemoteException;

    void ignoreCompressedBytes(byte[] compressedMessage)
	throws RemoteException;

    void ignoreBytes(byte[] message)
	throws RemoteException;

}

