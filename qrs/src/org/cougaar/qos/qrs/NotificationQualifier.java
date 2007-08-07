/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

public interface NotificationQualifier {
    boolean shouldNotify(DataValue value);
}
