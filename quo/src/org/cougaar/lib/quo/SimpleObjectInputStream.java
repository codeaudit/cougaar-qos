/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import java.io.*;

class SimpleObjectInputStream extends InputStream
{
    private ObjectInput in;

    SimpleObjectInputStream(ObjectInput in) {
	this.in = in;
    }

    public int available() 
	throws IOException
    {
	return in.available();
    }

    public void close() 
	throws IOException
    {
	in.close();
    }

    public boolean markSupported() {
	return false;
    }

    public int read() 
	throws IOException 
    {
	return in.read();
    }

    public int read(byte[] b) 
	throws IOException 
    {
	return in.read(b);
    }

    public int read(byte[] b, int off, int len)
	throws IOException
    {
	return in.read(b, off, len);
    }

    public synchronized void reset() 
	throws IOException
    {
    }

    public long skip (long n)
	throws IOException
    {
	return in.skip(n);
    }

}
