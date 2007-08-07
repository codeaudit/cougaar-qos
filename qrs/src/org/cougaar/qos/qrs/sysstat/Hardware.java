/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

public class Hardware {

    public static native long getClockSpeed();

    public static native String getHostName();

    static {
        System.loadLibrary("RSSUnixUtils");
    }
}
