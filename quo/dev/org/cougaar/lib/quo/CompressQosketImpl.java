/* =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import com.bbn.quo.NetUtilities;
import com.bbn.quo.ParsedReference;
import com.bbn.quo.corba.Scope;
import com.bbn.quo.data.Constants;
import com.bbn.quo.data.Utilities;

import com.bbn.quo.rmi.QuoKernel;
import com.bbn.quo.rmi.ValueSC;
import com.bbn.quo.rmi.SysCond;
import com.bbn.quo.rmi.DataSC;
import com.bbn.quo.rmi.impl.RmiUtilities;

import java.rmi.Remote;
import java.util.zip.Deflater;

public class CompressQosketImpl 
  extends CompressQosketSkel
  implements Constants

{

  private MTInstrumented instrumentedServer;

  public CompressQosketImpl ()  {
  }


  public void runCompression(org.cougaar.core.society.Message message)
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
      System.out.println("$$$$$ Compression failure " + message);
      remote_ex.printStackTrace();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void runSerializeAndCompress(org.cougaar.core.society.Message message)
  {
    long startTime = System.currentTimeMillis(); 

    try {
      //Compressed Byte Array (makes array before streaming)
      Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
      byte[] compressedMessage = Zippy.zip(message, deflater);
      instrumentedServer.receiveOnlyCompressedBytes(compressedMessage);
    } 
    catch (java.rmi.RemoteException remote_ex) {
      System.out.println("$$$$$ Compression failure " + message);
      remote_ex.printStackTrace();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void runSerializeOnly(org.cougaar.core.society.Message message)
  {
    long startTime = System.currentTimeMillis(); 

    try {

      // Uncompressed Byte array
      byte[] msg = Zippy.toByteArray(message);
      instrumentedServer.receiveOnlyBytes(msg);

    } 
    catch (java.rmi.RemoteException remote_ex) {
      System.out.println("$$$$$ Compression failure " + message);
      remote_ex.printStackTrace();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }



  private void initRSSSysconds(QuoKernel kernel) 
    throws java.rmi.RemoteException
  {

    expectedNetworkCapacity =  
      Utilities.expectedNetworkCapacitySyscond(((Remote) instrumentedServer),
					       "NetCapacity", kernel);
    System.out.println("NetworkCapacity Syscond ready");

    // make LoadAverage SysCond
    	
    // The RMI parser finds the impl name from the ref.  The CORBA
    // parser finds the interface name from the ior.  Whichever
    // one is missing needs to be filled in manually by the
    // caller....
    
    ParsedReference remoteRef =  
      RmiUtilities.parseReference(instrumentedServer);
    //	try {
    // clientHost = Utilities.canonicalizeAddress(NetUtilities.getHostAddress());
    //     serverHost = Utilities.canonicalizeAddress(remoteRef.host);
    //	}
    //	catch (java.net.UnknownHostException unknown_host) {
    //	    unknown_host.printStackTrace();
    //	    return;
    //	}

    remoteRef.interfaceName="org/cougaar/core/society/MT";
    System.out.println(remoteRef);
    Scope[] remotePath = remoteRef.toPath();
    System.out.println("Created Load Average Path" + remotePath);
    SysCond syscond =  kernel.bindSysCond("ExpectedServerLoadAverage",
     					  "com.bbn.quo.rmi.DataSC",
     					  "com.bbn.quo.data.DataSCImpl");
    expectedServerLoadAverage = (DataSC) syscond;
    System.out.println("Created Load Average syscond");
    kernel.bindDataFormula(expectedServerLoadAverage, remotePath, "LoadAverage");
    //kernel.bindDataFormula(expectedServerLoadAverage, remotePath, "EffectiveServerLoadAverage");
    

    String effectiveStr =   "expectedServerEffectiveMJips";
    expectedServerEffectiveMJips=
      Utilities.expectedServerEffectiveMJipsSyscond(remoteRef,
						    effectiveStr, 
						    "org/cougaar/core/society/MT",
						    kernel);
    System.out.println("expectedServerEffectiveMJips Syscond ready");

    effectiveStr =   "expectedClientEffectiveMJips";
    expectedClientEffectiveMJips=
      Utilities.expectedClientEffectiveMJipsSyscond  (effectiveStr, kernel);
    System.out.println("expectedClientEffectiveMJips Syscond ready");

  }

  public void initSysconds(QuoKernel kernel) 
    throws java.rmi.RemoteException
  {
    SysCond syscond =  kernel.bindSysCond("UseCompression",
					  "com.bbn.quo.rmi.ValueSC",
					  "com.bbn.quo.ValueSCImpl");
    System.out.println("Created UseCompression syscond");

    UseCompression = (ValueSC) syscond;
    boolean useCompression = Boolean.getBoolean("org.cougaar.lib.quo.UseCompression");
    UseCompression.booleanValue(useCompression);
    try {
      initRSSSysconds(kernel);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }



  // No callbacks

    

  public void setInstrumentedServer(MTInstrumented object)
  {
    instrumentedServer = object;
  }

}


