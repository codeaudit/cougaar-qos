package org.cougaar.lib.quo;
/**
 *	Generated from IDL definition of exception "CorbaMessageSecurityException"
 *	@author JacORB IDL compiler 
 */

final public class CorbaMessageSecurityExceptionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public org.cougaar.lib.quo.CorbaMessageSecurityException value;

	public CorbaMessageSecurityExceptionHolder ()
	{
	}
	public CorbaMessageSecurityExceptionHolder (org.cougaar.lib.quo.CorbaMessageSecurityException initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream _in)
	{
		value = org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.read(_in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.write(_out,value);
	}
}
