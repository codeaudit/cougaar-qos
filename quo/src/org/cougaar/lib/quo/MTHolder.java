package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */

public class MTHolder	implements org.omg.CORBA.portable.Streamable{
	 public MT value;
	public MTHolder()
	{
	}
	public MTHolder(MT initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return MTHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = MTHelper.read(in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		MTHelper.write(_out,value);
	}
}
