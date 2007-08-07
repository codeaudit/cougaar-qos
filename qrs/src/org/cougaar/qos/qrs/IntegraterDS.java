/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;

/**
 * A standard ResourceContext type, which listens on all known DataFeeds and
 * selects the 'best' value by maximizing validity. The only available formula
 * is 'Formula'.
 */
public class IntegraterDS extends ResourceContext {

    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new IntegraterDS(parameters, parent);
            }

        };
        registerContextInstantiater("Integrater", cinst);
    }

    private static final String KEY = "key";

    private static Logger logger = Logging.getLogger(IntegraterDS.class);

    protected IntegraterDS(String[] keys, ResourceContext parent) throws ParameterError {
        super(keys, parent);
    }

    // Host Integraters can be the first element in a path. They have
    // no parent or context other than the root.
    protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("Formula")) {
            return new Formula();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain one object, a String, which will be the key
     * used to register this context as a listener on the various DataFeeds.
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length == 0) {
            throw new ParameterError("IntegraterDS: no parameters");
        }
        String key = parameters[0];
        bindSymbolValue(KEY, key.intern());
    }

    /**
     * Standard formula for the IntegraterDS.
     */
    public static class Formula extends DataFormula implements DataFeedListener {
        private DataFeed bestFeed;
        private String key;

        public Formula() {
        }

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            this.key = (String) context.getValue(KEY);
            java.util.Enumeration e = RSS.instance().feeds();
            while (e.hasMoreElements()) {
                DataFeed feed = (DataFeed) e.nextElement();
                if (logger.isDebugEnabled()) {
                    logger.debug("Subscribing to " + feed);
                }
                feed.addListenerForKey(this, key);
            }
            synchronized (this) {
                rescanFeeds();
            }
        }

        // No explicit calculation is ever needed here
        protected DataValue doCalculation(DataFormula.Values values) {
            return getCachedValue();
        }

        void rescanFeeds() {
            rescanFeeds(getCachedValue());
        }

        private void rescanFeeds(DataValue old) {
            DataValue best = old;
            java.util.Enumeration e = RSS.instance().feeds();
            while (e.hasMoreElements()) {
                DataFeed feed = (DataFeed) e.nextElement();
                DataValue current = feed.lookup(key);
                DataValue new_best = DataValue.mostCredible(current, best);
                if (new_best != best) {
                    best = new_best;
                    bestFeed = feed;
                }
            }

            if (best != old) {
                setCachedValue(best);
            }
        }

        /**
         * When new data arrives from any feed, the cached 'best' value will be
         * updated. If the new data comes from the same feed that previously
         * supplied the 'best' value, and if the validity of the new data is
         * greater than or equal to the validity the previous best, the new data
         * becomes the best data. If the new data comes from the same feed that
         * previously supplied the 'best' value, and if the validity of the new
         * data is less than the validity the previous best, the feeds are
         * scanned for a new best. If the new data comes from the different feed
         * than the one that previously supplied the 'best' value, and if the
         * validity of the new data is higher than that of the previos best, the
         * new data becomes the best data. Otherwise the previous stays best.
         */
        public synchronized void newData(DataFeed store, String key, DataValue data) {
            if (logger.isDebugEnabled()) {
                logger.debug("Integrater for " + key + " got new data " + data + " from "
                        + store.getName());
            }
            DataValue old = getCachedValue();
            DataValue most = DataValue.mostCredible(old, data);
            if (store == bestFeed) {
                if (data == most) {
                    // Validity of "best feed" has gone up.
                    setCachedValue(data);
                } else {
                    // Validity of "best feed" has gone down. Rescan to
                    // find a new 'best'.
                    if (data.getCredibility() == 0) {
                        // Ordinarily this would be a signal that the
                        // value has timed out and is no longer valid.
                        rescanFeeds(DataValue.NO_VALUE);
                    } else {
                        rescanFeeds(data);
                    }
                }
            } else {
                if (data == most) {
                    // A different feed is now "best"
                    bestFeed = store;
                    setCachedValue(data);
                } else {
                    // Less valid data from a feed that's not the current
                    // 'best' -- no action required.
                }
            }
        }

    }

}
