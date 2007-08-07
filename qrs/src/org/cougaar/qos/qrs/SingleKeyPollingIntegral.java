/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * An extension of SingleKeyPollingIntegral in which the calculation is just a
 * single DataFeed value. Instantiate subclasses must proide the key, through
 * the method getKey().
 */
abstract class SingleKeyPollingIntegral extends PollingIntegral {
    abstract protected String getKey();

    protected void configureDependencies() {
        String key = getKey();
        String[] parameters = {key};
        ResourceContext dependency = RSS.instance().resolveSpec("Integrater", parameters);
        registerDependency(dependency, "Formula");
    }

    protected DataValue computeValueFromDependencies(Values values) {
        return values.get("Formula");
    }

}
