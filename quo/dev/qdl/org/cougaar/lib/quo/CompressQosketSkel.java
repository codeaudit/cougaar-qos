/* -*- JAVA -*- $Id: CompressQosketSkel.java,v 1.1 2001-10-18 21:27:35 psharma Exp $ */

// ****** Code generated by the QuO codegenerator version 3.0.7 ******
// QuO and the QuO codegenerator have been developed by 
// BBN Technologies.

// Information about QuO is available at:
// http://www.dist-systems.bbn.com/tech/QuO



package org.cougaar.lib.quo ;

abstract public class CompressQosketSkel
         implements org.cougaar.lib.quo.CompressQosket, 
         org.cougaar.lib.quo.CompressDelegateQosket
{
  // Sysconds from contract arglist
  protected com.bbn.quo.rmi.DataSC expectedServerLoadAverage;
  protected com.bbn.quo.rmi.DataSC expectedServerEffectiveMJips;
  protected com.bbn.quo.rmi.DataSC expectedClientEffectiveMJips;
  protected com.bbn.quo.rmi.DataSC expectedNetworkCapacity;
  protected com.bbn.quo.rmi.ValueSC UseCompression;

  // Callbacks from contract arglist

  // Syscondseqs from contract arglist

  // Callbacks from contract arglist

  // Contract
  protected com.bbn.quo.rmi.Contract quo_Compress;

  // Subclass Responsibility to override this function 
  public void initSysconds  (com.bbn.quo.rmi.QuoKernel kernel)
  throws java.rmi.RemoteException 
  {} 

  // Subclass Responsibility to override this function 
  public void initCallbacks ()
  {} 

  public com.bbn.quo.rmi.Contract initContract(com.bbn.quo.rmi.QuoKernel kernel)
  throws java.rmi.RemoteException 
  {
    com.bbn.quo.rmi.SysCond[] sysconds =  new com.bbn.quo.rmi.SysCond[5]; 
    sysconds[0] = expectedServerLoadAverage;
    sysconds[1] = expectedServerEffectiveMJips;
    sysconds[2] = expectedClientEffectiveMJips;
    sysconds[3] = expectedNetworkCapacity;
    sysconds[4] = UseCompression;

    com.bbn.quo.rmi.Callback[] callbacks =  new com.bbn.quo.rmi.Callback[0]; 

    quo_Compress = kernel.bindContract("Compress", "org::cougaar::lib::quo::Compress", sysconds , callbacks ); 
    return quo_Compress;
  }
}

