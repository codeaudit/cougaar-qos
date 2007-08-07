/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;


/**
 * A ResourceContext for a Process which is on a Host. For now the Process named
 * by its port, but latter it needs to have a list of ports and a PID. The
 * available formulae ???
 */
public class ProcessDS extends ResourceContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new ProcessDS(parameters, parent);
            }

        };
        registerContextInstantiater("Process", cinst);
    }

    private static final String PID = "pid";
    private static final String PORT = "port";

    protected DataFormula instantiateFormula(String kind) {
        return null;
    }

    /**
     * The parameters should contain one string, the port of the host being
     * monitored.
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("ProcessDS: wrong number of parameters");
        } else {
            String port_string = parameters[0];
            try {
                int port = Integer.parseInt(port_string);
                bindSymbolValue(PORT, port);

                // To be done
                bindSymbolValue(PID, 0);
            } catch (NumberFormatException not_a_num) {
                throw new ParameterError("ProcessDS: port is not an int");
            }
        }

    }

    private ProcessDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    // Some Day Process will have formulas to calculate things like
    // nice value.

}
