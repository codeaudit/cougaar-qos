/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior Logging ()
{
    remote_object org::cougaar::core::mts::MT remote;

    void org::cougaar::core::mts::MT::rerouteMessage(in org::cougaar::core::mts::AttributedMessage m) {
      return_value org::cougaar::core::mts::MessageAttributes attr;

	inplaceof METHODCALL {
	  local long long startTime;
	  startTime = System.currentTimeMillis();
	  attr = remote.rerouteMessage(m);
	  java_code #{
	    org.cougaar.lib.mquo.Utils.logMessage(startTime,m); 
	  }#;
	}


	before POSTMETHODCONTRACTEVAL {
	    // force post-method eval
	}
    }

    org::cougaar::core::mts::MessageAddress org::cougaar::core::mts::MT::getMessageAddress () {
    }


};
