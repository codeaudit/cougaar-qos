package org.cougaar.lib.quo;


/**
 *	Generated from IDL definition of exception "CorbaMisdeliveredMessage"
 *	@author JacORB IDL compiler 
 */

public final class CorbaMisdeliveredMessageHelper
{
	private static org.omg.CORBA.TypeCode _type = null;
	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_exception_tc( org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.id(),"CorbaMisdeliveredMessage",new org.omg.CORBA.StructMember[0]);
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final org.cougaar.lib.quo.CorbaMisdeliveredMessage s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static org.cougaar.lib.quo.CorbaMisdeliveredMessage extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:cougaar/CorbaMisdeliveredMessage:1.0";
	}
	public static org.cougaar.lib.quo.CorbaMisdeliveredMessage read (final org.omg.CORBA.portable.InputStream in)
	{
		org.cougaar.lib.quo.CorbaMisdeliveredMessage result = new org.cougaar.lib.quo.CorbaMisdeliveredMessage();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final org.cougaar.lib.quo.CorbaMisdeliveredMessage s)
	{
		out.write_string(id());
	}
}
