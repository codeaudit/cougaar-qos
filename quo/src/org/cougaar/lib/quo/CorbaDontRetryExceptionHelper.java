package org.cougaar.lib.quo;


/**
 *	Generated from IDL definition of exception "CorbaDontRetryException"
 *	@author JacORB IDL compiler 
 */

public final class CorbaDontRetryExceptionHelper
{
	private static org.omg.CORBA.TypeCode _type = null;
	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_exception_tc( org.cougaar.lib.quo.CorbaDontRetryExceptionHelper.id(),"CorbaDontRetryException",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("cause", org.cougaar.lib.quo.bytesHelper.type(), null)});
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final org.cougaar.lib.quo.CorbaDontRetryException s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static org.cougaar.lib.quo.CorbaDontRetryException extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:cougaar/CorbaDontRetryException:1.0";
	}
	public static org.cougaar.lib.quo.CorbaDontRetryException read (final org.omg.CORBA.portable.InputStream in)
	{
		org.cougaar.lib.quo.CorbaDontRetryException result = new org.cougaar.lib.quo.CorbaDontRetryException();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		result.cause = org.cougaar.lib.quo.bytesHelper.read(in);
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final org.cougaar.lib.quo.CorbaDontRetryException s)
	{
		out.write_string(id());
		org.cougaar.lib.quo.bytesHelper.write(out,s.cause);
	}
}
