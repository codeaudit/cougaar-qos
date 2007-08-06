/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Iterator;
import java.util.Map;

abstract public class WeightedAverage
    extends Aggregator
{
    abstract protected double getWeight(String key);

    protected DataValue doCalculation(Values values) 
    {
	double cred = values.minPositiveCredibility();
	double sum = 0.0;
	int count = 0;
	Iterator itr = values.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    DataValue value = (DataValue) entry.getValue();
	    String key = (String) entry.getKey();
	    double weight = getWeight(key);
	    sum += value.getDoubleValue()*weight;
	    count += weight;
	}
	DataValue result = new DataValue(sum/count, cred);
	return result;
    }

}


