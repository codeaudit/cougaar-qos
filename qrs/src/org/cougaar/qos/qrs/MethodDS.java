/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public class MethodDS extends ResourceContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new MethodDS(parameters, parent);
            }

        };
        registerContextInstantiater("Method", cinst);
    }

    private static final String METHOD_NAME = "MethodName";

    protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("ReplySizeMean")) {
            return new ReplySizeMean();
        } else if (kind.equals("ReplyInstPerByteMean")) {
            return new ReplyInstPerByteMean();
        } else if (kind.equals("RequestSizeMean")) {
            return new RequestSizeMean();
        } else if (kind.equals("RequestInstPerByteMean")) {
            return new RequestInstPerByteMean();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain one string, the name of the method
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("MethodDS: wrong number of parameters");
        } else {
            bindSymbolValue(METHOD_NAME, parameters[0]);
        }
    }

    private MethodDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    abstract static class Formula extends DataFormula implements Constants {

        abstract String getKey();

        abstract DataValue defaultValue();

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            String className = (String) context.getValue("ClassName");
            String methodName = (String) context.getValue("MethodName");
            String key =
                    "Class" + KEY_SEPR + className + KEY_SEPR + methodName + KEY_SEPR + getKey();

            String[] parameters = {key};
            ResourceContext dependency = RSS.instance().resolveSpec("Integrater", parameters);
            registerDependency(dependency, "Formula");
        }

        protected DataValue doCalculation(DataFormula.Values values) {
            DataValue computedValue = values.get("Formula");
            DataValue defaultValue = defaultValue();
            return DataValue.mostCredible(computedValue, defaultValue);
        }

    }

    public static class ReplySizeMean extends Formula {
        String getKey() {
            return "reply" + KEY_SEPR + "size" + KEY_SEPR + "mean";
        }

        DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class ReplyInstPerByteMean extends Formula {
        String getKey() {
            return "reply" + KEY_SEPR + "instPerByte" + KEY_SEPR + "mean";
        }

        DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class RequestSizeMean extends Formula {
        String getKey() {
            return "request" + KEY_SEPR + "size" + KEY_SEPR + "mean";
        }

        DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class RequestInstPerByteMean extends Formula {
        String getKey() {
            return "request" + KEY_SEPR + "instPerByte" + KEY_SEPR + "mean";
        }

        DataValue defaultValue() {
            return new DataValue(0);
        }
    }

}
