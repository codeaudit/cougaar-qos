/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.qrs.AbstractContextInstantiater;
import org.cougaar.qos.qrs.ContextInstantiater;
import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.ResourceContext;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * This RSS ResourceContext represents a remote Agent to which this Node is
 * communicating. Its parent context in the RSS inheritance tree should be a
 * Node ({@link NodeDS}). It's identified by the name of the destination
 * Agent. It supports a range of messaging and ip-resource formulas, described
 * in more detail elsewhere.
 */
public class DestinationDS extends CougaarDS {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ParameterError {
                return new DestinationDS(parameters, parent);
            }

            @Override
            public Object identifyParameters(String[] parameters) {
                if (parameters == null || parameters.length != 1) {
                    return null;
                }
                return parameters[0];
            }

        };
        registerContextInstantiater("Destination", cinst);
    }

    private static final String DESTINATION = "destination".intern();

    public DestinationDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    @Override
   protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("DestinationDS: wrong number of parameters");
        }
        if (!(parameters[0] != null)) {
            throw new ParameterError("DestinationDS: wrong parameter type");
        } else {
            // could canonicalize here
            String destination = parameters[0];
            bindSymbolValue(DESTINATION, destination);
            String node_name = (String) getValue(NodeDS.NODENAME);
            historyPrefix =
                    "Node" + KEY_SEPR + node_name + KEY_SEPR + "Destination" + KEY_SEPR
                            + destination;
        }
    }

    @Override
   protected DataFormula instantiateFormula(String kind) {
        if (kind.equals(Constants.MSG_FROM) || kind.equals(Constants.BYTES_FROM)
                || kind.equals(Constants.MSG_TO) || kind.equals(Constants.BYTES_TO)) {
            return new DecayingHistoryFormula(historyPrefix, kind);
        } else if (kind.equals(Constants.PERSIST_SIZE_LAST)) {
            return new PersistSizeLast();
        } else if (kind.equals("AgentIpAddress")) {
            return new AgentIpAddress();
        } else if (kind.equals("CapacityMax")) {
            return new CapacityMax();
        } else if (kind.equals("OnSameSecureLAN")) {
            return new OnSameSecureLAN();
        } else {
            return null;
        }
    }

    static class AgentIpAddress extends DataFormula {
        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            String agent = (String) context.getValue(DESTINATION);
            String[] parameters = {agent};
            ResourceNode resource_node = new ResourceNode("Agent", parameters);
            ResourceNode[] path = {resource_node};
            ResourceContext dependency = RSS.instance().getPathContext(path);
            registerDependency(dependency, "PrimaryIpAddress");
        }

        @Override
      public void formulaDeleted(DataFormula formula) {
            super.formulaDeleted(formula);

            Logger logger = Logging.getLogger("org.cougaar.core.qos.rss.DestinationDS");
            if (logger.isDebugEnabled()) {
                String agent = (String) getContext().getValue(DESTINATION);
                logger.debug("Formula " + formula + " deleted from " + agent);
            }

        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            DataValue result = values.get("PrimaryIpAddress");
            Logger logger = Logging.getLogger("org.cougaar.core.qos.rss.DestinationDS");
            if (logger.isDebugEnabled()) {
                String agent = (String) getContext().getValue(DESTINATION);
                logger.debug("Recalculating Agent " + agent + " AgentIpAddress as" + result);
            }
            return result;
        }

    }

    static class CapacityMax extends DataFormula {
        private static final String IP_FLOW_KEY = "IpFlow";
        private static final DataValue UnknownCapacityMax = new DataValue(0, 0.0);
        private String agentAddr;
        private String myAddr;
        
        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            // subscribe to our own IpAddress and NodeIpAddress
            registerDependency(context, "AgentIpAddress");
            registerDependency(context, "PrimaryIpAddress");
        }
        
        private DataValue maybeReinit(String agentAddr, String myAddr) {
            if (!agentAddr.equals(this.agentAddr) || !myAddr.equals(this.myAddr)) {
                this.agentAddr = agentAddr;
                this.myAddr = myAddr;
                
                if (NodeDS.isUnknownHost(agentAddr) || NodeDS.isUnknownHost(myAddr)) {
                    // No point is changing dependencies until we have a valid pair
                    // for IpFlow.
                    return UnknownCapacityMax;
                }
                
                unregisterDependency(IP_FLOW_KEY);
                String[] flow_params = {myAddr, agentAddr};
                ResourceNode flow = new ResourceNode(IP_FLOW_KEY, flow_params);
                ResourceNode capm = new ResourceNode("CapacityMax", null);
                ResourceNode[] path = {flow, capm};
                DataFormula cap_max = RSS.instance().getPathFormula(path);
                registerDependency(cap_max, IP_FLOW_KEY);
                return cap_max.blockingQuery();
            } else {
                return null;
            }
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            String agentAddr = null;
            String myAddr = null;
            Logger logger = Logging.getLogger("org.cougaar.core.qos.rss.DestinationDS");
            try {
                agentAddr = values.get("AgentIpAddress").getStringValue();
                myAddr = values.get("PrimaryIpAddress").getStringValue();
                DataValue cap_max = maybeReinit(agentAddr, myAddr);
                if (cap_max != null) {
                    return cap_max;
                }
            } catch (Exception ex) {
                // One of the dependencies is NO_VALUE
                logger.info(ex.getMessage());
                return UnknownCapacityMax;
            }
            
            DataValue result = values.get(IP_FLOW_KEY);
            if (logger.isDebugEnabled()) {
                logger.debug("Recalculating CapacityMax with" + " my address = " + myAddr
                             + " dest address = " + agentAddr + " = " + result);
            }
            return result == null ? UnknownCapacityMax : result;
        }

    }

    private static final double LAN_CAPACITY = 10000.0;

    static class OnSameSecureLAN extends DataFormula {
        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            registerDependency(context, "CapacityMax");
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            DataValue capacityMax = values.get("CapacityMax");
            double capacityMaxValue = capacityMax.getDoubleValue();
            double capacityMaxCred = capacityMax.getCredibility();

            DataValue results =
                    new DataValue(capacityMaxCred >= SYS_DEFAULT_CREDIBILITY
                            && capacityMaxValue >= LAN_CAPACITY, capacityMaxCred);
            Logger logger = Logging.getLogger("org.cougaar.core.qos.rss.DestinationDS");
            if (logger.isDebugEnabled()) {
                String agent = (String) getContext().getValue(DESTINATION);
                logger.debug("Recalculating Agent " + agent + " OnSameSecureLAN with"
                        + " capacity max = " + capacityMax + " results = " + results);
            }
            return results;
        }

    }

    class PersistSizeLast extends DataFormula {

        protected DataValue defaultValue() {
            return new DataValue(0);
        }

        @Override
      protected void initialize(ResourceContext ctx) {
            super.initialize(ctx);
            String key = historyPrefix + KEY_SEPR + Constants.PERSIST_SIZE_LAST;
            String[] parameters = {key};
            ResourceNode node = new ResourceNode();
            node.kind = "Integrater";
            node.parameters = parameters;
            ResourceNode formula = new ResourceNode();
            formula.kind = "Formula";
            formula.parameters = new String[0];
            ResourceNode[] path = {node, formula};
            DataFormula dependency = RSS.instance().getPathFormula(path);
            registerDependency(dependency, "Formula");

        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            DataValue computedValue = values.get("Formula");
            DataValue defaultValue = defaultValue();
            return DataValue.mostCredible(computedValue, defaultValue);
        }

    }

}
