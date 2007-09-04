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

package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Constants;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.qrs.DataFormula;
import org.cougaar.qos.qrs.DataValue;
import org.cougaar.qos.qrs.RSS;
import org.cougaar.qos.qrs.ResourceContext;

/**
 * This RSS formula class encapsules the use of data published into the RSS by
 * {@link org.cougaar.core.qos.metrics.DecayingHistory}.
 */
public class DecayingHistoryFormula extends DataFormula {
    private String period;
    private final String prefix;
    private final String kind;

    public DecayingHistoryFormula(String prefix, String kind) {
        super();
        this.prefix = prefix;
        this.kind = kind;
    }

    protected DataValue defaultValue() {
        return new DataValue(0);
    }

    protected void initialize(ResourceContext context) {
        super.initialize(context);

        String key = prefix + KEY_SEPR + kind + period + Constants.SecAvgKeySuffix;
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

    protected DataValue doCalculation(DataFormula.Values values) {
        DataValue computedValue = values.get("Formula");
        DataValue defaultValue = defaultValue();
        return DataValue.mostCredible(computedValue, defaultValue);
    }

    protected void setArgs(String[] args) {
        super.setArgs(args);
        this.period = args[0];
    }

}
