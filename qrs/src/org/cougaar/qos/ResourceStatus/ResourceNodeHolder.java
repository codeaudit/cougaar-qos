package org.cougaar.qos.ResourceStatus;

/**
* com/bbn/ResourceStatus/ResourceNodeHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/

public final class ResourceNodeHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.ResourceNode value = null;

  public ResourceNodeHolder ()
  {
  }

  public ResourceNodeHolder (org.cougaar.qos.ResourceStatus.ResourceNode initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.ResourceNodeHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.ResourceNodeHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.ResourceNodeHelper.type ();
  }

}
