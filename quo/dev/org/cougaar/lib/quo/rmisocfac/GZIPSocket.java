package org.cougaar.lib.quo.rmisocfac;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
 
class GZIPSocket extends Socket {

    private InputStream in;
    private OutputStream out;

    public GZIPSocket() { super(); }

    public GZIPSocket(String host, int port) 
	throws java.io.IOException 
    {
        super(host, port);
    }

    public InputStream getInputStream() throws java.io.IOException {
        if (in == null) {
	    in = new GZIPInputStream(super.getInputStream());
        }
        return in;
    }

    public OutputStream getOutputStream() throws java.io.IOException {
        if (out == null) {
	    out = new GZIPOutputStream(super.getOutputStream());
        }
        return out;
    }

    public synchronized void close() throws java.io.IOException {
        OutputStream o = getOutputStream();
        o.flush();
	super.close();
    }
}
