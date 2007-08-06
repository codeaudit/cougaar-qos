/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

public class Rusage 
{
    public int user_secs;
    public int user_usecs;
    public int sys_secs;
    public int sys_usecs;
    public long lastUpdated;

    public native void getResourceUsage();

    // This is the JNI callback from getResourceUsage()
    public void fill(int user_secs, int user_usecs,
		     int sys_secs, int sys_usecs)
    {
	this.user_secs = user_secs;
	this.user_usecs = user_usecs;
	this.sys_secs = sys_secs;
	this.sys_usecs = sys_usecs;
	lastUpdated = System.currentTimeMillis();
    }

    public void fail()
    {
	System.err.println("Native call failed");
    }

    public void update() 
    {
	getResourceUsage();
    }

    static { System.loadLibrary("RSSUnixUtils"); }

}
