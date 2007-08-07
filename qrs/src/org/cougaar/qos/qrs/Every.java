/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;

abstract public class Every extends Aggregator {
    protected DataValue doCalculation(Values values) {
        double cred = values.minPositiveCredibility();
        boolean result = true;
        for (DataValue value : values.values()) {
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
