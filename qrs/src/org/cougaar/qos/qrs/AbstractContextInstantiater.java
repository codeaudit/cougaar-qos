/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

abstract public class AbstractContextInstantiater implements ContextInstantiater {
    public Object identifyParameters(String[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        if (parameters.length == 1) {
            return parameters[0];
        }
        String result = "";
        for (String element : parameters) {
            result += element.toString();
        }
        return result.intern();
    }
}
