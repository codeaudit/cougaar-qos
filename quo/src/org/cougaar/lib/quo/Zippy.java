/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import java.io.*;
import java.util.zip.*;

public class Zippy implements Externalizable
{
    public static boolean reportCompression = false;

    private transient Object data;
    private transient Deflater deflater;
    private transient Inflater inflater;


    public Zippy() {
	inflater = new Inflater();
    }

    public Zippy(Object data) {
	this(data, Deflater.BEST_SPEED);
    }

    public Zippy (Object data, int level) {
	this.data = data;
	this.deflater = new Deflater(level);
    }

    public Object getData() {
	return data;
    }

    public int getRawDataSize() {
	return deflater.getTotalIn();
    }

    public int getCompressedDataSize() {
	return deflater.getTotalOut();
    }

    public synchronized void writeExternal(ObjectOutput out) 
	throws IOException
    {
	deflater.reset();
	OutputStream os = null;
	//  	    if (out instanceof ObjectOutputStream)
	//  		os = (ObjectOutputStream) out;
	//  	    else
	//  		os = new SimpleObjectOutputStream(out);

	// Re-using the output stream isn't working in COUGAAR, 
	// so make a new stream each time.
	os = new SimpleObjectOutputStream(out);
	   
	DeflaterOutputStream dos = new DeflaterOutputStream(os, deflater);
	ObjectOutputStream dos_out = new ObjectOutputStream(dos);
	dos_out.writeObject(data);
	dos.finish();
	dos.flush();
    }

    public synchronized void readExternal(ObjectInput in) 
	throws IOException, ClassNotFoundException
    {
	inflater.reset();
	InputStream is = null;
	// 	    if (in instanceof ObjectInputStream) 
	// 		is = (ObjectInputStream) in;
	// 	    else
	// 		is = new SimpleObjectInputStream(in);
	// Re-using the input stream isn't working in COUGAAR, 
	// so make a new stream each time.
	is = new SimpleObjectInputStream(in);

	InflaterInputStream iis = new InflaterInputStream(is);
	ObjectInputStream iis_in = new ObjectInputStream(iis);
	data = iis_in.readObject();
    }


    public static byte[] zip(Object data, Deflater def) {
	byte[] uncompressed = null;

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);
	ObjectOutputStream oos = null;

	try {
	    oos = new ObjectOutputStream(dos);
	    oos.writeObject(data);
	} catch (IOException ioe) {
	    return null;
	}

	try {
	    oos.close();
	} catch (IOException ioe2) {
	}

	byte[] compressed = baos.toByteArray();

	return compressed;
    }

    public static byte[] zip(Object data) {
	return zip(data, Deflater.BEST_COMPRESSION);
    }

    public static byte[] zip(Object data, int level) {
	Deflater def = new Deflater(level);
	return zip(data, def);
    }	


    public static Object unzip(byte[] data) {
	ByteArrayInputStream bais = new ByteArrayInputStream(data);
	InflaterInputStream infis = new InflaterInputStream(bais);
	ObjectInputStream ois = null;
	Object udata = null;

	try {
	    ois = new ObjectInputStream(infis);
	    udata = ois.readObject();
	} catch (IOException ioe) {
	    return null;
	} catch (ClassNotFoundException cnf) {
	    return null;
	}
	
	try {
	    ois.close();
	} catch (IOException ioe2) {
	}

	return udata;
    }



    public static byte[] toByteArray(Object data) {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ObjectOutputStream oos = null;

	try {
	    oos = new ObjectOutputStream(baos);
	    oos.writeObject(data);
	} catch (IOException ioe) {
	    return null;
	}

	try {
	    oos.close();
	} catch (IOException ioe2) {
	}

	return baos.toByteArray();
    }


    public static Object fromByteArray(byte[] data) {
	ByteArrayInputStream bais = new ByteArrayInputStream(data);
	ObjectInputStream ois = null;
	Object udata = null;

	try {
	    ois = new ObjectInputStream(bais);
	    udata = ois.readObject();
	} catch (IOException ioe) {
	    return null;
	} catch (ClassNotFoundException cnf) {
	    return null;
	}
	
	try {
	    ois.close();
	} catch (IOException ioe2) {
	}

	return udata;
    }



}
