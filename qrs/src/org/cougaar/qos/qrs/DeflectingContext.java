/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * A context that deflects symbol lookups to some other context before it walks
 * up the tree. The deflector is set via setDeflector.
 */
abstract public class DeflectingContext extends ResourceContext {
    private ResourceContext deflector;

    protected DeflectingContext(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    protected void setDeflector(ResourceContext deflector) {
        this.deflector = deflector;
    }

    protected Object nextLookup(String symbol, String[] args, boolean resolve) {
        if (deflector != null) {
            Object value = deflector.lookupSymbol(symbol, args, resolve);
            if (value != null) {
                return value;
            }
        }

        // If we get here the deflector hasn't handled it
        return super.nextLookup(symbol, args, resolve);
    }

}
