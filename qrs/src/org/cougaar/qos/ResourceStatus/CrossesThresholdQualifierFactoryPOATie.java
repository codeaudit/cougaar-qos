package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/CrossesThresholdQualifierFactoryPOATie.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/


// the given threshold.
public class CrossesThresholdQualifierFactoryPOATie extends CrossesThresholdQualifierFactoryPOA
{

  // Constructors

  public CrossesThresholdQualifierFactoryPOATie ( org.cougaar.qos.ResourceStatus.CrossesThresholdQualifierFactoryOperations delegate ) {
      this._impl = delegate;
  }
  public CrossesThresholdQualifierFactoryPOATie ( org.cougaar.qos.ResourceStatus.CrossesThresholdQualifierFactoryOperations delegate , org.omg.PortableServer.POA poa ) {
      this._impl = delegate;
      this._poa      = poa;
  }
  public org.cougaar.qos.ResourceStatus.CrossesThresholdQualifierFactoryOperations _delegate() {
      return this._impl;
  }
  public void _delegate (org.cougaar.qos.ResourceStatus.CrossesThresholdQualifierFactoryOperations delegate ) {
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
  public org.cougaar.qos.ResourceStatus.Qualifier getQualifier (double threshold)
  {
    return _impl.getQualifier(threshold);
  } // getQualifier

  private org.cougaar.qos.ResourceStatus.CrossesThresholdQualifierFactoryOperations _impl;
  private org.omg.PortableServer.POA _poa;

} // class CrossesThresholdQualifierFactoryPOATie
