package org.cougaar.lib.quo.rmisocfac;

import java.net.ServerSocket; 
import java.net.Socket; 

class GZIPServerSocket extends ServerSocket {

    public GZIPServerSocket(int port) throws java.io.IOException {
        super(port);
    }

    public Socket accept() throws java.io.IOException {
        Socket s = new GZIPSocket();
        implAccept(s);
        return s;
    }
}
