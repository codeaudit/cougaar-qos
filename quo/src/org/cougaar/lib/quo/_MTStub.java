package org.cougaar.lib.quo;

/**
 *	Generated from IDL definition of interface "MT"
 *	@author JacORB IDL compiler 
 */

public class _MTStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements org.cougaar.lib.quo.MT
{
	private String[] ids = {"IDL:cougaar/MT:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = org.cougaar.lib.quo.MTOperations.class;
	public void rerouteMessage(byte[] m) throws org.cougaar.lib.quo.CorbaMisdeliveredMessage
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "rerouteMessage", true);
				org.cougaar.lib.quo.bytesHelper.write(_os,m);
				_is = _invoke(_os);
				return;
			}
			catch( org.omg.CORBA.portable.RemarshalException _rx ){}
			catch( org.omg.CORBA.portable.ApplicationException _ax )
			{
				String _id = _ax.getId();
				if( _id.equals("IDL:cougaar/CorbaMisdeliveredMessage:1.0"))
				{
					throw org.cougaar.lib.quo.CorbaMisdeliveredMessageHelper.read(_ax.getInputStream());
				}
				else 
					throw new RuntimeException("Unexpected exception " + _id );
			}
			finally
			{
				this._releaseReply(_is);
			}
		}
		else
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "rerouteMessage", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			MTOperations _localServant = (MTOperations)_so.servant;
			try
			{
			_localServant.rerouteMessage(m);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

}
