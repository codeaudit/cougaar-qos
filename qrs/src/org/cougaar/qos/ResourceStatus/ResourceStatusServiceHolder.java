package org.cougaar.qos.ResourceStatus;

/**
* org/cougaar/qos/ResourceStatus/ResourceStatusServiceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// syntactic problem with the argument.
public final class ResourceStatusServiceHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.ResourceStatusService value = null;

  public ResourceStatusServiceHolder ()
  {
  }

  public ResourceStatusServiceHolder (org.cougaar.qos.ResourceStatus.ResourceStatusService initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.ResourceStatusServiceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.ResourceStatusServiceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.ResourceStatusServiceHelper.type ();
  }

}