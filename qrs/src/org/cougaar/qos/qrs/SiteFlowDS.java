/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * A sample ResourceContext which looks for ip-flow capacity data on any feed,
 * by using a Remos-style key with an IntegraterDS. The available formulas are
 * 'CapacityMax' and 'CapacityUnused'.
 */
public class SiteFlowDS extends ResourceContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new SiteFlowDS(parameters, parent);
            }

        };
        registerContextInstantiater("SiteFlow", cinst);
    }

    private static final String SOURCE = "source";
    private static final String DESTINATION = "destination";

    // Can be the first element in a path. They have no parent or
    // context other than the root.
    protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("CapacityMax")) {
            return new CapacityMax();
        } else if (kind.equals("CapacityUnused")) {
            return new CapacityUnused();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain two strings, the source and destination
     * Sites of the flow.
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        // should be two strings (ip addresses)
        if (parameters == null || parameters.length != 2) {
            throw new ParameterError("SiteFlowDS ...");
        } else {
            bindSymbolValue(SOURCE, parameters[0]);
            bindSymbolValue(DESTINATION, parameters[1]);
        }
    }

    private SiteFlowDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    abstract static class Formula extends DataFormula implements Constants {
        abstract String getKey();

        protected void initialize(ResourceContext context) {
            super.initialize(context);

            String destination = (String) context.getValue(DESTINATION);
            String source = (String) context.getValue(SOURCE);
            String formulaKey = getKey();

            String key =
                    "Site" + KEY_SEPR + "Flow" + KEY_SEPR + source + KEY_SEPR + destination
                            + KEY_SEPR + formulaKey;
            String[] parameters = {key};
            ResourceContext dependency = RSS.instance().resolveSpec("Integrater", parameters);
            registerDependency(dependency, "Formula", "Forward");

            String rkey =
                    "Site" + KEY_SEPR + "Flow" + KEY_SEPR + destination + KEY_SEPR + source
                            + KEY_SEPR + formulaKey;
            String[] rparameters = {rkey};
            ResourceContext rdependency = RSS.instance().resolveSpec("Integrater", rparameters);
            registerDependency(rdependency, "Formula", "Reverse");

        }

    }

    public static class CapacityMax extends Formula {

        private DataValue[] values;

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            values = new DataValue[3];
            values[0] = new DataValue(10000, DEFAULT_CREDIBILITY);
        }

        String getKey() {
            return "Capacity" + KEY_SEPR + "Max";
        }

        protected DataValue doCalculation(DataFormula.Values values) {
            this.values[1] = values.get("Reverse");
            this.values[2] = values.get("Forward");
            return DataValue.maxCredibility(this.values);
        }

    }

    public static class CapacityUnused extends Formula {

        private DataValue[] values;

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            values = new DataValue[3];
            // default is the CapacityMax from the same context
            registerDependency(context, "CapacityMax");
        }

        String getKey() {
            return "Capacity" + KEY_SEPR + "Unused";
        }

        protected DataValue doCalculation(DataFormula.Values values) {
            this.values[0] = values.get("CapacityMax");
            this.values[1] = values.get("Reverse");
            this.values[2] = values.get("Forward");
            return DataValue.maxCredibility(this.values);
        }

    }

}
