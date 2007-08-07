/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Map;

abstract public class WeightedAverage extends Aggregator {
    abstract protected double getWeight(String key);

    protected DataValue doCalculation(Values values) {
        double cred = values.minPositiveCredibility();
        double sum = 0.0;
        int count = 0;
        for (Map.Entry<String, DataValue> entry : values.entrySet()) {
            String key = entry.getKey();
            DataValue value = entry.getValue();
            double weight = getWeight(key);
            sum += value.getDoubleValue() * weight;
            count += weight;
        }
        DataValue result = new DataValue(sum / count, cred);
        return result;
    }

}
