/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior MTInst ()
{
    qosket qosket::instrumentation::ServerInstrumentationDelegateQosket qk;

    
    org::cougaar::core::mts::MessageAddress org::cougaar::lib::mquo::MTInstrumented::getMessageAddress ()
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

    instr::Trace_rec org::cougaar::lib::mquo::MTInstrumented::rerouteMessage
	(in instr::Trace_rec record,  in org::cougaar::core::mts::AttributedMessage m)
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


    instr::Trace_rec org::cougaar::lib::mquo::MTInstrumented::receiveCompressedMessage 
	(in instr::Trace_rec record, 
	 in org::cougaar::lib::mquo::Zippy compressedMessage)
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
		    org.cougaar.core.mts.AttributedMessage msg = 
			(org.cougaar.core.mts.AttributedMessage) compressedMessage.getData();
		    ((org.cougaar.core.mts.MT) qk.getServer()).rerouteMessage(msg);
		}#;
	    }

	    before METHODRETURN {
		result = rec;
	    }
	}


    void org::cougaar::lib::mquo::MTInstrumented::receiveOnlyCompressedMessage 
	(in org::cougaar::lib::mquo::Zippy compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
		java_code #{
		    org.cougaar.core.mts.AttributedMessage msg = 
			(org.cougaar.core.mts.AttributedMessage) compressedMessage.getData();
		    ((org.cougaar.core.mts.MT) qk.getServer()).rerouteMessage(msg);
		}#;
	    }
	}


    void org::cougaar::lib::mquo::MTInstrumented::receiveOnlyCompressedBytes 
	(in byteSeq compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }

	    inplaceof METHODCALL {
		java_code #{
		    org.cougaar.core.mts.AttributedMessage msg = 
		      (org.cougaar.core.mts.AttributedMessage) 
		      org.cougaar.lib.mquo.Zippy.unzip(compressedMessage);
		    ((org.cougaar.core.mts.MT) 
		     qk.getServer()).rerouteMessage(msg);
		}#;
	    }
	}


    void org::cougaar::lib::mquo::MTInstrumented::receiveOnlyBytes (in byteSeq message) {
	inplaceof PREMETHODCONTRACTEVAL {
	}
	inplaceof METHODCALL {
	    java_code #{
		org.cougaar.core.mts.AttributedMessage msg = 
		    (org.cougaar.core.mts.AttributedMessage) 
		  org.cougaar.lib.mquo.Zippy.fromByteArray(message);
		((org.cougaar.core.mts.MT) 
		 qk.getServer()).rerouteMessage(msg);
	    }#;
	}
    }

    void org::cougaar::lib::mquo::MTInstrumented::ignoreCompressedMessage 
	(in org::cougaar::lib::mquo::Zippy compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
	    }
	}

    void org::cougaar::lib::mquo::MTInstrumented::ignoreCompressedBytes 
	(in byteSeq compressedMessage)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
	      org.cougaar.lib.mquo.Zippy.unzip(compressedMessage);
	    }
	}

    void org::cougaar::lib::mquo::MTInstrumented::ignoreBytes (in byteSeq message)
	{
	    inplaceof PREMETHODCONTRACTEVAL {
	    }
	    inplaceof METHODCALL {
	      org.cougaar.lib.mquo.Zippy.fromByteArray(message);
	    }
	}

};

