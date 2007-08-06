/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public interface ContextInstantiater
{
    public ResourceContext instantiateContext(String[] parameters, 
					      ResourceContext parent)
	throws ResourceContext.ParameterError;
    public Object identifyParameters(String[] parameters);
}
