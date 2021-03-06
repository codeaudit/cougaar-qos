package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/

public final class ResourceDescriptionParseException extends org.omg.CORBA.UserException
{
  /**
    * 
    */
   private static final long serialVersionUID = 1L;
public String description = null;
  public String reason = null;

  public ResourceDescriptionParseException ()
  {
    super(ResourceDescriptionParseExceptionHelper.id());
  } // ctor

  public ResourceDescriptionParseException (String _description, String _reason)
  {
    super(ResourceDescriptionParseExceptionHelper.id());
    description = _description;
    reason = _reason;
  } // ctor


  public ResourceDescriptionParseException (String $reason, String _description, String _reason)
  {
    super(ResourceDescriptionParseExceptionHelper.id() + "  " + $reason);
    description = _description;
    reason = _reason;
  } // ctor

} // class ResourceDescriptionParseException
