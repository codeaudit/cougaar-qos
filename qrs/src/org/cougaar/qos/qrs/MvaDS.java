/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

 */

package org.cougaar.qos.qrs;

/**
 * Mean Value Analysis model for Latency This assumes that component latencies
 * are separable.
 */
public class MvaDS extends ResourceContext {
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

    @Override
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
    @Override
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

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            registerDependency(context, "EffectiveMJips");
            registerDependency(context, "ReplySizeMean");
            registerDependency(context, "ReplyInstPerByteMean");
            registerDependency(context, "RequestSizeMean");
            registerDependency(context, "RequestInstPerByteMean");
        }

        @Override
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
