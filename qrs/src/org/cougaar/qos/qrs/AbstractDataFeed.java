/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * Base implementation of DataFeed that handles naming (and nothing else).
 */
abstract public class AbstractDataFeed implements DataFeed {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
