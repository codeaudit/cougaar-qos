package org.cougaar.lib.quo;
/**
 *	Generated from IDL definition of alias "bytes"
 *	@author JacORB IDL compiler 
 */

public class bytesHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_alias_tc( org.cougaar.lib.quo.bytesHelper.id(),"bytes",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(10))));
	public bytesHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, byte[] s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static byte[] extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:cougaar/bytes:1.0";
	}
	public static byte[] read(org.omg.CORBA.portable.InputStream _in)
	{
		byte[] _result;
		int _l_result0 = _in.read_long();
		_result = new byte[_l_result0];
	_in.read_octet_array(_result,0,_l_result0);
		return _result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream _out, byte[] _s)
	{
		
		_out.write_long(_s.length);
		_out.write_octet_array(_s,0,_s.length);
	}
}
