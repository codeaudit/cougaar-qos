package org.cougaar.lib.quo;

import org.omg.PortableServer.POA;
/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */

public class MTPOATie
	extends MTPOA
{
	private MTOperations _delegate;

	private POA _poa;
	public MTPOATie(MTOperations delegate)
	{
		_delegate = delegate;
	}
	public MTPOATie(MTOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public org.cougaar.lib.quo.MT _this()
	{
		return org.cougaar.lib.quo.MTHelper.narrow(_this_object());
	}
	public org.cougaar.lib.quo.MT _this(org.omg.CORBA.ORB orb)
	{
		return org.cougaar.lib.quo.MTHelper.narrow(_this_object(orb));
	}
	public MTOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(MTOperations delegate)
	{
		_delegate = delegate;
	}
	public byte[] rerouteMessage(byte[] message) throws org.cougaar.lib.quo.CorbaMisdeliveredMessage
	{
		return _delegate.rerouteMessage(message);
	}

}
