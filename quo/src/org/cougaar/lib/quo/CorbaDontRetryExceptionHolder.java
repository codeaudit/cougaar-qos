package org.cougaar.lib.quo;
/**
 *	Generated from IDL definition of exception "CorbaDontRetryException"
 *	@author JacORB IDL compiler 
 */

final public class CorbaDontRetryExceptionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public org.cougaar.lib.quo.CorbaDontRetryException value;

	public CorbaDontRetryExceptionHolder ()
	{
	}
	public CorbaDontRetryExceptionHolder (org.cougaar.lib.quo.CorbaDontRetryException initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream _in)
	{
		value = org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.read(_in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.write(_out,value);
	}
}
