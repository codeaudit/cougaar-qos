/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;



import org.cougaar.core.mts.StandardAspect;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

public class SSLAspect 
    extends StandardAspect

{

    public Object getDelegate(Object object, Class type) 
    {
	if (type == Socket.class) {
	    Socket socket = (Socket) object;
	    String host = socket.getInetAddress().getHostName();
	    int port = socket.getPort();
	    try {
		return SSLSocketFactory.getDefault().createSocket(host, port);
	    } catch (IOException ex) {
		loggingService.error(null, ex);
		return null;
	    }
	} else if (type == ServerSocket.class) {
	    ServerSocket socket = (ServerSocket) object;
	    int port = 0; // socket.getLocalPort();
	    try {
		// I think we need to close the old socket and open
		// an entirely new one.
		socket.close();
		return SSLServerSocketFactory.getDefault().createServerSocket(port);
	    } catch (IOException ex) {
		loggingService.error(null, ex);
		return null;
	    }
	} else {
	    return null;
	}
    }

}
