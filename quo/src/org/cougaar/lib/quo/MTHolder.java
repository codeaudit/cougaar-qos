package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */

public final class MTHolder	implements org.omg.CORBA.portable.Streamable{
	 public MT value;
	public MTHolder ()
	{
	}
	public MTHolder (final MT initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return MTHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = MTHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		MTHelper.write (_out,value);
	}
}
