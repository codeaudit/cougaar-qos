/* =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;

import com.bbn.quo.NetUtilities;
import com.bbn.quo.ParsedReference;
import com.bbn.quo.data.Constants;
import com.bbn.quo.data.Utilities;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.ValueSC;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.rmi.DataSC;
import com.bbn.quo.rmi.impl.RmiUtilities;

import org.cougaar.core.mts.AttributedMessage;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.service.LoggingService;

import java.rmi.Remote;
import java.util.zip.Deflater;

public class CompressQosketImpl 
  extends CompressQosketSkel
  implements Constants

{
    private static ValueSC USE_COMPRESSION;

    private MTInstrumented instrumentedServer;
    private LoggingService loggingService;

    public CompressQosketImpl ()  {
    }


    public void setLoggingService(LoggingService loggingService) {
	this.loggingService = loggingService;
    }

    public MessageAttributes runCompression(AttributedMessage message)
    {
	long startTime = System.currentTimeMillis(); 
	MessageAttributes attr = null;
	try {
	    // Compressed Message (controls stream directly)
	    Zippy compressedMessage = new Zippy(message);
	    attr = instrumentedServer.receiveOnlyCompressedMessage(compressedMessage);
	    int raw =  compressedMessage.getRawDataSize();
	    int compressed = compressedMessage.getCompressedDataSize();
	    Utils.logMessageWithLength(startTime, message, raw, compressed); 
  
	} 
	catch (java.rmi.RemoteException remote_ex) {
	    loggingService.error("runCompression, RemoteException", remote_ex);
	}
	catch (Exception ex) {
	    loggingService.error("runCompression", ex);
	}
	return attr;
    }

    public MessageAttributes runSerializeAndCompress(AttributedMessage message)
    {
	long startTime = System.currentTimeMillis(); 
	MessageAttributes attr = null;

	try {
	    //Compressed Byte Array (makes array before streaming)
	    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
	    byte[] compressedMessage = Zippy.zip(message, deflater);
	    attr = instrumentedServer.receiveOnlyCompressedBytes(compressedMessage);
	} 
	catch (java.rmi.RemoteException remote_ex) {
	    loggingService.error("runSerializeAndCompress RemoteException", 
				 remote_ex);
	}
	catch (Exception ex) {
	    loggingService.error("runSerializeAndCompress", ex);
	}
	return attr;
    }

    public MessageAttributes runSerializeOnly(AttributedMessage message)
    {
	long startTime = System.currentTimeMillis(); 
	MessageAttributes attr = null;

	try {

	    // Uncompressed Byte array
	    byte[] msg = Zippy.toByteArray(message);
	    attr = instrumentedServer.receiveOnlyBytes(msg);

	} 
	catch (java.rmi.RemoteException remote_ex) {
	    loggingService.error("runSerializeOnly RemoteException", 
				 remote_ex);
	}
	catch (Exception ex) {
	    loggingService.error("runSerializeOnly", ex);
	}
	
	return attr;
    }



    private void initMetricSysconds(QuoKernel kernel) 
	throws java.rmi.RemoteException
    {

	// The RMI parser finds the impl name from the ref.  The CORBA
	// parser finds the interface name from the ior.  Whichever
	// one is missing needs to be filled in manually by the
	// caller....
	final String iface = "org/cougaar/core/mts/MT";
	String clientHost = null;
	String serverHost = null; 
	Remote remote = (Remote) instrumentedServer;
	ParsedReference remoteRef =  
	    RmiUtilities.parseReference(instrumentedServer);
	remoteRef.interfaceName= iface;
	try {
	    clientHost = 
		Utilities.canonicalizeAddress(NetUtilities.getHostAddress());
	    serverHost = Utilities.canonicalizeAddress(remoteRef.host);
	} catch (java.net.UnknownHostException unknown_host) {
	    loggingService.error("initMetricSysconds", unknown_host);
	}


	SyscondFactory factory = SyscondFactory.getFactory();
	
	expectedNetworkCapacity = 
	    factory.getExpectedBandwidthForHostSyscond(serverHost);

	expectedServerEffectiveMJips=
	    factory.getExpectedEffectiveMJipsForHostSyscond(serverHost);

	expectedClientEffectiveMJips=
	    factory.getExpectedEffectiveMJipsForHostSyscond(clientHost);

    }

    static synchronized void initSharedSysconds(QuoKernel kernel) 
	throws java.rmi.RemoteException
    {
	if (USE_COMPRESSION == null) {
	    SysCond syscond =  kernel.bindSysCond("UseCompression",
						  "com.bbn.quo.rmi.ValueSC",
						  "com.bbn.quo.ValueSCImpl");
	    USE_COMPRESSION = (ValueSC) syscond;
	}

    }

    public void initSysconds(QuoKernel kernel) 
	throws java.rmi.RemoteException
    {
	try {
	    initSharedSysconds(kernel);
	    UseCompression = USE_COMPRESSION;
	    boolean useCompression = 
		Boolean.getBoolean("org.cougaar.lib.quo.UseCompression");
	    UseCompression.booleanValue(useCompression);
	    initMetricSysconds(kernel);

	}
	catch (Exception ex) {
	    loggingService.error("initSysconds", ex);
	}
    }



    // No callbacks

    

    public void setInstrumentedServer(MTInstrumented object)
    {
	instrumentedServer = object;
    }

}


