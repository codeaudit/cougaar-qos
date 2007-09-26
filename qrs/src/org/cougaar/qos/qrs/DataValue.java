/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
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

package org.cougaar.qos.qrs;

import org.cougaar.qos.ResourceStatus.data_types;
import org.cougaar.qos.ResourceStatus.data_value;
import org.cougaar.util.log.Logger;

/**
 * A simple struct-like class used to represent the values manipulated by the
 * various DataScope/DataFormula objects.
 */
public class DataValue implements Constants, java.io.Serializable {
    /**
     * Standard instance of a value without any specific credibility. This may
     * move into the Constants interface later.
     */
    public static final DataValue NO_VALUE = new DataValue(0.0, 0.0);

    private Object value;
    private final double credibility;
    private final String units;
    private final String provenance;
    private final long timestamp;
    private final long halflife;

    public DataValue(DataValue value) {
        this(value.value,
             value.credibility,
             value.units,
             value.provenance,
             value.timestamp,
             value.halflife);
    }

    public DataValue(Object value,
                     double credibility,
                     String units,
                     String provenance,
                     long timestamp,
                     long halflife) {
        this.value = value;
        this.credibility = credibility;
        this.units = units;
        this.provenance = provenance;
        this.timestamp = timestamp;
        this.halflife = halflife;
    }

    public DataValue(double value, double credibility, String units, String provenance) {
        this(new Double(value), credibility, units, provenance);
    }

    public DataValue(long value, double credibility, String units, String provenance) {
        this(new Long(value), credibility, units, provenance);
    }

    public DataValue(char value, double credibility, String units, String provenance) {
        this(new Character(value), credibility, units, provenance);
    }

    public DataValue(boolean value, double credibility, String units, String provenance) {
        this(value ? Boolean.TRUE : Boolean.FALSE, credibility, units, provenance);
    }

    public DataValue(org.cougaar.qos.ResourceStatus.DataValue value) {
        data_value v = value.value;
        switch (v.discriminator().value()) {
            case data_types._boolean_data:
                this.value = v.b_value() ? Boolean.TRUE : Boolean.FALSE;
                break;

            case data_types._string_data:
                this.value = v.s_value();
                break;

            case data_types._number_data:
                this.value = new Double(v.d_value());
                break;
        }
        this.credibility = value.credibility;
        this.units = value.units;
        this.provenance = value.provenance;
        this.timestamp = value.timestamp;
        this.halflife = value.halflife;
    }

    public DataValue(Object value, double credibility, String units, String provenance) {
        this(value, credibility, units, provenance, System.currentTimeMillis(), 0);
    }

    /**
     * Use value of 0.0 and default credibility (from Constants).
     */
    public DataValue() {
        this(0.0);
    }

    public DataValue(double value, double credibility) {
        this(new Double(value), credibility, null, null);
    }

    public DataValue(double value) {
        this(new Double(value), DEFAULT_CREDIBILITY, null, null);
    }

    public DataValue(long value, double credibility) {
        this(new Long(value), credibility, null, null);
    }

    public DataValue(long value) {
        this(new Long(value), DEFAULT_CREDIBILITY, null, null);
    }

    public DataValue(String value, double credibility) {
        this(value, credibility, null, null);
    }

    public DataValue(String value) {
        this(value, DEFAULT_CREDIBILITY, null, null);
    }

    public DataValue(char value, double credibility) {
        this(new Character(value), credibility, null, null);
    }

    public DataValue(char value) {
        this(new Character(value), DEFAULT_CREDIBILITY, null, null);
    }

    public DataValue(boolean value, double credibility) {
        this(value, credibility, null, null);
    }

    public DataValue(boolean value) {
        this(value, DEFAULT_CREDIBILITY, null, null);
    }

    public DataValue newDataValue(Object raw) {
        return new DataValue(raw, credibility, units, provenance, timestamp, halflife);
    }

    public DataValue newCredibility(double new_credibility) {
        return new DataValue(value, new_credibility, units, provenance, timestamp, halflife);
    }

    public String toString() {
        return "<" + value + ":" + credibility + ">";
    }

    public org.cougaar.qos.ResourceStatus.DataValue getCorbaValue() {
        data_value val = new data_value();
        if (value instanceof String) {
            val.s_value((String) value);
        } else if (value instanceof Character) {
            val.s_value(((Character) value).toString());
        } else if (value instanceof Double) {
            val.d_value(((Double) value).doubleValue());
        } else if (value instanceof Long) {
            val.d_value(((Long) value).longValue());
        } else if (value instanceof Boolean) {
            val.b_value(((Boolean) value).booleanValue());
        } else {
            Logger logger = Logging.getLogger(DataValue.class);
            logger.error("Value is weird " + val);
        }
        return new org.cougaar.qos.ResourceStatus.DataValue(timestamp,
                                                            halflife,
                                                            credibility,
                                                            (units != null ? units : ""),
                                                            (provenance != null ? provenance : ""),
                                                            val);
    }

    // Irrespective of timestamp
    public boolean contentsEquals(DataValue candidate) {
        if (candidate == null) {
            return false;
        }
        if (candidate == this) {
            return true;
        }
        return (value == null && candidate.value == null || value != null
                && value.equals(candidate.value))
                && credibility == candidate.credibility
                && (units == null && candidate.units == null || units != null
                        && units.equals(candidate.units))
                && (provenance == null && candidate.provenance == null || provenance != null
                        && provenance.equals(candidate.provenance))
                && halflife == candidate.halflife;
    }

    public boolean equals(DataValue candidate) {
        return contentsEquals(candidate) && timestamp == candidate.timestamp;
    }

    public Object getRawValue() {
        return value;
    }

    public String getStringValue() {
        return (String) value;
    }

    public byte getByteValue() {
        return ((Number) value).byteValue();
    }

    public short getShortValue() {
        return ((Number) value).shortValue();
    }

    public int getIntValue() {
        return ((Number) value).intValue();
    }

    public long getLongValue() {
        return ((Number) value).longValue();
    }

    public float getFloatValue() {
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else if (value instanceof Long) {
            return ((Long) value).longValue();
        } else {
            // Should signal an error...
            return (float) 0.0;
        }
    }

    public double getDoubleValue() {
        if (value instanceof Double) {
            return ((Double) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).longValue();
        } else {
            // Should signal an error...
            return 0.0;
        }
    }

    public char getCharValue() {
        return ((Character) value).charValue();
    }

    public boolean getBooleanValue() {
        return ((Boolean) value).booleanValue();
    }

    public double getCredibility() {
        return credibility;
    }

    public String getUnits() {
        return units;
    }

    public String getProvenance() {
        return provenance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getHalflife() {
        return halflife;
    }

    public static DataValue mostCredible(DataValue d1, DataValue d2) {
        if (d1 == null) {
            return d2;
        } else if (d2 == null) {
            return d1;
        } else if (d1.credibility == d2.credibility) {
            return d1.timestamp > d2.timestamp ? d1 : d2;
        } else if (d1.credibility > d2.credibility) {
            return d1;
        } else {
            return d2;
        }
    }

    /**
     * Returns the value with the hightest credibility. If the max credibilitry
     * is common to several values, returns the last one (order is significant).
     */
    public static DataValue maxCredibility(DataValue[] values) {
        DataValue max = null;
        for (DataValue element : values) {
            max = mostCredible(max, element);
        }
        return max;
    }

}
