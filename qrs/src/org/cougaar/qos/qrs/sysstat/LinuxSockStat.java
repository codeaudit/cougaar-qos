/*
 * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

public class LinuxSockStat 
    extends SysStatHandler
{
    private static final String TcpLineKey = "TCP: inuse ";
    private static final String UdpLineKey = "UDP: inuse ";

    private FileReader fr;
    private BufferedReader br;
    private String tcp_key, udp_key;

    public void initialize(String host, int pid) 
    {
	tcp_key =  "Host" + KEY_SEPR + host + 
	    KEY_SEPR + "Network" + 
	    KEY_SEPR + "TCP" +
	    KEY_SEPR + "sockets" + 
	    KEY_SEPR + "inuse";
	udp_key =  "Host" + KEY_SEPR + host + 
	    KEY_SEPR + "Network" + 
	    KEY_SEPR + "UDP" +
	    KEY_SEPR + "sockets" + 
	    KEY_SEPR + "inuse";
    }

    private void close() 
    {
	try { br.close(); } catch (java.io.IOException ioe_ex) {}
    }

    private DataValue parse(String line_key, String line)
    {
	int start = line_key.length();
	int end = line.indexOf(' ', start);
	String doub = null;
	if (end == -1) 
	    doub = line.substring(start);
	else
	    doub = line.substring(start, end);
	return new DataValue(Double.parseDouble(doub),
			     SECOND_MEAS_CREDIBILITY,
			     "",
			     PROVENANCE);
    }

    public void getData(Map map) 
    {
	String line = null;
	fr = null;
	try {
	    fr = new FileReader("/proc/net/sockstat");
	} catch (java.io.FileNotFoundException fnf_ex) {
	    return;
	}

	br = new BufferedReader(fr);

	while (true) {
	    try {
		line = br.readLine();
		if (line == null) break;
		if (line.startsWith(TcpLineKey)) {
		    map.put(tcp_key, parse(TcpLineKey, line));
		} else if (line.startsWith(UdpLineKey)) {
		    map.put(udp_key, parse(UdpLineKey, line));
		}
	    } catch (java.io.IOException io_ex) {
		close();
		return;
	    }
	}

	close();
    }

}
