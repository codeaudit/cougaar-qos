/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior Compression ()
{

    remote_object org::cougaar::core::mts::MT remote;
    qosket org::cougaar::lib::quo::CompressDelegateQosket qk;

    org::cougaar::core::mts::MessageAttributes
      org::cougaar::core::mts::MT::rerouteMessage(in org::cougaar::core::mts::AttributedMessage m) {
      return_value org::cougaar::core::mts::MessageAttributes attr;
	inplaceof METHODCALL {
	    region Normal {
	      attr = remote.rerouteMessage(m);
	    }

	    region Compress {
	      attr = qk.runSerializeAndCompress(m);
	    }
	}


	before POSTMETHODCONTRACTEVAL {
	    // force post-method eval
	}
    }

    org::cougaar::core::mts::MessageAddress org::cougaar::core::mts::MT::getMessageAddress () {
    }


};
