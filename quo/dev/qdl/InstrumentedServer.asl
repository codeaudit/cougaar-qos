/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior InstrumentedServer ()
{
  
  SimpleInstrumentedServer<org::cougaar::lib::mquo::MTInstrumented::rerouteMessage>;
  SimpleInstrumentedServer<org::cougaar::lib::mquo::MTInstrumented::receiveCompressedMessage>;

};
