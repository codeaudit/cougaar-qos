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
    @Override
   protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    @Override
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

    @Override
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

        @Override
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

        @Override
      void contextDeleted() {
            synchronized (task_lock) {
                if (task != null) {
                    RSSUtils.unschedule(task);
                }
                task = null;
            }
            super.contextDeleted();
        }

        @Override
      protected DataValue doCalculation(Values vals) {
            return getCachedValue();
        }

    }

    public static class OneSecond extends AlarmFormula {
        @Override
      long getPeriod() {
            return 1000;
        }
    }

    public static class FiveSeconds extends AlarmFormula {
        @Override
      long getPeriod() {
            return 5000;
        }
    }

    public static class FifteenSeconds extends AlarmFormula {
        @Override
      long getPeriod() {
            return 15000;
        }
    }

}
