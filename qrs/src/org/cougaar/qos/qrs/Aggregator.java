/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.List;

abstract public class Aggregator extends DataFormula {
    abstract protected String getKey(String element);

    abstract protected List getElements();

    protected void initialize(ResourceContext context) {
        super.initialize(context);
        init();
    }

    public void reinitialize() {
        super.reinitialize();
        init();
    }

    private void init() {
        List elements = getElements();
        synchronized (elements) {
            for (int i = 0; i < elements.size(); i++) {
                String element = (String) elements.get(i);
                String[] parameters = {getKey(element)};
                ResourceContext dependency = RSS.instance().resolveSpec("Integrater", parameters);
                registerDependency(dependency, "Formula", element);
            }
        }
    }

}
