package org.cougaar.lib.quo;
/**
 *	Generated from IDL definition of alias "bytes"
 *	@author JacORB IDL compiler 
 */

final public class bytesHolder
	implements org.omg.CORBA.portable.Streamable
{
	public byte[] value;

	public bytesHolder ()
	{
	}
	public bytesHolder (byte[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return bytesHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = bytesHelper.read(in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		bytesHelper.write(out,value);
	}
}
