package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of exception "CorbaDontRetryException"
 *	@author JacORB IDL compiler 
 */

public final class CorbaDontRetryException
	extends org.omg.CORBA.UserException
{
	public CorbaDontRetryException()
	{
		super(org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.id());
	}

	public byte[] cause;
	public CorbaDontRetryException(java.lang.String _reason,byte[] cause)
	{
		super(org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.id()+""+_reason );
		this.cause = cause;
	}
	public CorbaDontRetryException(byte[] cause)
	{
		this.cause = cause;
	}
}
