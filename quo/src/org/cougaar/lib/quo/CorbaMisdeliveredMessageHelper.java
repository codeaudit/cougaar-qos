package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of exception "CorbaMisdeliveredMessage"
 *	@author JacORB IDL compiler 
 */

public class CorbaMisdeliveredMessageHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_struct_tc(org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.id(),"CorbaMisdeliveredMessage",new org.omg.CORBA.StructMember[0]);
	public CorbaMisdeliveredMessageHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, org.cougaar.lib.quo.CorbaMisdeliveredMessage s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static org.cougaar.lib.quo.CorbaMisdeliveredMessage extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:cougaar/CorbaMisdeliveredMessage:1.0";
	}
	public static org.cougaar.lib.quo.CorbaMisdeliveredMessage read(org.omg.CORBA.portable.InputStream in)
	{
		org.cougaar.lib.quo.CorbaMisdeliveredMessage result = new org.cougaar.lib.quo.CorbaMisdeliveredMessage();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		return result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, org.cougaar.lib.quo.CorbaMisdeliveredMessage s)
	{
		out.write_string(id());
	}
}
