/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import java.io.*;

class SimpleObjectOutputStream extends OutputStream
{
    private ObjectOutput out;

    SimpleObjectOutputStream(ObjectOutput out) {
	this.out = out;
    }

    public void close()
	throws IOException
    {
	out.close();
    }


    public void flush() 
	throws IOException
    {
	out.flush();
    }

    public void write(int b)
	throws IOException
    {
	out.write(b);
    }

    public void write(byte[] b)
	throws IOException
    {
	out.write(b);
    }

    public void write(byte[] b, int off, int len)
	throws IOException
    {
	out.write(b, off, len);
    }


}
