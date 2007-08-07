package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/NoSuchMethodException.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/

public final class NoSuchMethodException extends org.omg.CORBA.UserException
{
  public String method_name = null;

  public NoSuchMethodException ()
  {
    super(NoSuchMethodExceptionHelper.id());
  } // ctor

  public NoSuchMethodException (String _method_name)
  {
    super(NoSuchMethodExceptionHelper.id());
    method_name = _method_name;
  } // ctor


  public NoSuchMethodException (String $reason, String _method_name)
  {
    super(NoSuchMethodExceptionHelper.id() + "  " + $reason);
    method_name = _method_name;
  } // ctor

} // class NoSuchMethodException
