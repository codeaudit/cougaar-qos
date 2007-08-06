/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;


/**
 * A ResourceContext for a physical Object which is on a Process which
 * is on a Host.  The available formulae ??? */
public class ObjectDS extends DeflectingContext
{
    static void register()
    {
	ContextInstantiater cinst = new AbstractContextInstantiater() {
		public ResourceContext instantiateContext(String[] parameters, 
							  ResourceContext parent)
		    throws ResourceContext.ParameterError
		{
		    return new ObjectDS(parameters, parent);
		}

		
	    };
	registerContextInstantiater("Object", cinst);
    }

    // pseudo-enum
    private static final String KEY = "key";
    private static final String INTERFACE_NAME = "interfaceName";
    private static final String IMPLEMENTATION_NAME = "implementationName";

    public static final String IMPLEMENTATION_DEF = "implementationDef";


    protected DataFormula instantiateFormula(String kind)
    {
	if (kind.equals("RemoteLoadAverage")) {
	    return new RemoteLoadAverage();
	} else {
	    return null;
	}
    }



    /**
     * The parameters should contain three strings: the object key,
     * interface name and the implementation.  The object key is in
     * the Object reference for both CORBA and RMI.  The interface is
     * in the CORBA reference and the (stub of the) implemention is in
     * the RMI reference. */
    protected void verifyParameters(String[] parameters) 
	throws ParameterError
    {
	if (parameters == null || parameters.length != 3) {
	    throw new ParameterError
		("ObjectDS: wrong number of parameters");
	} else {
	    bindSymbolValue(KEY, parameters[0]);
	    String iface = (String) parameters[1];
	    bindSymbolValue(INTERFACE_NAME, iface);
	    String impl = (String) parameters[2];
	    bindSymbolValue(IMPLEMENTATION_NAME, impl);

	    // find extant class context
	    String[] args = { impl, iface };
	    ResourceContext class_ctxt = 
		RSS.instance().resolveSpec("Class", args);
	    bindSymbolValue(IMPLEMENTATION_DEF, class_ctxt);
	    // Use class as prototype for Object
	    setDeflector(class_ctxt);
	}
    }

    private ObjectDS(String[] parameters, ResourceContext parent) 
	throws ParameterError
    {
	super(parameters, parent);
    }


    /**
     * Only for testing.  **/
    public static class RemoteLoadAverage extends DataFormula {

	protected void initialize(ResourceContext context) {
	    super.initialize(context);
	    Object raw = context.getValue("calls");
	    ResourceContext remote = (ResourceContext) raw;
	    registerDependency(remote, "LoadAverage");
	}

	protected DataValue doCalculation(DataFormula.Values values) {
	    return values.get("LoadAverage");
	}
    }


    // Some Day Object will have formulas to calculate things like
    // class, version?

    

}
