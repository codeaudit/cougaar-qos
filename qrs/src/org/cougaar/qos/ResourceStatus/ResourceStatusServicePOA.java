package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/ResourceStatusServicePOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// syntactic problem with the argument.
public abstract class ResourceStatusServicePOA extends org.omg.PortableServer.Servant
 implements org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("query", new java.lang.Integer (0));
    _methods.put ("query_s", new java.lang.Integer (1));
    _methods.put ("blockingQuery", new java.lang.Integer (2));
    _methods.put ("blockingQuery_s", new java.lang.Integer (3));
    _methods.put ("unqualifiedSubscribe", new java.lang.Integer (4));
    _methods.put ("unqualifiedSubscribe_s", new java.lang.Integer (5));
    _methods.put ("qualifiedSubscribe", new java.lang.Integer (6));
    _methods.put ("qualifiedSubscribe_s", new java.lang.Integer (7));
    _methods.put ("unsubscribe", new java.lang.Integer (8));
    _methods.put ("unsubscribe_s", new java.lang.Integer (9));
    _methods.put ("getQualifierFactory", new java.lang.Integer (10));
    _methods.put ("invoke", new java.lang.Integer (11));
    _methods.put ("invoke_s", new java.lang.Integer (12));
    _methods.put ("pushString", new java.lang.Integer (13));
    _methods.put ("pushLong", new java.lang.Integer (14));
    _methods.put ("addDependency", new java.lang.Integer (15));
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

  // This call is non-blocking by design.
       case 0:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/query
       {
         org.cougaar.qos.ResourceStatus.ResourceNode formula[] = org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.read (in);
         org.cougaar.qos.ResourceStatus.DataValueHolder result = new org.cougaar.qos.ResourceStatus.DataValueHolder ();
         boolean $result = false;
         $result = this.query (formula, result);
         out = $rh.createReply();
         out.write_boolean ($result);
         org.cougaar.qos.ResourceStatus.DataValueHelper.write (out, result.value);
         break;
       }

       case 1:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/query_s
       {
         try {
           String formula = in.read_string ();
           org.cougaar.qos.ResourceStatus.DataValueHolder result = new org.cougaar.qos.ResourceStatus.DataValueHolder ();
           boolean $result = false;
           $result = this.query_s (formula, result);
           out = $rh.createReply();
           out.write_boolean ($result);
           org.cougaar.qos.ResourceStatus.DataValueHelper.write (out, result.value);
         } catch (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (out, $ex);
         }
         break;
       }


  // an exception in the former case.
       case 2:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/blockingQuery
       {
         org.cougaar.qos.ResourceStatus.ResourceNode formula[] = org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.read (in);
         long timeout = in.read_longlong ();
         org.cougaar.qos.ResourceStatus.DataValueHolder result = new org.cougaar.qos.ResourceStatus.DataValueHolder ();
         boolean $result = false;
         $result = this.blockingQuery (formula, timeout, result);
         out = $rh.createReply();
         out.write_boolean ($result);
         org.cougaar.qos.ResourceStatus.DataValueHelper.write (out, result.value);
         break;
       }

       case 3:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/blockingQuery_s
       {
         try {
           String formula = in.read_string ();
           long timeout = in.read_longlong ();
           org.cougaar.qos.ResourceStatus.DataValueHolder result = new org.cougaar.qos.ResourceStatus.DataValueHolder ();
           boolean $result = false;
           $result = this.blockingQuery_s (formula, timeout, result);
           out = $rh.createReply();
           out.write_boolean ($result);
           org.cougaar.qos.ResourceStatus.DataValueHelper.write (out, result.value);
         } catch (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (out, $ex);
         }
         break;
       }


  // one corresponds to a given callback invocation.
       case 4:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/unqualifiedSubscribe
       {
         org.cougaar.qos.ResourceStatus.RSSSubscriber listener = org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.read (in);
         org.cougaar.qos.ResourceStatus.ResourceNode formula[] = org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.read (in);
         int callback_id = in.read_long ();
         boolean $result = false;
         $result = this.unqualifiedSubscribe (listener, formula, callback_id);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 5:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/unqualifiedSubscribe_s
       {
         try {
           org.cougaar.qos.ResourceStatus.RSSSubscriber listener = org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.read (in);
           String formula = in.read_string ();
           int callback_id = in.read_long ();
           boolean $result = false;
           $result = this.unqualifiedSubscribe_s (listener, formula, callback_id);
           out = $rh.createReply();
           out.write_boolean ($result);
         } catch (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (out, $ex);
         }
         break;
       }


  // qualifier.
       case 6:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/qualifiedSubscribe
       {
         org.cougaar.qos.ResourceStatus.RSSSubscriber listener = org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.read (in);
         org.cougaar.qos.ResourceStatus.ResourceNode formula[] = org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.read (in);
         int callback_id = in.read_long ();
         org.cougaar.qos.ResourceStatus.Qualifier qualifier_ = org.cougaar.qos.ResourceStatus.QualifierHelper.read (in);
         boolean $result = false;
         $result = this.qualifiedSubscribe (listener, formula, callback_id, qualifier_);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 7:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/qualifiedSubscribe_s
       {
         try {
           org.cougaar.qos.ResourceStatus.RSSSubscriber listener = org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.read (in);
           String formula = in.read_string ();
           int callback_id = in.read_long ();
           org.cougaar.qos.ResourceStatus.Qualifier qualifier_ = org.cougaar.qos.ResourceStatus.QualifierHelper.read (in);
           boolean $result = false;
           $result = this.qualifiedSubscribe_s (listener, formula, callback_id, qualifier_);
           out = $rh.createReply();
           out.write_boolean ($result);
         } catch (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (out, $ex);
         }
         break;
       }


  // given listener.
       case 8:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/unsubscribe
       {
         org.cougaar.qos.ResourceStatus.RSSSubscriber listener = org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.read (in);
         org.cougaar.qos.ResourceStatus.ResourceNode formula[] = org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.read (in);
         this.unsubscribe (listener, formula);
         out = $rh.createReply();
         break;
       }

       case 9:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/unsubscribe_s
       {
         try {
           org.cougaar.qos.ResourceStatus.RSSSubscriber listener = org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.read (in);
           String formula = in.read_string ();
           this.unsubscribe_s (listener, formula);
           out = $rh.createReply();
         } catch (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (out, $ex);
         }
         break;
       }


  // request a Qualifier.
       case 10:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/getQualifierFactory
       {
         org.cougaar.qos.ResourceStatus.QualifierKind kind = org.cougaar.qos.ResourceStatus.QualifierKindHelper.read (in);
         org.cougaar.qos.ResourceStatus.QualifierFactory $result = null;
         $result = this.getQualifierFactory (kind);
         out = $rh.createReply();
         org.cougaar.qos.ResourceStatus.QualifierFactoryHelper.write (out, $result);
         break;
       }


  // context rather than a formula.
       case 11:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/invoke
       {
         try {
           org.cougaar.qos.ResourceStatus.ResourceNode resource_context[] = org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.read (in);
           String method_name = in.read_string ();
           String args[] = org.cougaar.qos.ResourceStatus.ParameterListHelper.read (in);
           boolean $result = false;
           $result = this.invoke (resource_context, method_name, args);
           out = $rh.createReply();
           out.write_boolean ($result);
         } catch (org.cougaar.qos.ResourceStatus.NoSuchMethodException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.NoSuchMethodExceptionHelper.write (out, $ex);
         }
         break;
       }

       case 12:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/invoke_s
       {
         try {
           String resource_context = in.read_string ();
           String method_name = in.read_string ();
           String args[] = org.cougaar.qos.ResourceStatus.ParameterListHelper.read (in);
           boolean $result = false;
           $result = this.invoke_s (resource_context, method_name, args);
           out = $rh.createReply();
           out.write_boolean ($result);
         } catch (org.cougaar.qos.ResourceStatus.NoSuchMethodException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.NoSuchMethodExceptionHelper.write (out, $ex);
         } catch (org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException $ex) {
           out = $rh.createExceptionReply ();
           org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.write (out, $ex);
         }
         break;
       }


  // raw value.
       case 13:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/pushString
       {
         String key = in.read_string ();
         String raw_value = in.read_string ();
         this.pushString (key, raw_value);
         out = $rh.createReply();
         break;
       }

       case 14:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/pushLong
       {
         String key = in.read_string ();
         int raw_value = in.read_long ();
         this.pushLong (key, raw_value);
         out = $rh.createReply();
         break;
       }


  // soon go away.
       case 15:  // org/cougaar/qos/ResourceStatus/ResourceStatusService/addDependency
       {
         org.cougaar.qos.ResourceStatus.ResourceNode resource = org.cougaar.qos.ResourceStatus.ResourceNodeHelper.read (in);
         org.cougaar.qos.ResourceStatus.ResourceNode node_dependencies[] = org.cougaar.qos.ResourceStatus.ResourceNodeSeqHelper.read (in);
         org.cougaar.qos.ResourceStatus.ResourceStatusService rss_dependencies[] = org.cougaar.qos.ResourceStatus.RSSSeqHelper.read (in);
         this.addDependency (resource, node_dependencies, rss_dependencies);
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:org/cougaar/qos/ResourceStatus/ResourceStatusService:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public ResourceStatusService _this() 
  {
    return ResourceStatusServiceHelper.narrow(
    super._this_object());
  }

  public ResourceStatusService _this(org.omg.CORBA.ORB orb) 
  {
    return ResourceStatusServiceHelper.narrow(
    super._this_object(orb));
  }


} // class ResourceStatusServicePOA