package org.cougaar.lib.quo.rmisocfac;

public class Accumulator implements java.io.Serializable
{
    private long count;

    public Accumulator() {
	count = 0;
    }

    public void reset() {
	count = 0;
    }

    public void increment(int inc) {
	count += inc;
    }

    public long getCount() {
	return count;
    }

}
