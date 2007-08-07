package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/QualifierPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/

public abstract class QualifierPOA extends org.omg.PortableServer.Servant
 implements org.cougaar.qos.ResourceStatus.QualifierOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("getAttribute", new java.lang.Integer (0));
    _methods.put ("setAttribute", new java.lang.Integer (1));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // org/cougaar/qos/ResourceStatus/Qualifier/getAttribute
       {
         try {
           String attribute_name = in.read_string ();
           org.cougaar.qos.ResourceStatus.data_valueHolder attribute_value = new org.cougaar.qos.ResourceStatus.data_valueHolder ();
           this.getAttribute (attribute_name, attribute_value);
           out = $rh.createReply();
           org.cougaar.qos.ResourceStatus.data_valueHelper.write (out, attribute_value.value);
         } catch (org.cougaar.qos.ResourceStatus.NoSuchAttributeException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.NoSuchAttributeExceptionHelper.write (out, $ex);
         } catch (org.cougaar.qos.ResourceStatus.BadAttributeValueException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.BadAttributeValueExceptionHelper.write (out, $ex);
         }
         break;
       }

       case 1:  // org/cougaar/qos/ResourceStatus/Qualifier/setAttribute
       {
         try {
           String attribute_name = in.read_string ();
           org.cougaar.qos.ResourceStatus.data_value attrobute_value = org.cougaar.qos.ResourceStatus.data_valueHelper.read (in);
           this.setAttribute (attribute_name, attrobute_value);
           out = $rh.createReply();
         } catch (org.cougaar.qos.ResourceStatus.NoSuchAttributeException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.NoSuchAttributeExceptionHelper.write (out, $ex);
         } catch (org.cougaar.qos.ResourceStatus.BadAttributeValueException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.BadAttributeValueExceptionHelper.write (out, $ex);
         }
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:org/cougaar/qos/ResourceStatus/Qualifier:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Qualifier _this() 
  {
    return QualifierHelper.narrow(
    super._this_object());
  }

  public Qualifier _this(org.omg.CORBA.ORB orb) 
  {
    return QualifierHelper.narrow(
    super._this_object(orb));
  }


} // class QualifierPOA
