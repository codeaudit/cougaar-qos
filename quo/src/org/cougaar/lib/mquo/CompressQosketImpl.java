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

import org.cougaar.core.mts.Message;
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

    public void runCompression(Message message)
    {
	long startTime = System.currentTimeMillis(); 

	try {
	    // Compressed Message (controls stream directly)
	    Zippy compressedMessage = new Zippy(message);
	    instrumentedServer.receiveOnlyCompressedMessage(compressedMessage);
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
    }

    public void runSerializeAndCompress(Message message)
    {
	long startTime = System.currentTimeMillis(); 

	try {
	    //Compressed Byte Array (makes array before streaming)
	    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
	    byte[] compressedMessage = Zippy.zip(message, deflater);
	    instrumentedServer.receiveOnlyCompressedBytes(compressedMessage);
	} 
	catch (java.rmi.RemoteException remote_ex) {
	    loggingService.error("runSerializeAndCompress RemoteException", 
				 remote_ex);
	}
	catch (Exception ex) {
	    loggingService.error("runSerializeAndCompress", ex);
	}
    }

    public void runSerializeOnly(Message message)
    {
	long startTime = System.currentTimeMillis(); 

	try {

	    // Uncompressed Byte array
	    byte[] msg = Zippy.toByteArray(message);
	    instrumentedServer.receiveOnlyBytes(msg);

	} 
	catch (java.rmi.RemoteException remote_ex) {
	    loggingService.error("runSerializeOnly RemoteException", 
				 remote_ex);
	}
	catch (Exception ex) {
	    loggingService.error("runSerializeOnly", ex);
	}
    }



    private void initRSSSysconds(QuoKernel kernel) 
	throws java.rmi.RemoteException
    {

	// The RMI parser finds the impl name from the ref.  The CORBA
	// parser finds the interface name from the ior.  Whichever
	// one is missing needs to be filled in manually by the
	// caller....
	final String iface = "org/cougaar/core/society/MT";
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
	    loggingService.error("initRSSSysconds", unknown_host);
	}


	String scname = "Bandwidth " +clientHost+ "->" +serverHost;
	expectedNetworkCapacity =  
	    Utilities.expectedNetworkCapacitySyscond(remote,
						     scname, 
						     kernel);

	scname = "Server MJips " +serverHost;
	expectedServerEffectiveMJips=
	    Utilities.expectedServerEffectiveMJipsSyscond(remoteRef,
							  scname, 
							  iface,
							  kernel);

	scname = "Client MJips " + clientHost;
	expectedClientEffectiveMJips=
	    Utilities.expectedClientEffectiveMJipsSyscond(scname, 
							  kernel);

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
	    initRSSSysconds(kernel);

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


