/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public interface DataInterpreter<T> {
    public double getCredibility(T x);

    public DataValue getDataValue(T x);
}
