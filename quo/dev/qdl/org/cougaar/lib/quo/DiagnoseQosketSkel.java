/* -*- JAVA -*- $Id: DiagnoseQosketSkel.java,v 1.1 2001-10-18 21:27:35 psharma Exp $ */

// ****** Code generated by the QuO codegenerator version 3.0.7 ******
// QuO and the QuO codegenerator have been developed by 
// BBN Technologies.

// Information about QuO is available at:
// http://www.dist-systems.bbn.com/tech/QuO



package org.cougaar.lib.quo ;

abstract public class DiagnoseQosketSkel
         extends com.bbn.quo.qosket.instrumentation.rmi.InstrumentationQosketImpl
         implements org.cougaar.lib.quo.DiagnoseQosket, 
         org.cougaar.lib.quo.DiagnoseDelegateQosket
{
  // Sysconds from contract arglist

  // Callbacks from contract arglist

  // Syscondseqs from contract arglist

  // Callbacks from contract arglist

  // Contract
  protected com.bbn.quo.rmi.Contract quo_Diagnose;

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
    com.bbn.quo.rmi.SysCond[] sysconds =  new com.bbn.quo.rmi.SysCond[0]; 

    com.bbn.quo.rmi.Callback[] callbacks =  new com.bbn.quo.rmi.Callback[0]; 

    quo_Diagnose = kernel.bindContract("Diagnose", "org::cougaar::lib::quo::Diagnose", sysconds , callbacks ); 
    return quo_Diagnose;
  }
}

