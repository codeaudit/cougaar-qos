/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.ArrayList;

/**
 * A ResourceContext for a Class.  */
public class ClassDS extends ResourceContext 
{
    static void register() 
    {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ResourceContext.ParameterError
		{
		    return new ClassDS(parameters, parent);
		}

		
	    };
	registerContextInstantiater("Class", cinst);
    }

    private static final String CLASS_NAME = "ClassName";
    private static final String INTERFACE_NAME = "InterfaceName";
    private static final String METHODS = "Methods";


    // Host Classes can be the first element in a path.  They have
    // no parent or context other than the root.
    protected ResourceContext preferredParent(RSS root) {
	return root;
    }



    protected DataFormula instantiateFormula(String kind)
    {
	return null;
    }


    /**
     * The parameters should contain one string,
     * the name of the class */
    protected void verifyParameters(String[] parameters) 
	throws ParameterError
    {
	if (parameters == null || parameters.length < 2) {
	    throw new ParameterError("ClassDS: wrong number of parameters");
	} else {
	    bindSymbolValue(CLASS_NAME, parameters[0]);
	    bindSymbolValue(INTERFACE_NAME, parameters[1]);
	    ResourceContext[] methods = 
		new ResourceContext[parameters.length-2];
	    // The rest of the parameters are method names
	    for (int i=2; i<parameters.length; i++) {
		// Make a context for the method name
		String[] params = { parameters[i] };
		methods[i-2] = resolveSpec("Method", params);
	    }
	    bindSymbolValue(METHODS, methods);

	    // For testing deflection
	    bindSymbolValue("Foo", new Double(10.0));
	}
    }



    private ClassDS(String[] parameters, ResourceContext parent) 
	throws ParameterError
    {
	super(parameters, parent);
    }


}
