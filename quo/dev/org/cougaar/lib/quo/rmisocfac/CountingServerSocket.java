package org.cougaar.lib.quo.rmisocfac;

import java.net.ServerSocket; 
import java.net.Socket; 

class CountingServerSocket extends ServerSocket 
{
    
    private Accumulator accumulator;
    public CountingServerSocket(int port, Accumulator accumulator)
	throws java.io.IOException 
    {
        super(port);
	this.accumulator = accumulator;
    }

    public Socket accept() throws java.io.IOException {
        Socket s = new CountingSocket(accumulator);
        implAccept(s);
        return s;
    }
}
