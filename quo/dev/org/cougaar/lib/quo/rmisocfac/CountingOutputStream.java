package org.cougaar.lib.quo.rmisocfac;

import java.io.FilterOutputStream;
import java.io.OutputStream;

class CountingOutputStream extends FilterOutputStream
{
    private Accumulator accumulator;

    public CountingOutputStream(OutputStream out, Accumulator accumulator) 
    {
        super(out);
	this.accumulator = accumulator;
	accumulator.reset();
    }
 
    public void write(int b) throws java.io.IOException {
	accumulator.increment(1);
	System.out.println("Wrote " + accumulator.getCount() + " bytes");
	out.write(b);
    }

    public void write(byte b[], int off, int len) throws java.io.IOException {
	accumulator.increment(len);
	System.out.println("Wrote " + accumulator.getCount() + " bytes");
	out.write(b, off, len);
    }

    public void write(byte b[]) throws java.io.IOException {
	accumulator.increment(b.length);
	System.out.println("Wrote " + accumulator.getCount() + " bytes");
	out.write(b);
    }


}
