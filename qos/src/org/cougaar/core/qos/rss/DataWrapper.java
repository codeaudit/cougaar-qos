/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Metric;

import com.bbn.rss.DataValue;

public class DataWrapper implements Metric, java.io.Serializable
{
    private DataValue data;

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
