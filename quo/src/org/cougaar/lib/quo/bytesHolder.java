package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of alias "bytes"
 *	@author JacORB IDL compiler 
 */

public final class bytesHolder
	implements org.omg.CORBA.portable.Streamable
{
	public byte[] value;

	public bytesHolder ()
	{
	}
	public bytesHolder (final byte[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return bytesHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = bytesHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		bytesHelper.write (out,value);
	}
}
