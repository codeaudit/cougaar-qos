package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */

public final class MTHelper
{
	public static void insert (final org.omg.CORBA.Any any, final org.cougaar.lib.quo.MT s)
	{
		any.insert_Object (s);
	}
	public static org.cougaar.lib.quo.MT extract (final org.omg.CORBA.Any any)
	{
		return narrow (any.extract_Object ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:cougaar/MT:1.0", "MT");
	}
	public static String id()
	{
		return "IDL:cougaar/MT:1.0";
	}
	public static MT read (final org.omg.CORBA.portable.InputStream in)
	{
		return narrow (in.read_Object ());
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, final org.cougaar.lib.quo.MT s)
	{
		_out.write_Object(s);
	}
	public static org.cougaar.lib.quo.MT narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (org.cougaar.lib.quo.MT)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:cougaar/MT:1.0"))
			{
				org.cougaar.lib.quo._MTStub stub;
				stub = new org.cougaar.lib.quo._MTStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
}
