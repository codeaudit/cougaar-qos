package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/QualifierKindHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// Only a few kinds of qualifiers so far
abstract public class QualifierKindHelper
{
  private static String  _id = "IDL:org/cougaar/qos/ResourceStatus/QualifierKind:1.0";

  public static void insert (org.omg.CORBA.Any a, org.cougaar.qos.ResourceStatus.QualifierKind that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.cougaar.qos.ResourceStatus.QualifierKind extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (org.cougaar.qos.ResourceStatus.QualifierKindHelper.id (), "QualifierKind", new String[] { "min_delta", "min_credibility", "exceeds_threshold", "crosses_threshold", "every", "some"} );
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.cougaar.qos.ResourceStatus.QualifierKind read (org.omg.CORBA.portable.InputStream istream)
  {
    return org.cougaar.qos.ResourceStatus.QualifierKind.from_int (istream.read_long ());
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.cougaar.qos.ResourceStatus.QualifierKind value)
  {
    ostream.write_long (value.value ());
  }

}
