package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/_EveryQualifierFactoryStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// the  set are satisfied
public class _EveryQualifierFactoryStub extends org.omg.CORBA.portable.ObjectImpl implements org.cougaar.qos.ResourceStatus.EveryQualifierFactory
{

  public org.cougaar.qos.ResourceStatus.Qualifier getQualifier (org.cougaar.qos.ResourceStatus.Qualifier[] qualifiers)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("getQualifier", true);
                org.cougaar.qos.ResourceStatus.QualifierSetHelper.write ($out, qualifiers);
                $in = _invoke ($out);
                org.cougaar.qos.ResourceStatus.Qualifier $result = org.cougaar.qos.ResourceStatus.QualifierHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return getQualifier (qualifiers        );
            } finally {
                _releaseReply ($in);
            }
  } // getQualifier

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:org/cougaar/qos/ResourceStatus/EveryQualifierFactory:1.0", 
    "IDL:org/cougaar/qos/ResourceStatus/QualifierFactory:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.Object obj = org.omg.CORBA.ORB.init (args, props).string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     String str = org.omg.CORBA.ORB.init (args, props).object_to_string (this);
     s.writeUTF (str);
  }
} // class _EveryQualifierFactoryStub
