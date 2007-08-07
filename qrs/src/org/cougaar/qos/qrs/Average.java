/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;


abstract public class Average extends Aggregator {
    protected DataValue doCalculation(Values values) {
        double cred = values.minPositiveCredibility();
        double sum = 0.0;
        int count = 0;
        for (DataValue value : values.values()) {
            sum += value.getDoubleValue();
            count++;
        }
        DataValue result = new DataValue(sum / count, cred);
        return result;
    }

}
