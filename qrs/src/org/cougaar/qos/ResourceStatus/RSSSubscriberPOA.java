package org.cougaar.qos.ResourceStatus;


/**
* com/bbn/ResourceStatus/RSSSubscriberPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/


// to the RSS should implement this.
public abstract class RSSSubscriberPOA extends org.omg.PortableServer.Servant
 implements org.cougaar.qos.ResourceStatus.RSSSubscriberOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("dataUpdate", new java.lang.Integer (0));
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
       case 0:  // com/bbn/ResourceStatus/RSSSubscriber/dataUpdate
       {
         int callback_id = in.read_long ();
         org.cougaar.qos.ResourceStatus.DataValue value = org.cougaar.qos.ResourceStatus.DataValueHelper.read (in);
         this.dataUpdate (callback_id, value);
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
    "IDL:com/bbn/ResourceStatus/RSSSubscriber:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public RSSSubscriber _this() 
  {
    return RSSSubscriberHelper.narrow(
    super._this_object());
  }

  public RSSSubscriber _this(org.omg.CORBA.ORB orb) 
  {
    return RSSSubscriberHelper.narrow(
    super._this_object(orb));
  }


} // class RSSSubscriberPOA
