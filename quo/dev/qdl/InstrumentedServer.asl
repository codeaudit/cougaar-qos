/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

behavior InstrumentedServer ()
{
  
  SimpleInstrumentedServer<org::cougaar::lib::quo::MTInstrumented::rerouteMessage>;
  SimpleInstrumentedServer<org::cougaar::lib::quo::MTInstrumented::receiveCompressedMessage>;

};
