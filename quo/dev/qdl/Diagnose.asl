/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior Diagnose () 
{
    // This should really be defined in a more general place and then
    // specialized here. Do that later.
    qosket org::cougaar::lib::quo::DiagnoseDelegateQosket qk;
    remote_object org::cougaar::core::mts::MT remote;

    void org::cougaar::core::mts::MT::rerouteMessage(in org::cougaar::core::society::Message m) {
	extern long length;

	after METHODENTRY {
	    length = 0;
	}

	inplaceof METHODCALL {
	    qk.runDiagnostic(m, remote);
	}

	after METHODCALL {
	    length = 0; // later fill this in for real
	}

	before POSTMETHODCONTRACTEVAL {
	    // force post-method eval
	}
    }

  
    org::cougaar::core::society::MessageAddress org::cougaar::core::mts::MT::getMessageAddress () {
    }


};
