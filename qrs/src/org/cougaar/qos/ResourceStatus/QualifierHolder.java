package org.cougaar.qos.ResourceStatus;

/**
* com/bbn/ResourceStatus/QualifierHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/

public final class QualifierHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.Qualifier value = null;

  public QualifierHolder ()
  {
  }

  public QualifierHolder (org.cougaar.qos.ResourceStatus.Qualifier initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.QualifierHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.QualifierHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.QualifierHelper.type ();
  }

}
