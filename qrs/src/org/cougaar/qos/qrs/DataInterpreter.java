/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public interface DataInterpreter
{
    public double getCredibility(Object x);
    public DataValue getDataValue(Object x);
}
