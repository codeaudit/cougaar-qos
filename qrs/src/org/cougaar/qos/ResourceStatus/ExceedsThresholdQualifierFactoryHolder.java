package org.cougaar.qos.ResourceStatus;

/**
* org/cougaar/qos/ResourceStatus/ExceedsThresholdQualifierFactoryHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// greater than the given threshold.
public final class ExceedsThresholdQualifierFactoryHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.ExceedsThresholdQualifierFactory value = null;

  public ExceedsThresholdQualifierFactoryHolder ()
  {
  }

  public ExceedsThresholdQualifierFactoryHolder (org.cougaar.qos.ResourceStatus.ExceedsThresholdQualifierFactory initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.ExceedsThresholdQualifierFactoryHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.ExceedsThresholdQualifierFactoryHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.ExceedsThresholdQualifierFactoryHelper.type ();
  }

}
