package org.cougaar.qos.ResourceStatus;

/**
* org/cougaar/qos/ResourceStatus/QualifierHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
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
