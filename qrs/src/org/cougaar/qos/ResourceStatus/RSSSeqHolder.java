package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/RSSSeqHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/

public final class RSSSeqHolder implements org.omg.CORBA.portable.Streamable
{
  public org.cougaar.qos.ResourceStatus.ResourceStatusService value[] = null;

  public RSSSeqHolder ()
  {
  }

  public RSSSeqHolder (org.cougaar.qos.ResourceStatus.ResourceStatusService[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.cougaar.qos.ResourceStatus.RSSSeqHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.cougaar.qos.ResourceStatus.RSSSeqHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.cougaar.qos.ResourceStatus.RSSSeqHelper.type ();
  }

}
