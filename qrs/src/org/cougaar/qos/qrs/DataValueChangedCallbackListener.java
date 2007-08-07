/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * This interface is for objects which want to be notified when the cached value
 * of a DataFormula has changed. A DataSC instance is such an object, as are any
 * DataFormulas which depend on other formulas.
 */
public interface DataValueChangedCallbackListener {
    public void dataValueChanged(DataFormula formula);

    public void formulaDeleted(DataFormula formula);

    public boolean shouldNotify(DataValue value);
}
