package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of exception "CorbaMessageSecurityException"
 *	@author JacORB IDL compiler 
 */

public class CorbaMessageSecurityExceptionHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_struct_tc(org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.id(),"CorbaMessageSecurityException",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("security_exception",org.omg.CORBA.ORB.init().create_alias_tc( org.cougaar.lib.quo.bytesHelper.id(),"bytes",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(10)))),null)});
	public CorbaMessageSecurityExceptionHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, org.cougaar.lib.quo.CorbaMessageSecurityException s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static org.cougaar.lib.quo.CorbaMessageSecurityException extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:cougaar/CorbaMessageSecurityException:1.0";
	}
	public static org.cougaar.lib.quo.CorbaMessageSecurityException read(org.omg.CORBA.portable.InputStream in)
	{
		org.cougaar.lib.quo.CorbaMessageSecurityException result = new org.cougaar.lib.quo.CorbaMessageSecurityException();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		result.security_exception = org.cougaar.lib.quo.bytesHelper.read(in);
		return result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, org.cougaar.lib.quo.CorbaMessageSecurityException s)
	{
		out.write_string(id());
		org.cougaar.lib.quo.bytesHelper.write(out,s.security_exception);
	}
}
