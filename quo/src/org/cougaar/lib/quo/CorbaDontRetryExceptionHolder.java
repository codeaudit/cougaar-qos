package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of exception "CorbaDontRetryException"
 *	@author JacORB IDL compiler 
 */

public final class CorbaDontRetryExceptionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public org.cougaar.lib.quo.CorbaDontRetryException value;

	public CorbaDontRetryExceptionHolder ()
	{
	}
	public CorbaDontRetryExceptionHolder (final org.cougaar.lib.quo.CorbaDontRetryException initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream _in)
	{
		value = org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.read (_in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.write (_out,value);
	}
}
