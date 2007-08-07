/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public class AlarmDS extends ResourceContext implements Constants {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new AlarmDS(parameters, parent);
            }

        };
        registerContextInstantiater("Alarm", cinst);
    }

    // Alarm contexts can be the first element in a path. They have
    // no parent or context other than the root.
    protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("OneSecond")) {
            return new OneSecond();
        } else if (kind.equals("FiveSeconds")) {
            return new FiveSeconds();
        } else if (kind.equals("FifteenSeconds")) {
            return new FifteenSeconds();
        } else if (kind.equals("AlarmFormula")) {
            return new AlarmFormula();
        } else {
            return null;
        }
    }

    protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 0) {
            throw new ParameterError("AlarmDS: wrong number of parameters");
        }
    }

    private AlarmDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    static class AlarmFormula extends DataFormula implements Constants {

        private Object task;
        private final Object task_lock = new Object();
        private long period;

        long getPeriod() {
            return period;
        }

        private void updateCache() {
            setCachedValue(new DataValue(System.currentTimeMillis(), CONFIRMED_MEAS_CREDIBILITY));

        }

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            String[] args = getArgs();
            if (args != null && args.length > 0) {
                period = Integer.parseInt(args[0]);
            }
            updateCache();
            Runnable body = new Runnable() {
                public void run() {
                    updateCache();
                }
            };
            synchronized (task_lock) {
                if (task != null) {
                    RSSUtils.unschedule(task);
                }
                task = RSSUtils.schedule(body, 0, getPeriod());
            }

        }

        void contextDeleted() {
            synchronized (task_lock) {
                if (task != null) {
                    RSSUtils.unschedule(task);
                }
                task = null;
            }
            super.contextDeleted();
        }

        protected DataValue doCalculation(Values vals) {
            return getCachedValue();
        }

    }

    public static class OneSecond extends AlarmFormula {
        long getPeriod() {
            return 1000;
        }
    }

    public static class FiveSeconds extends AlarmFormula {
        long getPeriod() {
            return 5000;
        }
    }

    public static class FifteenSeconds extends AlarmFormula {
        long getPeriod() {
            return 15000;
        }
    }

}
