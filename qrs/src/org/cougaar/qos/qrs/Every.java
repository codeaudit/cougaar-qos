/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

abstract public class Every extends Aggregator {
    protected DataValue doCalculation(Values values) {
        double cred = values.minPositiveCredibility();
        boolean result = true;
        Iterator itr = values.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            DataValue value = (DataValue) entry.getValue();
            Object raw = value.getRawValue();
            if (raw instanceof Boolean) {
                if (!value.getBooleanValue()) {
                    result = false;
                    break;
                }
            } else {
                // log this
                Logger logger = Logging.getLogger(Every.class);
                logger.error("'Every' got a non-boolean value " + raw);
            }
        }
        return new DataValue(result, cred);
    }

}
