/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior RemoteSSL ()
{
    remote_object org::cougaar::core::mts::DestinationLink remote;
    qosket org::cougaar::lib::quo::RemoteSSLDelegateQosket qk;
    
    long org::cougaar::core::mts::DestinationLink::cost
      (in org::cougaar::core::mts::Message message)
      {
	return_value long cost;
	inplaceof METHODCALL {
	  region SSL {
	    cost = qk.computeCost(message);
	  }
	}
      }

    

};
