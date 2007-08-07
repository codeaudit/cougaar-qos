/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

public class NativeProcess {
    public static native int getProcessID();

    public static native int getProcessPriority(int pid);

    public static native boolean setProcessPriority(int pid, int priority);

    static {
        System.loadLibrary("RSSUnixUtils");
    }
}
