package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/_ResourceStatusServiceStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// syntactic problem with the argument.
public class _ResourceStatusServiceStub extends org.omg.CORBA.portable.ObjectImpl implements org.cougaar.qos.ResourceStatus.ResourceStatusService
{


  // This call is non-blocking by design.
  public boolean query (org.cougaar.qos.ResourceStatus.ResourceNode[] formula, org.cougaar.qos.ResourceStatus.DataValueHolder result)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("query", true);
                org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.write ($out, formula);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                result.value = org.cougaar.qos.ResourceStatus.DataValueHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return query (formula, result        );
            } finally {
                _releaseReply ($in);
            }
  } // query

  public boolean query_s (String formula, org.cougaar.qos.ResourceStatus.DataValueHolder result) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("query_s", true);
                $out.write_string (formula);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                result.value = org.cougaar.qos.ResourceStatus.DataValueHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return query_s (formula, result        );
            } finally {
                _releaseReply ($in);
            }
  } // query_s


  // an exception in the former case.
  public boolean blockingQuery (org.cougaar.qos.ResourceStatus.ResourceNode[] formula, long timeout, org.cougaar.qos.ResourceStatus.DataValueHolder result)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("blockingQuery", true);
                org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.write ($out, formula);
                $out.write_longlong (timeout);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                result.value = org.cougaar.qos.ResourceStatus.DataValueHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return blockingQuery (formula, timeout, result        );
            } finally {
                _releaseReply ($in);
            }
  } // blockingQuery

  public boolean blockingQuery_s (String formula, long timeout, org.cougaar.qos.ResourceStatus.DataValueHolder result) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("blockingQuery_s", true);
                $out.write_string (formula);
                $out.write_longlong (timeout);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                result.value = org.cougaar.qos.ResourceStatus.DataValueHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return blockingQuery_s (formula, timeout, result        );
            } finally {
                _releaseReply ($in);
            }
  } // blockingQuery_s


  // one corresponds to a given callback invocation.
  public boolean unqualifiedSubscribe (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, org.cougaar.qos.ResourceStatus.ResourceNode[] formula, int callback_id)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("unqualifiedSubscribe", true);
                org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.write ($out, listener);
                org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.write ($out, formula);
                $out.write_long (callback_id);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return unqualifiedSubscribe (listener, formula, callback_id        );
            } finally {
                _releaseReply ($in);
            }
  } // unqualifiedSubscribe

  public boolean unqualifiedSubscribe_s (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, String formula, int callback_id) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("unqualifiedSubscribe_s", true);
                org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.write ($out, listener);
                $out.write_string (formula);
                $out.write_long (callback_id);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return unqualifiedSubscribe_s (listener, formula, callback_id        );
            } finally {
                _releaseReply ($in);
            }
  } // unqualifiedSubscribe_s


  // qualifier.
  public boolean qualifiedSubscribe (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, org.cougaar.qos.ResourceStatus.ResourceNode[] formula, int callback_id, org.cougaar.qos.ResourceStatus.Qualifier qualifier_)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("qualifiedSubscribe", true);
                org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.write ($out, listener);
                org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.write ($out, formula);
                $out.write_long (callback_id);
                org.cougaar.qos.ResourceStatus.QualifierHelper.write ($out, qualifier_);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return qualifiedSubscribe (listener, formula, callback_id, qualifier_        );
            } finally {
                _releaseReply ($in);
            }
  } // qualifiedSubscribe

  public boolean qualifiedSubscribe_s (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, String formula, int callback_id, org.cougaar.qos.ResourceStatus.Qualifier qualifier_) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("qualifiedSubscribe_s", true);
                org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.write ($out, listener);
                $out.write_string (formula);
                $out.write_long (callback_id);
                org.cougaar.qos.ResourceStatus.QualifierHelper.write ($out, qualifier_);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return qualifiedSubscribe_s (listener, formula, callback_id, qualifier_        );
            } finally {
                _releaseReply ($in);
            }
  } // qualifiedSubscribe_s


  // given listener.
  public void unsubscribe (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, org.cougaar.qos.ResourceStatus.ResourceNode[] formula)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("unsubscribe", true);
                org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.write ($out, listener);
                org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.write ($out, formula);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                unsubscribe (listener, formula        );
            } finally {
                _releaseReply ($in);
            }
  } // unsubscribe

  public void unsubscribe_s (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, String formula) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("unsubscribe_s", true);
                org.cougaar.qos.ResourceStatus.RSSSubscriberHelper.write ($out, listener);
                $out.write_string (formula);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                unsubscribe_s (listener, formula        );
            } finally {
                _releaseReply ($in);
            }
  } // unsubscribe_s


  // request a Qualifier.
  public org.cougaar.qos.ResourceStatus.QualifierFactory getQualifierFactory (org.cougaar.qos.ResourceStatus.QualifierKind kind)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("getQualifierFactory", true);
                org.cougaar.qos.ResourceStatus.QualifierKindHelper.write ($out, kind);
                $in = _invoke ($out);
                org.cougaar.qos.ResourceStatus.QualifierFactory $result = org.cougaar.qos.ResourceStatus.QualifierFactoryHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return getQualifierFactory (kind        );
            } finally {
                _releaseReply ($in);
            }
  } // getQualifierFactory


  // context rather than a formula.
  public boolean invoke (org.cougaar.qos.ResourceStatus.ResourceNode[] resource_context, String method_name, String[] args) throws org.cougaar.qos.ResourceStatus.NoSuchMethodException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("invoke", true);
                org.cougaar.qos.ResourceStatus.ResourceDescriptionHelper.write ($out, resource_context);
                $out.write_string (method_name);
                org.cougaar.qos.ResourceStatus.ParameterListHelper.write ($out, args);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/NoSuchMethodException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.NoSuchMethodExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return invoke (resource_context, method_name, args        );
            } finally {
                _releaseReply ($in);
            }
  } // invoke

  public boolean invoke_s (String resource_context, String method_name, String[] args) throws org.cougaar.qos.ResourceStatus.NoSuchMethodException, org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("invoke_s", true);
                $out.write_string (resource_context);
                $out.write_string (method_name);
                org.cougaar.qos.ResourceStatus.ParameterListHelper.write ($out, args);
                $in = _invoke ($out);
                boolean $result = $in.read_boolean ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/NoSuchMethodException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.NoSuchMethodExceptionHelper.read ($in);
                else if (_id.equals ("IDL:org/cougaar/qos/ResourceStatus/ResourceDescriptionParseException:1.0"))
                    throw org.cougaar.qos.ResourceStatus.ResourceDescriptionParseExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return invoke_s (resource_context, method_name, args        );
            } finally {
                _releaseReply ($in);
            }
  } // invoke_s


  // raw value.
  public void pushString (String key, String raw_value)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("pushString", true);
                $out.write_string (key);
                $out.write_string (raw_value);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                pushString (key, raw_value        );
            } finally {
                _releaseReply ($in);
            }
  } // pushString

  public void pushLong (String key, int raw_value)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("pushLong", true);
                $out.write_string (key);
                $out.write_long (raw_value);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                pushLong (key, raw_value        );
            } finally {
                _releaseReply ($in);
            }
  } // pushLong


  // soon go away.
  public void addDependency (org.cougaar.qos.ResourceStatus.ResourceNode resource, org.cougaar.qos.ResourceStatus.ResourceNode[] node_dependencies, org.cougaar.qos.ResourceStatus.ResourceStatusService[] rss_dependencies)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("addDependency", true);
                org.cougaar.qos.ResourceStatus.ResourceNodeHelper.write ($out, resource);
                org.cougaar.qos.ResourceStatus.ResourceNodeSeqHelper.write ($out, node_dependencies);
                org.cougaar.qos.ResourceStatus.RSSSeqHelper.write ($out, rss_dependencies);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                addDependency (resource, node_dependencies, rss_dependencies        );
            } finally {
                _releaseReply ($in);
            }
  } // addDependency

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:org/cougaar/qos/ResourceStatus/ResourceStatusService:1.0"};

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
} // class _ResourceStatusServiceStub
