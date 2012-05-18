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

import org.cougaar.util.log.Logger;

/**
 * This a 'rate-ifier' abstraction. It's designed to accumulate calculated
 * instantanteous data so that a periodic poller can supply a rate to the
 * client. The period is supplied as an argument. For now the assumption is that
 * raw values are doubles. Instantiate subclasses must provide two methods:
 * configureDependencies(), to set up any dependencies on other formulas, and
 * computeValueFromDependencies, which should calculate a new value given a
 * collection of dependency values.
 * 
 * A useful extension is SingleKeyPollingIntegral.java, a PollingIntegral which
 * relies on a single DataFeed value.
 */
abstract class PollingIntegral extends DataFormula {
    private double lastValue;
    private long lastTime; // not necessarily == lastValue.timestamp
    private double runningIntegral, lastRunningIntegral;
    private double credibility;
    private boolean pollingValid;
    private DataFormula alarm;
    private int period;
    private Logger logger;

    protected abstract void configureDependencies();

    protected abstract DataValue computeValueFromDependencies(Values values);

    @Override
   protected void setArgs(String[] args) {
        if (args == null || args.length == 0) {
            throw new RuntimeException("No period supplied to PollingIntegral");
        }
        super.setArgs(args);
        this.period = Integer.parseInt(args[0]);
    }

    @Override
   protected void initialize(ResourceContext context) {
        super.initialize(context);
        lastTime = 0;
        configureDependencies();
        String[] parameters = {};
        ResourceContext dependency = RSS.instance().resolveSpec("Alarm", parameters);
        alarm = registerDependency(dependency, "AlarmFormula", getArgs(), "Alarm");
        logger = Logging.getLogger(PollingIntegral.class);
    }

    // Note that this doesn't return the value of the formula itself.
    // The value returned here will simply be included in the
    // integral, via newValue(). Only the poller alters the actual
    // formula value.
    // 
    @Override
   protected DataValue doCalculation(Values values) {
        return computeValueFromDependencies(values);
    }

    // Some value I depend on has changed. Backward chain, but don't
    // notify listeners!
    @Override
   public void dataValueChanged(DataFormula changedFormula) {
        if (changedFormula == alarm) {
            pollUpdate();
        } else {
            DataValue newValue = computeValue(false);
            newValue(newValue);
        }
    }

    // No forward-chaining here, just use the cache
    @Override
   public DataValue blockingQuery() {
        return getCachedValue();
    }

    private void integrate(long time) {
        long duration = time - lastTime;
        runningIntegral += duration * lastValue;
        lastTime = time;
    }

    private boolean noData() {
        return lastTime == 0.0;
    }

    private synchronized void newValue(DataValue value) {
        if (logger.isDebugEnabled()) {
            logger.debug("newValue " + value.getDoubleValue());
        }
        if (noData()) {
            // first call, no valid timestamp yet
            runningIntegral = 0.0;
            lastRunningIntegral = 0.0;
            lastTime = value.getTimestamp();
        } else {
            integrate(value.getTimestamp());
        }
        credibility = value.getCredibility(); // this is arbitrary, fix later
        lastValue = value.getDoubleValue();
    }

    private void pollUpdate() {
        synchronized (this) {
            if (noData()) {
                // No callbacks have arrived yet. No-op
                pollingValid = false;
            } else if (!pollingValid) {
                // First poll with valid data; too early to compute an average
                // Update runningIntegral with lastValue until now
                integrate(System.currentTimeMillis());
                lastRunningIntegral = runningIntegral;
                pollingValid = true;
            } else {
                // update runningIntegral with lastValue until now
                integrate(System.currentTimeMillis());
                double avg = (runningIntegral - lastRunningIntegral) / period;
                if (logger.isDebugEnabled()) {
                    logger.debug("pollUpdate:" + " last=" + lastRunningIntegral + " running="
                            + runningIntegral + " avg= " + avg);
                }
                lastRunningIntegral = runningIntegral;
                setCachedValue(new DataValue(avg, credibility));
            }
        }

    }

}
