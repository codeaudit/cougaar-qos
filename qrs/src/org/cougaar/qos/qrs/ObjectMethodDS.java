/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.cougaar.qos.ResourceStatus.ResourceNode;

public class ObjectMethodDS extends DeflectingContext
{
    static void register()
    {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ResourceContext.ParameterError
		{
		    return new ObjectMethodDS(parameters, parent);
		}

		
	    };
	registerContextInstantiater("ObjectMethod", cinst);
    }

    private static final String METHOD_NAME = "MethodName";
    private MethodDS methodDefinition;

    protected DataFormula instantiateFormula(String kind)
    {
	return null;
    }



    /**
     * The parameters should contain one string, the name of the
     * method */
    protected void verifyParameters(String[] parameters) 
	throws ParameterError
    {
	if (parameters == null || parameters.length != 1) {
	    throw new ParameterError("MethodDS: wrong number of parameters");
	} else {
	    bindSymbolValue(METHOD_NAME, parameters[0]);

	    ResourceContext parent = getParent();
	    ResourceContext class_ctxt = (ResourceContext)
		parent.getSymbolValue(ObjectDS.IMPLEMENTATION_DEF);
	    methodDefinition = (MethodDS) 
		class_ctxt.resolveSpec("Method", parameters);
	    setDeflector(methodDefinition);
	}
    }

    private ObjectMethodDS(String[] parameters, ResourceContext parent) 
	throws ParameterError
    {
	super(parameters, parent);
    }

}
