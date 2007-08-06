package org.cougaar.qos.ResourceStatus;


/**
* com/bbn/ResourceStatus/QualifierHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/

abstract public class QualifierHelper
{
  private static String  _id = "IDL:com/bbn/ResourceStatus/Qualifier:1.0";

  public static void insert (org.omg.CORBA.Any a, org.cougaar.qos.ResourceStatus.Qualifier that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.cougaar.qos.ResourceStatus.Qualifier extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (org.cougaar.qos.ResourceStatus.QualifierHelper.id (), "Qualifier");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.cougaar.qos.ResourceStatus.Qualifier read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_QualifierStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.cougaar.qos.ResourceStatus.Qualifier value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static org.cougaar.qos.ResourceStatus.Qualifier narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.cougaar.qos.ResourceStatus.Qualifier)
      return (org.cougaar.qos.ResourceStatus.Qualifier)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      org.cougaar.qos.ResourceStatus._QualifierStub stub = new org.cougaar.qos.ResourceStatus._QualifierStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static org.cougaar.qos.ResourceStatus.Qualifier unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.cougaar.qos.ResourceStatus.Qualifier)
      return (org.cougaar.qos.ResourceStatus.Qualifier)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      org.cougaar.qos.ResourceStatus._QualifierStub stub = new org.cougaar.qos.ResourceStatus._QualifierStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
