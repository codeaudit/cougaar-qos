/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior MTInst ()
{
    qosket qosket::instrumentation::ServerInstrumentationDelegateQosket qk;

    
    org::cougaar::core::mts::MessageAddress org::cougaar::lib::quo::MTInstrumented::getMessageAddress ()
	{
	    return_value org::cougaar::core::mts::MessageAddress addr;

	    inplaceof PREMETHODCONTRACTEVAL {
	    }

	    inplaceof METHODCALL {
		java_code #{
		    addr = ((org.cougaar.core.mts.MT) qk.getServer()).getMessageAddress();
		}#;
	    }
	}

    instr::Trace_rec org::cougaar::lib::quo::MTInstrumented::rerouteMessage
	(in instr::Trace_rec record,  in org::cougaar::core::mts::Message m)
	{
	    extern instr::Trace_rec rec;
	    extern long length;
	    return_value instr::Trace_rec result;

	    after METHODENTRY {
		length = 0;
		rec = record;
	    }

	    inplaceof METHODCALL {
		java_code #{
		    ((org.cougaar.core.mts.MT) qk.getServer()).rerouteMessage(m);
		}#;

	    }

	    before METHODRETURN {
		result = rec;
	    }
	}


    instr::Trace_rec org::cougaar::lib::quo::MTInstrumented::receiveCompressedMessage 
	(in instr::Trace_rec record, 
	 in org::cougaar::lib::quo::Zippy compressedMessage)
	{
	    extern instr::Trace_rec rec;
	    extern long length;
	    return_value instr::Trace_rec result;

	    after METHODENTRY {
		length = 0;
		rec = record;
	    }

	    inplaceof METHODCALL {
		java_code #{
		    org.cougaar.core.mts.Message msg = 
			(org.cougaar.core.mts.Message) compressedMessage.getData();
		    ((org.cougaar.core.mts.MT) qk.getServer()).rerouteMessage(msg);
		}#;
	    }

	    before METHODRETURN {
		result = rec;
	    }
	}


    void org::cougaar::lib::quo::MTInstrumented::receiveOnlyCompressedMessage 
	(in org::cougaar::lib::quo::Zippy compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
		java_code #{
		    org.cougaar.core.mts.Message msg = 
			(org.cougaar.core.mts.Message) compressedMessage.getData();
		    ((org.cougaar.core.mts.MT) qk.getServer()).rerouteMessage(msg);
		}#;
	    }
	}


    void org::cougaar::lib::quo::MTInstrumented::receiveOnlyCompressedBytes 
	(in byteSeq compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }

	    inplaceof METHODCALL {
		java_code #{
		    org.cougaar.core.mts.Message msg = 
		      (org.cougaar.core.mts.Message) 
		      Zippy.unzip(compressedMessage);
		    ((org.cougaar.core.mts.MT) 
		     qk.getServer()).rerouteMessage(msg);
		}#;
	    }
	}


    void org::cougaar::lib::quo::MTInstrumented::receiveOnlyBytes (in byteSeq message) {
	inplaceof PREMETHODCONTRACTEVAL {
	}
	inplaceof METHODCALL {
	    java_code #{
		org.cougaar.core.mts.Message msg = 
		    (org.cougaar.core.mts.Message) 
		    Zippy.fromByteArray(message);
		((org.cougaar.core.mts.MT) 
		 qk.getServer()).rerouteMessage(msg);
	    }#;
	}
    }

    void org::cougaar::lib::quo::MTInstrumented::ignoreCompressedMessage 
	(in org::cougaar::lib::quo::Zippy compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
	    }
	}

    void org::cougaar::lib::quo::MTInstrumented::ignoreCompressedBytes 
	(in byteSeq compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
		Zippy.unzip(compressedMessage);
	    }
	}

    void org::cougaar::lib::quo::MTInstrumented::ignoreBytes (in byteSeq message)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
		Zippy.fromByteArray(message);
	    }
	}

};

