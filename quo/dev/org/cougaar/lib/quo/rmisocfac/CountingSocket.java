package org.cougaar.lib.quo.rmisocfac;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
 
class CountingSocket extends Socket 
{

    private CountingInputStream in;
    private CountingOutputStream out;
    private Accumulator accumulator;

    public CountingSocket(Accumulator accumulator) { 
	super(); 
	this.accumulator = accumulator;
    }

    public CountingSocket(String host, int port, Accumulator accumulator) 
	throws java.io.IOException 
    {
        super(host, port);
	this.accumulator = accumulator;
    }

    public InputStream getInputStream() throws java.io.IOException {
        if (in == null) {
	    in = new CountingInputStream(super.getInputStream(),
					 accumulator);
        }
        return in;
    }

    public OutputStream getOutputStream() throws java.io.IOException {
        if (out == null) {
	    out = new CountingOutputStream(super.getOutputStream(),
					   accumulator);
        }
        return out;
    }

    public synchronized void close() throws java.io.IOException {
        OutputStream o = getOutputStream();
        o.flush();
	// if (out != null) out.report();
	// if (in != null) in.report();
	super.close();
    }
}
