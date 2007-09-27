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

import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.util.log.Logger;

import java.util.Observable;
import java.util.Observer;

public class BoundDataFormula extends Observable implements DataValueChangedCallbackListener {
    private Object currentValue;

    private ResourceContext context;
    private String symbol;

    private ResourceNode[] description;
    private DataFormula formula;
    private Runnable poller;
    private boolean isReady;
    private NotificationQualifier qualifier;

    /**
     * Make a forward-chained binding of a context and a symbol in that context.
     * If keepCurrent is true, a thread will be created to retrieve the value
     * periodically. If keepCurrent is false, the value will only be retrieved
     * on request.
     */
    public BoundDataFormula(ResourceContext context, String symbol, boolean keepCurrent) {
        this.symbol = symbol;
        this.context = context;
        isReady = true;
        if (keepCurrent) {
            update();
            poller = new Runnable() {
                public void run() {
                    update();
                }
            };
            RSSUtils.schedule(poller, 0, 1000);
        }
    }

    /**
     * Make a backward-chained binding of a formula. It will be notified when
     * the formula's value changes.
     */
    public BoundDataFormula(DataFormula formula) throws NullFormulaException {
        if (formula == null) {
            throw new NullFormulaException();
        }
        this.formula = formula;
        isReady = true;
        formula.subscribe(this);
        // Initialize the value
        dataValueChanged(formula);
    }

    public BoundDataFormula(ResourceNode[] description) throws NullFormulaException {
        this(description, false, null);
    }

    // NB: The caller is responsible for running the formula creator
    // Runnable if delayed == true!
    public BoundDataFormula(ResourceNode[] description,
                            boolean delayed,
                            NotificationQualifier qualifier) throws NullFormulaException {
        this.description = description;
        this.qualifier = qualifier;
        if (!delayed) {
            DataFormula f = RSS.instance().getPathFormula(description);
            // We now have the formula but haven't subscribed yet. If
            // it's deleted before we subscribe, we lose.

            if (f == null) {
                throw new NullFormulaException();
            }
            setFormula(f);
        } else {
            // we need a valid initial value
            currentValue = DataValue.NO_VALUE;
        }
    }

    void unsubscribe() {
        if (formula != null) {
            formula.unsubscribe(this);
            // formula = null ?
        }
    }

    public boolean shouldNotify(DataValue value) {
        Logger logger = Logging.getLogger(BoundDataFormula.class);
        boolean result = qualifier == null || qualifier.shouldNotify(value);
        if (!result && logger.isInfoEnabled()) {
            logger.info(value + " failed to satisfy " + qualifier);
        }
        return result;
    }

    public Runnable getDelayedFormulaCreator() {
        return new Runnable() {
            public void run() {
                // We now have the formula but haven't subscribed yet. If
                // it's deleted before we subscribe, we lose.
                setFormula(RSS.instance().getPathFormula(description));
            }
        };
    }

    private void setFormula(DataFormula formula) {
        if (isReady) {
            return;
        }
        this.formula = formula;
        isReady = true;
        formula.subscribe(this);
        // Initialize the value
        initialNotify(formula);
    }

    public void formulaDeleted(DataFormula formula) {
        DataFormula replacement = null;
        replacement = RSS.instance().getPathFormula(description);
        // We now have the formula but haven't subscribed yet. If
        // it's deleted before we subscribe, we lose.

        Logger logger = Logging.getLogger(BoundDataFormula.class);
        if (logger.isInfoEnabled()) {
            logger.info("Replaced deleted formula " + formula + " with " + replacement);
        }
        this.formula = replacement;
        isReady = true;
        replacement.subscribe(this);
        initialNotify(replacement);
    }

    private void notifyObserversAndLog(Object value) {
        Logger logger = Logging.getEventLogger(BoundDataFormula.class);
        if (logger.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Callback: ");
            buf.append(currentValue);
            if (description != null) {
                buf.append(" for ");
                RSSUtils.pathToString(buf, description);
            }
            logger.info(buf.toString());
        }
        notifyObservers(value);
    }

    private void update() {
        Object raw = context.getValue(symbol);
        setChanged();
        if (raw instanceof DataValue) {
            currentValue = raw;
            notifyObserversAndLog(currentValue);
        } else if (raw instanceof Number) {
            currentValue = new Double(raw.toString());
            notifyObserversAndLog(currentValue);
        } else {
            Logger logger = Logging.getLogger(BoundDataFormula.class);
            logger.error("Symbol " + symbol + " of  " + context + " has value " + raw
                    + " which is not a DataValue");
        }
        clearChanged();
    }

    /**
     * Callback from the DataFormula indicating that the value has changed. This
     * implements the DataValueChangedCallbackListener interface.
     */
    public void dataValueChanged(DataFormula formula) {
        DataValue rawValue = formula.query();
        if (rawValue != null) {
            currentValue = rawValue;
            setChanged();
            notifyObserversAndLog(currentValue);
            clearChanged();
        }
    }

    private void initialNotify(DataFormula formula) {
        DataValue rawValue = formula.query();
        if (rawValue != null && !rawValue.equals(DataValue.NO_VALUE)) {
            currentValue = rawValue;
            if (shouldNotify(rawValue)) {
                setChanged();
                notifyObserversAndLog(currentValue);
                clearChanged();
            }
        }
    }

    public void addObserver(Observer o) {
        super.addObserver(o);
        DataValue current = (DataValue) getCurrentValue();
        if (!current.equals(DataValue.NO_VALUE) && shouldNotify(current)) {
            o.update(this, current);
        }
    }

    public ResourceNode[] getDescription() {
        return description;
    }

    /**
     * Return the current value, updating if necessary
     */
    public Object getCurrentValue() {
        if (context != null && poller == null) {
            // passive forward-chaining -- update manually on request
            update();
        }
        return currentValue;
    }

}
