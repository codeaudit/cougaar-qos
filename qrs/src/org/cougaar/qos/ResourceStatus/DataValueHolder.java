package org.cougaar.qos.ResourceStatus;

/**
* com/bbn/ResourceStatus/DataValueHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/

public final class DataValueHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.DataValue value = null;

  public DataValueHolder ()
  {
  }

  public DataValueHolder (org.cougaar.qos.ResourceStatus.DataValue initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.DataValueHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.DataValueHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.DataValueHelper.type ();
  }

}
