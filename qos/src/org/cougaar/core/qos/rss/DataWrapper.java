/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.core.qos.rss;

import com.bbn.quo.data.DataValue;
import org.cougaar.core.qos.metrics.Metric;

public class DataWrapper implements Metric, java.io.Serializable
{
    private DataValue data;

    public DataWrapper() {
    }

    public DataWrapper(DataValue data) {
	this.data = data;
    }

    public String toString() {
	return data.toString();
    }

    public DataValue getDataValue() {
	return data;
    }

    public String stringValue() { return data.getStringValue(); }
    public byte byteValue() { return data.getByteValue(); }
    public short shortValue() { return data.getShortValue(); }
    public int intValue() { return data.getIntValue(); }
    public long longValue() { return data.getLongValue(); }
    public float floatValue() { return data.getFloatValue(); }
    public double doubleValue() { return data.getDoubleValue(); }
    public char charValue() { return data.getCharValue(); }
    public boolean booleanValue() { return data.getBooleanValue(); }

    public Object getRawValue() { return data.getRawValue(); }
    public double getCredibility() { return data.getCredibility(); }
    public String getUnits() { return data.getUnits(); }
    public String getProvenance() { return data.getProvenance(); }
    public long getTimestamp() { return data.getTimestamp(); }
    public long getHalflife() { return data.getHalflife(); }
}
