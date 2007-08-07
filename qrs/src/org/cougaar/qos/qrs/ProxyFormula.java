/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public class ProxyFormula extends DataFormula {

    private final DataFormula delegate;

    public ProxyFormula(DataFormula delegate) {
        super();
        this.delegate = delegate;
    }

    protected void initialize(ResourceContext context) {
        super.initialize(context);
        registerDependency(delegate, "Delegate");
    }

    protected DataValue doCalculation(Values values) {
        return delegate.computeValue(true);
    }

    protected boolean hasArgs(String[] args) {
        return delegate.hasArgs(args);
    }

}
