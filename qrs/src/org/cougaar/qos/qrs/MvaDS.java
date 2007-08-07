/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * Mean Value Analysis model for Latency This assumes that component latencies
 * are separable.
 */
public class MvaDS extends ResourceContext implements Constants {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new MvaDS(parameters, parent);
            }

        };
        registerContextInstantiater("MVA", cinst);
    }

    private static final String MODELTYPE = "modelType";

    protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("CpuLatencyMean")) {
            return new CpuLatencyMean();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain one string, the type of model, in this case
     * MVA
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("MvaDS: wrong number of parameters");
        } else {
            // could canonicalize here
            String modelType = parameters[0];
            bindSymbolValue(MODELTYPE, modelType);

        }
    }

    private MvaDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    public static class CpuLatencyMean extends DataFormula {

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            registerDependency(context, "EffectiveMJips");
            registerDependency(context, "ReplySizeMean");
            registerDependency(context, "ReplyInstPerByteMean");
            registerDependency(context, "RequestSizeMean");
            registerDependency(context, "RequestInstPerByteMean");
        }

        protected DataValue doCalculation(DataFormula.Values values) {
            // JAZ Need to make credibility a function of componant data's
            // credibility
            double credibility = values.minCredibility();

            double replySize = values.doubleValue("ReplySizeMean");
            double replyIpb = values.doubleValue("ReplyInstPerByteMean");
            double requestSize = values.doubleValue("RequestSizeMean");
            double requestIpb = values.doubleValue("RequestInstPerByteMean");
            double mjips = values.doubleValue("EffectiveMJips");

            double latency = (replySize * replyIpb + requestSize * requestIpb) // requestinstructions
                    / mjips // Million Java Inst Per Sec
                    / 1000; // Million / millisec

            return new DataValue(latency, credibility);
        }

    }

}
