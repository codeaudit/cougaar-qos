package org.cougaar.lib.quo.rmisocfac; 

import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class ClientSocketFactory 
    implements RMIClientSocketFactory, Serializable 
{ 
    
    private String type;
    private SSLSocketFactory ssf;
    private Accumulator accumulator;

    public ClientSocketFactory (String type, Accumulator accumulator) {
	this.type = type;
	this.accumulator = accumulator;
    }

    private void ensure_ssf() {
	if (ssf == null) 
	    ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
    }

    public Socket createSocket(String host, int port) 
	throws java.io.IOException 
    { 
	Socket socket = null;
	if (type == null) {
	    socket = new Socket(host, port);
	} else if (type.equals("gzip")) {
	    socket = new GZIPSocket(host, port); 
	    System.out.println("### made gzip socket");
	} else if (type.equals("compression")) {
	    socket = new CompressionSocket(host, port); 
	    System.out.println("### made compression socket");
	} else if (type.equals("ssl")) {
	    ensure_ssf();
	    socket = ssf.createSocket(host, port);
	    System.out.println("### made ssl socket");
	} else if (type.equals("count")) {
	    socket = new CountingSocket(host, port, accumulator); 
	    System.out.println("### made counting socket");
	} else {
	    System.err.println("Unhandled socket type " + type);
	}
	return socket; 
    } 

} 
      
