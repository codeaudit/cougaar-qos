package org.cougaar.qos.ResourceStatus;


/**
* org/cougaar/qos/ResourceStatus/QualifierOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from qrs.idl
* Tuesday, August 7, 2007 11:57:30 AM EDT
*/

public interface QualifierOperations 
{
  void getAttribute (String attribute_name, org.cougaar.qos.ResourceStatus.data_valueHolder attribute_value) throws org.cougaar.qos.ResourceStatus.NoSuchAttributeException, org.cougaar.qos.ResourceStatus.BadAttributeValueException;
  void setAttribute (String attribute_name, org.cougaar.qos.ResourceStatus.data_value attrobute_value) throws org.cougaar.qos.ResourceStatus.NoSuchAttributeException, org.cougaar.qos.ResourceStatus.BadAttributeValueException;
} // interface QualifierOperations
