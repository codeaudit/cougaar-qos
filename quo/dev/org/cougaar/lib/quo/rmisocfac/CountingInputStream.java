package org.cougaar.lib.quo.rmisocfac;

import java.io.FilterInputStream;
import java.io.InputStream;

class CountingInputStream extends FilterInputStream
{
    private Accumulator accumulator;

    public CountingInputStream(InputStream in, Accumulator accumulator)
    {
        super(in);
	this.accumulator = accumulator;
	accumulator.reset();
    }
 
    public int read() throws java.io.IOException {
	int b = in.read();
	if (b > 0) accumulator.increment(1);
	System.out.println("Read " + accumulator.getCount() + " bytes");
	return b;
    }

    public int read(byte b[], int off, int len) throws java.io.IOException {
	int bytes = in.read(b, off, len);
	if (bytes > 0) accumulator.increment(bytes);
	System.out.println("Read " + accumulator.getCount() + " bytes");
	return bytes;
    }

    public int read(byte b[]) throws java.io.IOException {
	int bytes = in.read(b);
	if (bytes > 0) accumulator.increment(bytes);
	System.out.println("Read " + accumulator.getCount() + " bytes");
	return bytes;
    }



}
