package org.cougaar.qos.ResourceStatus;


/**
* com/bbn/ResourceStatus/ResourceStatusServicePOATie.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Volumes/Data/Projects/quo/idl/rss.idl
* Monday, August 6, 2007 12:12:54 PM EDT
*/


// syntactic problem with the argument.
public class ResourceStatusServicePOATie extends ResourceStatusServicePOA
{

  // Constructors

  public ResourceStatusServicePOATie ( org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations delegate ) {
      this._impl = delegate;
  }
  public ResourceStatusServicePOATie ( org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations delegate , org.omg.PortableServer.POA poa ) {
      this._impl = delegate;
      this._poa      = poa;
  }
  public org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations _delegate() {
      return this._impl;
  }
  public void _delegate (org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations delegate ) {
      this._impl = delegate;
  }
  public org.omg.PortableServer.POA _default_POA() {
      if(_poa != null) {
          return _poa;
      }
      else {
          return super._default_POA();
      }
  }

  // This call is non-blocking by design.
  public boolean query (org.cougaar.qos.ResourceStatus.ResourceNode[] formula, org.cougaar.qos.ResourceStatus.DataValueHolder result)
  {
    return _impl.query(formula, result);
  } // query

  public boolean query_s (String formula, org.cougaar.qos.ResourceStatus.DataValueHolder result) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
    return _impl.query_s(formula, result);
  } // query_s


  // an exception in the former case.
  public boolean blockingQuery (org.cougaar.qos.ResourceStatus.ResourceNode[] formula, long timeout, org.cougaar.qos.ResourceStatus.DataValueHolder result)
  {
    return _impl.blockingQuery(formula, timeout, result);
  } // blockingQuery

  public boolean blockingQuery_s (String formula, long timeout, org.cougaar.qos.ResourceStatus.DataValueHolder result) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
    return _impl.blockingQuery_s(formula, timeout, result);
  } // blockingQuery_s


  // one corresponds to a given callback invocation.
  public boolean unqualifiedSubscribe (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, org.cougaar.qos.ResourceStatus.ResourceNode[] formula, int callback_id)
  {
    return _impl.unqualifiedSubscribe(listener, formula, callback_id);
  } // unqualifiedSubscribe

  public boolean unqualifiedSubscribe_s (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, String formula, int callback_id) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
    return _impl.unqualifiedSubscribe_s(listener, formula, callback_id);
  } // unqualifiedSubscribe_s


  // qualifier.
  public boolean qualifiedSubscribe (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, org.cougaar.qos.ResourceStatus.ResourceNode[] formula, int callback_id, org.cougaar.qos.ResourceStatus.Qualifier qualifier_)
  {
    return _impl.qualifiedSubscribe(listener, formula, callback_id, qualifier_);
  } // qualifiedSubscribe

  public boolean qualifiedSubscribe_s (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, String formula, int callback_id, org.cougaar.qos.ResourceStatus.Qualifier qualifier_) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
    return _impl.qualifiedSubscribe_s(listener, formula, callback_id, qualifier_);
  } // qualifiedSubscribe_s


  // given listener.
  public void unsubscribe (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, org.cougaar.qos.ResourceStatus.ResourceNode[] formula)
  {
    _impl.unsubscribe(listener, formula);
  } // unsubscribe

  public void unsubscribe_s (org.cougaar.qos.ResourceStatus.RSSSubscriber listener, String formula) throws org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
    _impl.unsubscribe_s(listener, formula);
  } // unsubscribe_s


  // request a Qualifier.
  public org.cougaar.qos.ResourceStatus.QualifierFactory getQualifierFactory (org.cougaar.qos.ResourceStatus.QualifierKind kind)
  {
    return _impl.getQualifierFactory(kind);
  } // getQualifierFactory


  // context rather than a formula.
  public boolean invoke (org.cougaar.qos.ResourceStatus.ResourceNode[] resource_context, String method_name, String[] args) throws org.cougaar.qos.ResourceStatus.NoSuchMethodException
  {
    return _impl.invoke(resource_context, method_name, args);
  } // invoke

  public boolean invoke_s (String resource_context, String method_name, String[] args) throws org.cougaar.qos.ResourceStatus.NoSuchMethodException, org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException
  {
    return _impl.invoke_s(resource_context, method_name, args);
  } // invoke_s


  // raw value.
  public void pushString (String key, String raw_value)
  {
    _impl.pushString(key, raw_value);
  } // pushString

  public void pushLong (String key, int raw_value)
  {
    _impl.pushLong(key, raw_value);
  } // pushLong


  // soon go away.
  public void addDependency (org.cougaar.qos.ResourceStatus.ResourceNode resource, org.cougaar.qos.ResourceStatus.ResourceNode[] node_dependencies, org.cougaar.qos.ResourceStatus.ResourceStatusService[] rss_dependencies)
  {
    _impl.addDependency(resource, node_dependencies, rss_dependencies);
  } // addDependency

  private org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations _impl;
  private org.omg.PortableServer.POA _poa;

} // class ResourceStatusServicePOATie
