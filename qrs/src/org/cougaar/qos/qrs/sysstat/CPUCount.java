/*
 * =====================================================================
 * (c) Copyright 2006   BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.util.Map;

import org.cougaar.qos.qrs.DataValue;


public class CPUCount 
    extends SysStatHandler
{
    private String key;

    public void initialize(String host, int pid) {
	key = "Host" + KEY_SEPR + host + 
	    KEY_SEPR + "CPU" + KEY_SEPR + "count";
    }


    public void getData(Map map) {
	int cpuCount = Runtime.getRuntime().availableProcessors();
	map.put(key, 
		new  DataValue(cpuCount,
			       SECOND_MEAS_CREDIBILITY,
			       "",
			       PROVENANCE));
    }

}
