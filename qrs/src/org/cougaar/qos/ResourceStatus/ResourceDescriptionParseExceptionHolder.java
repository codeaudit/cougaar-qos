package org.cougaar.qos.ResourceStatus;

/**
* com/bbn/ResourceStatus/ResourceDescriptionParseExceptionHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/

public final class ResourceDescriptionParseExceptionHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException value = null;

  public ResourceDescriptionParseExceptionHolder ()
  {
  }

  public ResourceDescriptionParseExceptionHolder (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.type ();
  }

}
