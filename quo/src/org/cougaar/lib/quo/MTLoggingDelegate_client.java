/* -*- JAVA -*- $Id: MTLoggingDelegate_client.java,v 1.4 2001-11-02 17:07:56 psharma Exp $ */

// ****** Code generated by the QuO codegenerator version 3.0.7 ******
// QuO and the QuO codegenerator have been developed by 
// BBN Technologies.

// Information about QuO is available at:
// http://www.dist-systems.bbn.com/tech/QuO


package org.cougaar.lib.quo ;

import java.rmi.*;
import com.bbn.quo.rmi.*;
import com.bbn.quo.*;

public class MTLoggingDelegate_client implements org.cougaar.core.mts.MT {

  //These are instance variables declared in ASL.
  protected org.cougaar.core.mts.MT remote;

  void set_remote(org.cougaar.core.mts.MT arg1){
    remote = arg1;
  }
  void set_remoteObj(org.cougaar.core.mts.MT arg1){
    remote = arg1;
  }
  org.cougaar.core.mts.MT  get_remoteObj(){
    return(remote);
  }
  protected com.bbn.quo.rmi.Contract quo_Logging;
  void set_contract_Logging(com.bbn.quo.rmi.Contract arg1){
    quo_Logging = arg1;
  }
  com.bbn.quo.rmi.Contract get_contract_Logging() {
    return quo_Logging;
  }

  final static int LOGGING__NORMAL = 0;

  public void rerouteMessage(org.cougaar.core.society.Message m) throws RemoteException {
    // Default declarations and setup of quo introduced variables local to method:
    int[] quo_curRegs_Logging = null;
    com.bbn.quo.corba.Association[] signal =
        new com.bbn.quo.corba.Association[0];
    com.bbn.quo.corba.SignalEvent quo_sig1;
    com.bbn.quo.corba.SignalEvent quo_sig2;

    quo_sig1 = 	new com.bbn.quo.corba.SignalEvent("premethod", signal);
    try { 
quo_curRegs_Logging = quo_Logging.signalAndEvalAndGetCurrentRegion(quo_sig1);
     } catch (java.rmi.RemoteException pre_meth_ceval_ex) {} 
      // Adaptive code for inplaceof methodcall
      long startTime;
      startTime = System.currentTimeMillis();
      remote.rerouteMessage(m);
      Utils.logMessage(startTime, m);
      // Adaptive code for before postmethodcontracteval
      quo_sig2 = 	new com.bbn.quo.corba.SignalEvent("postmethod", signal);
      try { 
quo_curRegs_Logging = quo_Logging.signalAndEvalAndGetCurrentRegion(quo_sig2);
             } catch( java.rmi.RemoteException post_meth_ceval_ex) {} 
        return;
      }
      public org.cougaar.core.society.MessageAddress getMessageAddress() throws RemoteException {
        // Default declarations and setup of quo introduced variables local to method:
        int[] quo_curRegs_Logging = null;
        com.bbn.quo.corba.Association[] signal =
            new com.bbn.quo.corba.Association[0];
        org.cougaar.core.society.MessageAddress quo_retval;
        com.bbn.quo.corba.SignalEvent quo_sig1;
        com.bbn.quo.corba.SignalEvent quo_sig2;

        quo_retval = remote.getMessageAddress();
        return(quo_retval);
      }
};

