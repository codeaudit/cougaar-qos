package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */

public abstract class MTPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, org.cougaar.lib.quo.MTOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "rerouteMessage", new java.lang.Integer(0));
	}
	private String[] ids = {"IDL:cougaar/MT:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public org.cougaar.lib.quo.MT _this()
	{
		return org.cougaar.lib.quo.MTHelper.narrow(_this_object());
	}
	public org.cougaar.lib.quo.MT _this(org.omg.CORBA.ORB orb)
	{
		return org.cougaar.lib.quo.MTHelper.narrow(_this_object(orb));
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // rerouteMessage
			{
			try
			{
				byte[] _arg0=org.cougaar.lib.quo.bytesHelper.read(_input);
				_out = handler.createReply();
				org.cougaar.lib.quo.bytesHelper.write(_out,rerouteMessage(_arg0));
			}
			catch(org.cougaar.lib.quo.CorbaMisdeliveredMessage _ex0)
			{
				_out = handler.createExceptionReply();
				org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.write(_out, _ex0);
			}
			catch(org.cougaar.lib.quo.CorbaMessageSecurityException _ex1)
			{
				_out = handler.createExceptionReply();
				org.cougaar.lib.quo.CorbaMessageSecurityExceptionHelper.write(_out, _ex1);
			}
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
