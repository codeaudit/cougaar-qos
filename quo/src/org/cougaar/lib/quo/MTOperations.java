package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */


public interface MTOperations
{
	/* constants */
	/* operations  */
	byte[] rerouteMessage(byte[] message) throws org.cougaar.lib.quo.CorbaMisdeliveredMessage,org.cougaar.lib.quo.CorbaDontRetryException;
}
