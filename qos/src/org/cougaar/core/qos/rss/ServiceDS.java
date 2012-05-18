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

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.qrs.AbstractContextInstantiater;
import org.cougaar.qos.qrs.ContextInstantiater;
import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.ResourceContext;

/**
 * This RSS ResourcContext represents subsystems in a Node, other than agents.
 * Its parent in the RSS hierarchy tree is a Node. It's identified by the name
 * of the service and defines two local formulas for CPU load.
 * <p>
 * The currently supported "services" are defined by the metrics and mts
 * containers. Other subsystems like servlet management and persistence might be
 * supported later.
 * 
 * @see NodeDS
 */
public class ServiceDS extends CougaarDS {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ParameterError {
                return new ServiceDS(parameters, parent);
            }

            @Override
            public Object identifyParameters(String[] parameters) {
                if (parameters == null || parameters.length != 1) {
                    return null;
                }
                return parameters[0];
            }

        };
        registerContextInstantiater("Service", cinst);
    }

    private static final String SERVICENAME = "servicename".intern();

    public ServiceDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    @Override
   protected boolean useParentPath() {
        return false;
    }

    // Service DataScopes can be the first element in a path. They must
    // find or make the corresponding NodeDS and return that as the
    // preferred parent.
    @Override
   protected ResourceContext preferredParent(RSS root) {
        ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
        NodeIdentificationService node_id_svc =
                sb.getService(this, NodeIdentificationService.class, null);
        String nodeID = node_id_svc.getMessageAddress().toString();

        String[] params = {nodeID};
        ResourceNode node = new ResourceNode();
        node.kind = "Node";
        node.parameters = params;
        ResourceNode[] path = {node};
        ResourceContext parent = root.getPathContext(path);
        setParent(parent);
        return parent;
    }

    @Override
   protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("ServiceDS: wrong number of parameters");
        }
        if (!(parameters[0] != null)) {
            throw new ParameterError("ServiceDS: wrong parameter type");
        } else {
            // could canonicalize here
            String servicename = parameters[0];
            bindSymbolValue(SERVICENAME, servicename);
            historyPrefix = "Service" + KEY_SEPR + servicename;
        }
    }

    @Override
   protected DataFormula instantiateFormula(String kind) {
        if (kind.equals(Constants.CPU_LOAD_AVG) || kind.equals(Constants.CPU_LOAD_MJIPS)) {
            return new DecayingHistoryFormula(historyPrefix, kind);
        } else {
            return null;
        }
    }

}
