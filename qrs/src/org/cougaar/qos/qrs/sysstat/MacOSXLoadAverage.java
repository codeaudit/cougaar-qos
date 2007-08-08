/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

// MacOS and Solaris have simular uptime
// The pattern and path were tuned so this works for both OSes
public class MacOSXLoadAverage extends SunOSLoadAverage {
    
    /**
     * Override because the OperatingSystemMXBean method is available in osx jre 1.6 beta
     * but kills the jvm when invoked.
     * 
     * XXX: Remove this override once the bean works properly in osx.
     */
    public Double getLoadAvgFromOS() {
        return null;
    }
}
