/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior Compression ()
{

    remote_object org::cougaar::core::mts::MT remote;
    qosket org::cougaar::lib::quo::CompressDelegateQosket qk;

    void org::cougaar::core::mts::MT::rerouteMessage(in org::cougaar::core::society::Message m) {
	inplaceof METHODCALL {
	    region Normal {
	      remote.rerouteMessage(m);
	    }

	    region Compress {
		qk.runSerializeAndCompress(m);
	    }
	}


	before POSTMETHODCONTRACTEVAL {
	    // force post-method eval
	}
    }

    org::cougaar::core::society::MessageAddress org::cougaar::core::mts::MT::getMessageAddress () {
    }


};
