package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of exception "CorbaMisdeliveredMessage"
 *	@author JacORB IDL compiler 
 */

public final class CorbaMisdeliveredMessageHolder
	implements org.omg.CORBA.portable.Streamable
{
	public org.cougaar.lib.quo.CorbaMisdeliveredMessage value;

	public CorbaMisdeliveredMessageHolder ()
	{
	}
	public CorbaMisdeliveredMessageHolder (final org.cougaar.lib.quo.CorbaMisdeliveredMessage initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream _in)
	{
		value = org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.read (_in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.write (_out,value);
	}
}
