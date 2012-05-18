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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.util.log.Logger;

/**
 * A DataFormula describes a (possibly computed) attribute of a ResourceContext.
 * DataFormulas are gotten from the corresponding context, by name. In general
 * the name will be a toplevel static inner class of the context (which implies
 * only one formula per type).
 */
abstract public class DataFormula implements DataValueChangedCallbackListener, Constants {
    private final List<DataValueChangedCallbackListener> listeners = 
        new ArrayList<DataValueChangedCallbackListener>();
    private String name;
    private String[] args;
    private DataValue cachedValue;
    private ResourceContext context;
    private List<DataFormula> dependencies; // collection of DataFormula's
    private Map<DataFormula,String> dependencyKeys; // formula->key
    private Values dependencyValues; // formula_key -> value
    private boolean deleted;
    private String pretty_name;

    
    /**
     * Instantiable subclasses provide this to do the formula's work, if there's
     * any to do.
     */
    abstract protected DataValue doCalculation(Values values);

    protected DataValue computeValue(boolean forwardChain) {
        synchronized (dependencies) {
            if (deleted) {
                return DataValue.NO_VALUE;
            }
            for (DataFormula formula : dependencies) {
                DataValue value = null;
                if (forwardChain) {
                    value = formula.blockingQuery();
                } else {
                    value = formula.getCachedValue();
                }
                String key = dependencyKeys.get(formula);
                dependencyValues.put(key, value);
            }
        }
        return doCalculation(dependencyValues);
    }

    @Override
   public String toString() {
        return pretty_name;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String[] getArgs() {
        return args;
    }

    protected void setArgs(String[] args) {
        this.args = args;
    }

    // simple version
    protected boolean hasArgs(String[] args) {
        if (args == null) {
            return this.args == null || this.args.length == 0;
        }
        if (this.args == null) {
            return args.length == 0;
        }
        if (this.args.length != args.length) {
            return false;
        }
        for (int i = 0; i < this.args.length; i++) {
            if (!this.args[i].equals(args[i])) {
                return false;
            }
        }
        return true;
    }

    protected List<ResourceNode> getPath() {
        List<ResourceNode> path = context.getPath();
        path.add(new ResourceNode(name, args));
        return path;
    }

    protected void initialize(ResourceContext context) {
        this.context = context;
        dependencies = new ArrayList<DataFormula>();
        dependencyValues = new Values();
        dependencyKeys = new HashMap<DataFormula,String>();
        cachedValue = DataValue.NO_VALUE;
        deleted = false;
    }

    public void reinitialize() {
        dependencies = new ArrayList<DataFormula>();
    }

    protected void postInitialize() {
        setCachedValue(blockingQuery());
        pretty_name = name;
        if (args != null && args.length > 0) {
            pretty_name += "[" + args[0];
            for (int i = 1; i < args.length; i++) {
                pretty_name += ", " + args[i];
            }
            pretty_name += "]";
        }
    }
    
    protected void unregisterDependencies() {
        List<DataFormula> dependencies_clone;
        synchronized (dependencies) {
            dependencies_clone = new ArrayList<DataFormula>(dependencies);
            dependencies.clear();
            for (DataFormula formula : dependencies_clone) {
                dependencyKeys.remove(formula);
            }
        }
        for (DataFormula formula : dependencies_clone) {
            formula.unsubscribe(this);
        }
    }
    
    protected void unregisterDependency(String key) {
        DataFormula formula = null;
        synchronized (dependencies) {
            for (Map.Entry<DataFormula, String> entry : dependencyKeys.entrySet()) {
                if (entry.getValue().equals(key)) {
                    formula = entry.getKey();
                    break;
                }
            }
            if (formula != null) {
                dependencyKeys.remove(formula);
                dependencies.remove(formula);
            }
        }
        if (formula != null) {
            formula.unsubscribe(this);
        }
    }

    protected void registerDependency(DataFormula formula, String key) {
        synchronized (dependencies) {
            if (deleted) {
                return;
            }
            dependencies.add(formula);
            dependencyKeys.put(formula, key);
        }
        formula.subscribe(this);
    }

    protected DataFormula registerDependency(ResourceContext context, String formulaName) {
        return registerDependency(context, formulaName, null, formulaName);
    }

    protected DataFormula registerDependency(ResourceContext context,
                                             String formulaName,
                                             String[] formulaArgs) {
        return registerDependency(context, formulaName, formulaArgs, formulaName);
    }

    protected DataFormula registerDependency(ResourceContext context, String formulaName, String key) {
        return registerDependency(context, formulaName, null, key);
    }

    protected DataFormula registerDependency(ResourceContext context,
                                             String formulaName,
                                             String[] formulaArgs,
                                             String key) {
        DataFormula requiredFormula = context.getFormula(formulaName, formulaArgs);
        if (requiredFormula != null) {
            registerDependency(requiredFormula, key);
        } else {
            Logger logger = Logging.getLogger(DataFormula.class);
            logger.error("### Failed to find formula " + formulaName + " in context " + context);
        }
        return requiredFormula;
    }

    /**
     * Returns a list of other formulas on which this one depends. Only used by
     * the ResourceContext gui (so far).
     */
    public List<DataFormula> getDependencies() {
        return dependencies;
    }

    /**
     * Returns a list of listeners on this formula. Only used by the
     * ResourceContext gui (so far).
     */
    public List<DataValueChangedCallbackListener> getSubscribers() {
        return listeners;
    }

    protected ResourceContext getContext() {
        return context;
    }

    protected DataValue getCachedValue() {
        return cachedValue;
    }

    protected synchronized void setCachedValue(DataValue cachedValue) {
        if (cachedValue != null && !cachedValue.contentsEquals(this.cachedValue)) {
            this.cachedValue = cachedValue;
            notifyListeners();
        }
    }

    /**
     * Implements the DataValueChangedCallbackListener. This would typically be
     * called here when one of the other formulas on which this one depends has
     * a new value.
     */
    public void dataValueChanged(DataFormula changedFormula) {
        // Some value I depend on has changed. Backward chain.
        DataValue newValue = computeValue(false);
        synchronized (this) {
            if (newValue != null && !newValue.contentsEquals(cachedValue)) {
                cachedValue = newValue;
                notifyListeners();
            }
        }
    }

    /**
     * Returns the currently cached value. This is part of the standard
     * (non-gui) DataFormula interface.
     */
    public DataValue query() {
        return cachedValue;
    }

    /**
     * Computes and returns a new value. This is part of the standard (non-gui)
     * DataFormula interface.
     */
    public DataValue blockingQuery() {
        // Forward chain (ignore cache, no notifications)
        return computeValue(true);
    }

    /**
     * Registers the listener so that will be called back when the value of this
     * one changes. This is part of standard (non-gui) DataFormula interface.
     */
    public void subscribe(DataValueChangedCallbackListener listener) {
        synchronized (listeners) {
            if (deleted) {
                // This should throw an exception or return a status
                // or something to let the caller know that the
                // subscription is meaningless.
                Logger logger = Logging.getLogger(DataFormula.class);
                logger.warn(listener + " subscribed to deleted formula " + this);
                return;

            }
            listeners.add(listener);
        }
    }

    /**
     * Unregisters the listener for callbackes. This is part of the standard
     * (non-gui) DataFormula interface.
     */
    public void unsubscribe(DataValueChangedCallbackListener listener) {
        synchronized (listeners) {
            if (deleted) {
                return;
            }
            listeners.remove(listener);
        }
    }

    public boolean shouldNotify(DataValue value) {
        return true;
    }

    private void notifyListeners() {
        synchronized (listeners) {
            if (deleted) {
                return;
            }
            for (DataValueChangedCallbackListener listener : listeners) {
                if (listener.shouldNotify(cachedValue)) {
                    listener.dataValueChanged(this);
                }
            }
        }
    }

    // Some formula I depend on has been deleted.
    public void formulaDeleted(DataFormula formula) {
        // remove formula from dependencies
        synchronized (dependencies) {
            if (deleted) {
                return;
            }
            dependencies.remove(formula);
        }
        String key = dependencyKeys.get(formula);
        dependencyKeys.remove(formula);
        List<ResourceNode> path_list = formula.getPath();
        ResourceNode[] path = new ResourceNode[path_list.size()];
        path_list.toArray(path);
        DataFormula replacement = RSS.instance().getPathFormula(path);
        if (replacement != null) {
            registerDependency(replacement, key);
            dataValueChanged(replacement);
        } else {
            Logger logger = Logging.getLogger(DataFormula.class);
            StringBuffer buf = new StringBuffer();
            buf.append("Unable to recreate ");
            RSSUtils.pathToString(buf, path);
            buf.append(". ");
            buf.append(this.toString());
            buf.append(" has lost this dependency forever");
            logger.warn(buf.toString());
        }
    }
    
    void contextDeleted() {
        List<DataValueChangedCallbackListener> listeners_clone;
        List<DataFormula> dependencies_clone;

        synchronized (listeners) {
            synchronized (dependencies) {
                deleted = true;
                listeners_clone = new ArrayList<DataValueChangedCallbackListener>(listeners);
                dependencies_clone = new ArrayList<DataFormula>(dependencies);
                listeners.clear();
                dependencies.clear();
            }
        }

        // Unsubscribe from all dependencies
        for (DataFormula formula : dependencies_clone) {
            formula.unsubscribe(this);
        }

        // Inform listeners
        for (DataValueChangedCallbackListener listener : listeners_clone) {
            listener.formulaDeleted(this);
        }
    }
    
    public static class Values extends HashMap<String,DataValue> {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public DataValue get(String key) {
            return super.get(key);
        }

        public double doubleValue(String key) {
            DataValue dvalue = get(key);
            if (dvalue == null) {
                return 0.0;
            } else {
                return dvalue.getDoubleValue();
            }
        }

        public String stringValue(String key) {
            DataValue svalue = get(key);
            if (svalue == null) {
                return "";
            } else {
                return svalue.getStringValue();
            }
        }

        public boolean booleanValue(String key) {
            DataValue bvalue = get(key);
            if (bvalue == null) {
                return false;
            } else {
                return bvalue.getBooleanValue();
            }
        }

        public double credibility(String key) {
            DataValue dvalue = get(key);
            if (dvalue == null) {
                return Constants.NO_CREDIBILITY;
            } else {
                return dvalue.getCredibility();
            }
        }

        // ignore 0.0 unless they're all 0.0
        public double minPositiveCredibility() {
            double minimum = 2.0;
            for (DataValue value : values()) {
                double credibility = value.getCredibility();
                if (credibility > 0.0) {
                    minimum = Math.min(credibility, minimum);
                }
            }
            return minimum > 1.0 ? Constants.NO_CREDIBILITY : minimum;
        }

        public double minCredibility() {
            double minimum = 2.0;
            for (DataValue value : values()) {
                double credibility = value.getCredibility();
                minimum = Math.min(credibility, minimum);
            }
            return minimum > 1.0 ? Constants.NO_CREDIBILITY : minimum;
        }

        public double maxCredibility() {
            double maximum = -1.0;
            for (DataValue value : values()) {
                maximum = Math.max(value.getCredibility(), maximum);
            }
            return maximum < 0.0 ? Constants.NO_CREDIBILITY : maximum;
        }
    }
}
