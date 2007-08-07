/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import org.cougaar.qos.qrs.RSS;

public class ResourceContextRoot extends ResourceContextInstanceNode {

    public ResourceContextRoot() {
        super(null, RSS.instance());
        // updateChildren();
    }

}
