/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Iterator;
import java.util.Map;

abstract public class Average extends Aggregator {
    protected DataValue doCalculation(Values values) {
        double cred = values.minPositiveCredibility();
        double sum = 0.0;
        int count = 0;
        Iterator itr = values.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            DataValue value = (DataValue) entry.getValue();
            sum += value.getDoubleValue();
            count++;
        }
        DataValue result = new DataValue(sum / count, cred);
        return result;
    }

}
