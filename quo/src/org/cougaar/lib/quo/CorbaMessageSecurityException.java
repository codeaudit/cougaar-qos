package org.cougaar.lib.quo;
/**
 *	Generated from IDL definition of exception "CorbaMessageSecurityException"
 *	@author JacORB IDL compiler 
 */

public final class CorbaMessageSecurityException
	extends org.omg.CORBA.UserException
{
	public CorbaMessageSecurityException()
	{
		super(org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.id());
	}

	public byte[] security_exception;
	public CorbaMessageSecurityException(java.lang.String _reason,byte[] security_exception)
	{
		super(org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.id()+""+_reason );
		this.security_exception = security_exception;
	}
	public CorbaMessageSecurityException(byte[] security_exception)
	{
		this.security_exception = security_exception;
	}
}
