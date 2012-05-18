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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.util.log.Logger;

/**
 * Abstract definition of a contect for resource data, ie a collection of
 * attributes bound over a set of parameter values. The attributes are
 * implemented as DataFormula instances.
 */
abstract public class ResourceContext implements Constants {
    private static Map<String, ContextInstantiater> ContextInstantiaters = 
        new HashMap<String, ContextInstantiater>();

    private ResourceContext parent;
    private final Map<String, Map<Object, ResourceContext>> children;
    private final Map<String, List<DataFormula>> formulas;
    private final Map<String, Object> symbol_table;
    private final String[] parameters;
    private Object id;
    private String kind;
    private String pretty_name;

    /**
     * Standard constructor. It verifies the parameters and adds the context to
     * the list of known contexts.
     */
    protected ResourceContext(String[] parameters, ResourceContext parent) throws ParameterError {
        this.parameters = parameters;
        this.parent = parent;
        children = new HashMap<String, Map<Object, ResourceContext>>();
        formulas = new HashMap<String, List<DataFormula>>();
        symbol_table = new HashMap<String, Object>();
        verifyParameters(parameters);
    }
    
    /**
     * Subclasses should provide this method. It should verify that the given
     * parameters are correct in number, position, type and value, throwing
     * ParameterError if not.
     */
    abstract protected void verifyParameters(String[] parameters) throws ParameterError;

    abstract protected DataFormula instantiateFormula(String formula_kind);

    protected void postInitialize() {
        pretty_name = kind;
        if (parameters != null && parameters.length > 0) {
            pretty_name += "[" + parameters[0];
            for (int i = 1; i < parameters.length; i++) {
                pretty_name += ", " + parameters[i];
            }
            pretty_name += "]";
        }
    }

    @Override
   public String toString() {
        return pretty_name;
    }

    protected List<ResourceNode> getPath() {
        List<ResourceNode> path = null;
        if (parent == null || parent == RSS.instance() || !useParentPath()) {
            path = new ArrayList<ResourceNode>();
        } else {
            path = parent.getPath();
        }
        path.add(new ResourceNode(kind, parameters));
        return path;
    }

    protected Object getID() {
        return id;
    }

    protected void setParent(ResourceContext parent) {
        this.parent = parent;
    }

    public Object getSymbolValue(String symbol) {
        return symbol_table.get(symbol);
    }

    protected void setSymbolValue(String symbol, Object value) {
        if (symbol_table.containsKey(symbol)) {
            symbol_table.put(symbol, value);
        } else if (parent != null) {
            parent.setSymbolValue(symbol, value);
        } else {
            Logger logger = Logging.getLogger(ResourceContext.class);
            logger.error("### Attempt to set value of symbol " + symbol + " which has no binding");
        }
    }

    public void bindSymbolValue(String symbol, Object value) {
        symbol_table.put(symbol, value);
    }

    protected void unbindSymbolValue(String symbol) {
        if (symbol_table.containsKey(symbol)) {
            symbol_table.remove(symbol);
        } else {
            Logger logger = Logging.getLogger(ResourceContext.class);
            logger.error("### No binding for symbol " + symbol);
        }
    }

    protected void invoke(String method, String[] parameters) throws NoSuchMethodException {
        throw new NoSuchMethodException(method, parameters);
    }

    // No-op by default. Subclasses should provide the
    // context-specific interpretation of the dependencies.
    protected void setDependencies(ResourceNode[] dependencies) {
    }

    

    // private DataFormula instantiateFormula(String formula_kind)
    // {
    // ContextInstantiater instantiater = null;
    // synchronized (ContextInstantiaters) {
    // instantiater = (ContextInstantiater)
    // ContextInstantiaters.get(kind);
    // }
    // if (instantiater != null)
    // return instantiater.instantiateFormula(formula_kind);
    // else
    // return null;
    // }

    private ResourceContext instantiateContext(String kind, String[] parameters) {
        ContextInstantiater instantiater = null;
        synchronized (ContextInstantiaters) {
            instantiater = ContextInstantiaters.get(kind);
        }
        if (instantiater != null) {
            try {
                return instantiater.instantiateContext(parameters, this);
            } catch (ParameterError error) {
                Logger logger = Logging.getLogger(ResourceContext.class);
                logger.error(null, error);
                return null;
            }
        } else {
            Logger logger = Logging.getLogger(ResourceContext.class);
            logger.error(kind + " is not a known ResourceContext");
            // Thread.dumpStack();
            return null;
        }
    }

    Object getIdentifier(String kind, String[] parameters) {
        ContextInstantiater instantiater = null;
        synchronized (ContextInstantiaters) {
            instantiater = ContextInstantiaters.get(kind);
        }
        if (instantiater != null) {
            return instantiater.identifyParameters(parameters);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of the classes of any ResourceContexts that have been
     * instantiated so far. This is only used by the ResourceContext gui.
     */
    public List<String> getContextClasses() {
        synchronized (children) {
            List<String> classes = new ArrayList<String>();
            classes.addAll(children.keySet());
            return classes;
        }
    }

    /**
     * Returns a list of the ResourceContexts for a given class that have been
     * instantiated so far. This is only used by the ResourceContext gui.
     */
    public List<ResourceContext> getContextsForClass(String c) {
        List<ResourceContext> result = new ArrayList<ResourceContext>();
        synchronized (children) {
            Map<Object, ResourceContext> context_map = children.get(c);
            result.addAll(context_map.values());
        }
        return result;
    }

    protected String getContextKind() {
        return kind;
    }

    protected boolean useParentPath() {
        return true;
    }

    // This method is a hook which allows ResourceContexts that are not at
    // the root of the KB to appear as the first element in a path.
    // In that case, the ResourceContext is resposible for finding or
    // constructing the context in which it belongs. ResourceContexts which
    // should never be the first element in a path should return null.
    protected ResourceContext preferredParent(RSS root) {
        return null;
    }

    protected void removeChild(ResourceContext child) {
        String kind = child.getContextKind();
        synchronized (children) {
            Map<Object, ResourceContext> kids = children.get(kind);
            if (kids != null) {
                kids.remove(child.id);
            }
        }
    }

    protected void addChild(Object id, ResourceContext child) {
        String kind = child.getContextKind();
        synchronized (children) {
            Map<Object, ResourceContext> kids = children.get(kind);
            if (kids == null) {
                kids = new HashMap<Object, ResourceContext>();
                children.put(kind, kids);
            }
            kids.put(id, child);
        }
    }

    protected ResourceContext resolveSpec(String kind, String[] parameters) {
        Object id = getIdentifier(kind, parameters);
        return resolveSpec(id, kind, parameters);
    }

    ResourceContext resolveSpec(Object id, String kind, String[] parameters) {
        synchronized (children) {
            Map<Object, ResourceContext> kids = children.get(kind);
            if (kids != null) {
                ResourceContext context = kids.get(id);
                if (context != null) {
                    return context;
                }
            }

            // include parent as argument
            ResourceContext context = instantiateContext(kind, parameters);
            if (context == null) {
                return null;
            } else {
                // cache in children hashtable
                context.id = id;
                context.kind = kind;
                context.postInitialize();
                addChild(id, context);
                RSS.instance().eventNotification(context, RSS.Event.CREATION_EVENT);
                // callback hook
                return context;
            }
        }
    }

    /**
     * ResourceContext factory method, arbitrary depth.
     */
    public DataFormula getPathFormula(ResourceNode[] path) {
        Logger logger = Logging.getLogger(ResourceContext.class);
        if (path == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Path is null");
            }
            return null;
        }

        ResourceContext context = this;
        ResourceNode node;
        // The last item is the formula
        for (int i = 0; i < path.length - 1; i++) {
            node = path[i];
            context = context.resolveSpec(node.kind, node.parameters);
            if (logger.isDebugEnabled()) {
                logger.debug("context " + context);
            }
            if (context == null) {
                return null;
            }
        }
        node = path[path.length - 1];

        DataFormula result = context.getFormula(node.kind, node.parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Formula " + result);
        }
        return result;
    }

    public ResourceContext getPathContext(ResourceNode[] path) {
        if (path == null) {
            return null;
        }
        ResourceContext context = this;
        ResourceNode node;
        // The last item is the formula
        for (ResourceNode element : path) {
            node = element;
            context = context.resolveSpec(node.kind, node.parameters);
            if (context == null) {
                return null;
            }
        }
        return context;
    }

    protected Object nextLookup(String symbol, String[] args, boolean resolve) {
        if (parent != null) {
            return parent.lookupSymbol(symbol, args, resolve);
        } else {
            return null;
        }
    }

    protected Object lookupSymbol(String symbol, String[] args, boolean resolve) {
        Logger logger = Logging.getLogger(ResourceContext.class);
        if (resolve) {
            // Check parameter bindings first if we're resolving.
            Object value = getSymbolValue(symbol);
            // if (logger.isInfoEnabled())
            // logger.info(this +" got "+ value +" for " +symbol);
            if (value != null) {
                return value;
            }
        }
        // No parameter binding (or we're not resolving). Look for a
        // local formula.
        DataFormula formula = findOrMakeLocalFormula(symbol, args);
        if (formula == null) {
            Object value = nextLookup(symbol, args, resolve);
            if (!resolve) {
                DataFormula parentFormula = (DataFormula) value;
                if (parentFormula == null) {
                    return null;
                } else {
                    // Make a local proxy and return that
                    return makeProxyFormula(parentFormula, symbol, args);
                }
            } else {
                return value;
            }
        } else if (resolve) {
            return formula.query();
        } else {
            if (logger.isInfoEnabled()) {
                logger.info(this + " got " + formula + " for " + symbol);
            }
            return formula;
        }
    }

    public Object getValue(String symbol) {
        return lookupSymbol(symbol, null, true);
    }

    public DataFormula getFormula(String kind, String[] args) {
        return (DataFormula) lookupSymbol(kind, args, false);
    }

    /**
     * Return the parent of this.
     */
    public ResourceContext getParent() {
        return parent;
    }

    /**
     * Returns an list of Formula classes instantiated so far by this context.
     * Only used by the ResourceContext gui.
     */
    public List<String> getFormulaKinds() {
        synchronized (formulas) {
            List<String> kinds = new ArrayList<String>();
            kinds.addAll(formulas.keySet());
            return kinds;
        }
    }

    /**
     * Returns a list of the Formulas for a given class that have been
     * instantiated so far. This is only used by the ResourceContext gui.
     */
    public List<DataFormula> getFormulasForKind(String kind) {
        synchronized (formulas) {
            return formulas.get(kind);
        }
    }

    /**
     * Returns a list of the parameter bindings. So far this is only used by the
     * ResourceContext gui.
     */
    public String[] getParameters() {
        return parameters;
    }

    protected boolean paramEqual(Object x, Object y) {
        return x.equals(y);
    }

    private DataFormula makeFormula(String formula_name, String[] formula_args) {
        DataFormula formula = instantiateFormula(formula_name);
        if (formula != null) {
            initializeFormula(formula, formula_name, formula_args);
            return formula;
        } else {
            Logger logger = Logging.getLogger(ResourceContext.class);
            if (logger.isInfoEnabled()) {
                logger.info(this + " failed to lookup " + formula_name);
            }
            return null;
        }
    }

    private void initializeFormula(DataFormula formula, String name, String[] args) {
        formula.setName(name);
        formula.setArgs(args);
        formula.initialize(this);
        formula.postInitialize();
    }

    /**
     * Returns a formula from the cache for the given name, or makes one (and
     * caches it) if none is cached. In the second case the 'makeFormula()'
     * method is used to create a new DataFormula.
     */
    private DataFormula findOrMakeLocalFormula(String name, String[] args) {
        synchronized (formulas) {
            DataFormula formula = null;
            List<DataFormula> forms = formulas.get(name);
            if (forms == null) {
                forms = new ArrayList<DataFormula>();
                formulas.put(name, forms);
            } else {
                
                for (DataFormula candidate : forms) {
                    if (candidate.hasArgs(args)) {
                        return candidate;
                    }
                }
            }

            // No extant formula
            formula = makeFormula(name, args);
            if (formula != null) {
                forms.add(formula);
            }

            return formula;
        }
    }

    private DataFormula makeProxyFormula(DataFormula formula, String name, String[] args) {
        Logger logger = Logging.getLogger(ResourceContext.class);
        if (formula == null) {
            logger.error("### Null delegate for " + name);
            return null;
        } else {
            synchronized (formulas) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Making proxy for " + formula + " in context " + this);
                }
                ProxyFormula proxy = new ProxyFormula(formula);
                initializeFormula(proxy, name, args);
                List<DataFormula> forms = formulas.get(name);
                forms.add(proxy);
                return proxy;
            }
        }
    }

    protected void reinitializeFormulas() {
        synchronized (formulas) {
            for (List<DataFormula> forms : formulas.values()) {
                for (DataFormula formula : forms) {
                    formula.reinitialize();
                }
            }
        }
    }

    protected void delete() {
        parent.removeChild(this);
        Logger logger = Logging.getLogger(ResourceContext.class);

        // Delete child children
        RSS rss = RSS.instance();
        synchronized (children) {
            for (Map<Object, ResourceContext> kids : children.values()) {
                if (kids != null) {
                    Iterator<Map.Entry<Object,ResourceContext>> ctxt_itr = kids.entrySet().iterator();
                    while (ctxt_itr.hasNext()) {
                        Map.Entry<Object, ResourceContext> child = ctxt_itr.next();
                        ResourceContext child_ctxt = child.getValue();
                        ctxt_itr.remove();
                        rss.deleteContext(child_ctxt);
                    }
                }
            }
        }

        // Delete formulas
        synchronized (formulas) {
            for (Map.Entry<String, List<DataFormula>> entry : formulas.entrySet()) {
                if (logger.isDebugEnabled()) {
                    Object key = entry.getKey();
                    logger.debug("Walking through " + key + " formulas for deletion");
                }
                List<DataFormula> forms = entry.getValue();
                if (forms != null) {
                    for (DataFormula formula : forms) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(this + " has informed formula " + formula
                                    + " that the context is deleted");
                        }
                        formula.contextDeleted();
                    }
                }
            }
        }
    }
    
    public static void registerContextInstantiater(String kind, ContextInstantiater instantiater) {
        synchronized (ContextInstantiaters) {
            ContextInstantiaters.put(kind, instantiater);
        }
    }

    /**
     * Exception class used to indicate the one of the supplied parameters is
     * missing, has the wrong type or has an unacceptable value.
     */
    public static class ParameterError extends Exception {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public ParameterError(String message) {
            super(message);
        }
    }

    public static class NoSuchMethodException extends Exception {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;
      private final String method;
        private final String[] args;

        public NoSuchMethodException(String method, String[] args) {
            super("No such Context method as " + method);
            this.method = method;
            this.args = args;
        }

        public String getMethod() {
            return method;
        }

        public String[] getArgs() {
            return args;
        }
    }
}
