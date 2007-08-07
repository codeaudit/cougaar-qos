package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/MinDeltaQualifierFactoryHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// numeric values.
abstract public class MinDeltaQualifierFactoryHelper
{
  private static String  _id = "IDL:org/cougaar/qos/ResourceStatus/MinDeltaQualifierFactory:1.0";

  public static void insert (org.omg.CORBA.Any a, org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactoryHelper.id (), "MinDeltaQualifierFactory");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_MinDeltaQualifierFactoryStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory)
      return (org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      org.cougaar.qos.ResourceStatus._MinDeltaQualifierFactoryStub stub = new org.cougaar.qos.ResourceStatus._MinDeltaQualifierFactoryStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory)
      return (org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactory)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      org.cougaar.qos.ResourceStatus._MinDeltaQualifierFactoryStub stub = new org.cougaar.qos.ResourceStatus._MinDeltaQualifierFactoryStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
