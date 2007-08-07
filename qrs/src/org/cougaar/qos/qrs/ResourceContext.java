/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;
import org.cougaar.qos.ResourceStatus.ResourceNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract definition of a contect for resource data, ie a collection of
 * attributes bound over a set of parameter values. The attributes are
 * implemented as DataFormula instances.
 */
abstract public class ResourceContext implements Constants {
    /**
     * Exception class used to indicate the one of the supplied parameters is
     * missing, has the wrong type or has an unacceptable value.
     */
    public static class ParameterError extends Exception {
        public ParameterError(String message) {
            super(message);
        }
    }

    public static class NoSuchMethodException extends Exception {
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

    private static HashMap ContextInstantiaters = new HashMap();

    public static void registerContextInstantiater(String kind, ContextInstantiater instantiater) {
        synchronized (ContextInstantiaters) {
            ContextInstantiaters.put(kind, instantiater);
        }
    }

    private ResourceContext parent;
    private final HashMap children;
    private final HashMap formulas;
    private final HashMap symbol_table;
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
        children = new HashMap();
        formulas = new HashMap();
        symbol_table = new HashMap();
        verifyParameters(parameters);
    }

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

    public String toString() {
        return pretty_name;
    }

    protected ArrayList getPath() {
        ArrayList path = null;
        if (parent == null || parent == RSS.instance() || !useParentPath()) {
            path = new ArrayList();
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

    /**
     * Subclasses should provide this method. It should verify that the given
     * parameters are correct in number, position, type and value, throwing
     * ParameterError if not.
     */
    abstract protected void verifyParameters(String[] parameters) throws ParameterError;

    abstract protected DataFormula instantiateFormula(String formula_kind);

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
            instantiater = (ContextInstantiater) ContextInstantiaters.get(kind);
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
            instantiater = (ContextInstantiater) ContextInstantiaters.get(kind);
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
    public ArrayList getContextClasses() {
        synchronized (children) {
            Iterator i = children.entrySet().iterator();
            ArrayList classes = new ArrayList();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                classes.add(entry.getKey());
            }
            return classes;
        }
    }

    /**
     * Returns a list of the ResourceContexts for a given class that have been
     * instantiated so far. This is only used by the ResourceContext gui.
     */
    public ArrayList getContextsForClass(String c) {
        ArrayList result = new ArrayList();
        synchronized (children) {
            HashMap context_map = (HashMap) children.get(c);
            Iterator i = context_map.values().iterator();
            while (i.hasNext()) {
                result.add(i.next());
            }
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
            HashMap kids = (HashMap) children.get(kind);
            if (kids != null) {
                kids.remove(child.id);
            }
        }
    }

    protected void addChild(Object id, ResourceContext child) {
        String kind = child.getContextKind();
        synchronized (children) {
            HashMap kids = (HashMap) children.get(kind);
            if (kids == null) {
                kids = new HashMap();
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
            HashMap kids = (HashMap) children.get(kind);
            if (kids != null) {
                ResourceContext context = (ResourceContext) kids.get(id);
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
                RSS.instance().eventNotification(context, RSS.CREATION_EVENT);
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
    public ArrayList getFormulaKinds() {
        synchronized (formulas) {
            Iterator i = formulas.entrySet().iterator();
            ArrayList kinds = new ArrayList();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                kinds.add(entry.getKey());
            }
            return kinds;
        }
    }

    /**
     * Returns a list of the Formulas for a given class that have been
     * instantiated so far. This is only used by the ResourceContext gui.
     */
    public ArrayList getFormulasForKind(String kind) {
        synchronized (formulas) {
            return (ArrayList) formulas.get(kind);
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
            List forms = (List) formulas.get(name);
            if (forms == null) {
                forms = new ArrayList();
                formulas.put(name, forms);
            } else {
                int length = forms.size();
                DataFormula candidate;
                for (int i = 0; i < length; i++) {
                    candidate = (DataFormula) forms.get(i);
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
                List forms = (List) formulas.get(name);
                forms.add(proxy);
                return proxy;
            }
        }
    }

    protected void reinitializeFormulas() {
        synchronized (formulas) {
            Iterator itr = formulas.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry entry = (Map.Entry) itr.next();
                List forms = (List) entry.getValue();
                for (int i = 0; i < forms.size(); i++) {
                    DataFormula formula = (DataFormula) forms.get(i);
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
            Iterator class_itr = children.entrySet().iterator();
            while (class_itr.hasNext()) {
                Map.Entry entry = (Map.Entry) class_itr.next();
                HashMap kids = (HashMap) entry.getValue();
                if (kids != null) {
                    Iterator ctxt_itr = kids.entrySet().iterator();
                    while (ctxt_itr.hasNext()) {
                        Map.Entry child = (Map.Entry) ctxt_itr.next();
                        ResourceContext child_ctxt = (ResourceContext) child.getValue();
                        ctxt_itr.remove();
                        rss.deleteContext(child_ctxt);
                    }
                }
            }
        }

        // Delete formulas
        synchronized (formulas) {
            DataFormula formula;
            Map.Entry entry;
            List forms;
            int length;
            Iterator class_itr = formulas.entrySet().iterator();
            while (class_itr.hasNext()) {
                entry = (Map.Entry) class_itr.next();
                if (logger.isDebugEnabled()) {
                    Object key = entry.getKey();
                    logger.debug("Walking through " + key + " formulas for deletion");
                }
                forms = (List) entry.getValue();
                if (forms != null) {
                    length = forms.size();
                    for (int i = 0; i < length; i++) {
                        formula = (DataFormula) forms.get(i);
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

}
