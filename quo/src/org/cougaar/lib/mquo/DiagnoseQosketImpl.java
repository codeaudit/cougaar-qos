/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;


import org.cougaar.lib.quo.*;

import org.cougaar.core.service.LoggingService;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.mts.AttributedMessage;
import org.cougaar.core.mts.MT;

import com.bbn.quo.corba.Association;
import com.bbn.quo.instr.corba.Trace_rec;

import com.bbn.quo.qosket.instrumentation.InstrumentationWorker;

import java.rmi.Remote;

public class DiagnoseQosketImpl 
    extends DiagnoseQosketSkel
{

    private InstrumentationWorker worker;
    private LoggingService loggingService;

    public DiagnoseQosketImpl ()  {
	worker =  new InstrumentationWorker();
    }


    public void setLoggingService(LoggingService loggingService) {
	this.loggingService = loggingService;
    }

    public MessageAttributes runDiagnostic(AttributedMessage m, 
					   MT remoteObj)
    {
	try {
	    // Compressed Message
	    MTInstrumented iserver = 
		((MTInstrumented) getInstrumentedServer());
	    long startTime = System.currentTimeMillis();
	    long startWholeCallTime = startTime;
	    Zippy compressedMessage = new Zippy(m);
	    iserver.ignoreCompressedMessage(compressedMessage);
	    Utils.logEvent(startTime,m,"CompressedMessage");
	    // Compressed Byte array
	    startTime = System.currentTimeMillis();
	    byte[] bytes = Zippy.zip(m);
	    iserver.ignoreCompressedBytes(bytes);
	    Utils.logEvent(startTime,m,"CompressedBytes");
	    // Premarshalled Byte array
	    startTime = System.currentTimeMillis();
	    bytes = Zippy.toByteArray(m);
	    iserver.ignoreBytes(bytes);
	    Utils.logEvent(startTime,m,"PremarshalledBytes");
	    // Real Call
	    startTime = System.currentTimeMillis();
	    MessageAttributes attr = remoteObj.rerouteMessage(m);
	    Utils.logMessage(startTime,m);
	    // Whole message
	    Utils.logEvent(startWholeCallTime,m,"Whole Call");
	    return attr;
	} 
	catch (java.rmi.RemoteException remote_ex) {
	    loggingService.error(null, remote_ex);
	    return null;
	}
	catch (Exception ex) {
	    loggingService.error(null, ex);
	    return null;
	}
    }


    // No sysconds or callbacks


}


