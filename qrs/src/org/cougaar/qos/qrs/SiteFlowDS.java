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
    @Override
   protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    @Override
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
    @Override
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

    abstract static class Formula extends DataFormula {
        abstract String getKey();

        @Override
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

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            values = new DataValue[3];
            values[0] = new DataValue(10000, DEFAULT_CREDIBILITY);
        }

        @Override
      String getKey() {
            return "Capacity" + KEY_SEPR + "Max";
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            this.values[1] = values.get("Reverse");
            this.values[2] = values.get("Forward");
            return DataValue.maxCredibility(this.values);
        }

    }

    public static class CapacityUnused extends Formula {

        private DataValue[] values;

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            values = new DataValue[3];
            // default is the CapacityMax from the same context
            registerDependency(context, "CapacityMax");
        }

        @Override
      String getKey() {
            return "Capacity" + KEY_SEPR + "Unused";
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            this.values[0] = values.get("CapacityMax");
            this.values[1] = values.get("Reverse");
            this.values[2] = values.get("Forward");
            return DataValue.maxCredibility(this.values);
        }

    }

}
