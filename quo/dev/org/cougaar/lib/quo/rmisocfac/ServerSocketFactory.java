package org.cougaar.lib.quo.rmisocfac; 

import java.io.FileInputStream;
import java.io.Serializable;
import java.net.ServerSocket; 
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;
import javax.net.ssl.SSLServerSocketFactory;
import javax.security.cert.X509Certificate;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.KeyManagerFactory;

       
public class ServerSocketFactory 
    implements RMIServerSocketFactory, Serializable 
{ 
    
    private String type;
    private String keyfile;
    private String protocol;
    private String algorithm;
    private String store;
    private char[] password;
    private SSLServerSocketFactory ssf;
    private Accumulator accumulator ;

    public ServerSocketFactory (String type,
				String keyfile,
				String protocol,
				String algorithm,
				String store,
				String password,
				Accumulator accumulator)
    {
	this.type = type;
	this.keyfile = keyfile;
	this.protocol = protocol;
	this.algorithm = algorithm;
	this.store = store;
	this.password = password.toCharArray();
	this.accumulator = accumulator;
    }


    private void ensure_ssf() 
	throws java.io.IOException
    {
	if (ssf != null) return;

	try {
	    SSLContext ctx = SSLContext.getInstance(protocol);
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
	    KeyStore ks = KeyStore.getInstance(store);
	    
	    ks.load(new FileInputStream(keyfile), password);
	    kmf.init(ks, password);
	    ctx.init(kmf.getKeyManagers(), null, null);
	    
	    ssf = ctx.getServerSocketFactory();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    public ServerSocket createServerSocket(int port) 
	throws java.io.IOException 
    { 
	ServerSocket socket = null;
	if (type == null) {
	    socket = new ServerSocket(port);
	} else if (type.equals("gzip")) {
	    socket = new GZIPServerSocket(port); 
	    System.out.println("### made gzip server socket");
	} else if (type.equals("compression")) {
	    socket = new CompressionServerSocket(port); 
	    System.out.println("### made compression server socket");
	} else if (type.equals("ssl")) {
	    ensure_ssf();
	    socket = ssf.createServerSocket(port);
	    System.out.println("### made SSL server socket");
	} else if (type.equals("count")) {
	    socket = new CountingServerSocket(port, accumulator);
	} else {
	    System.err.println("Unhandled socket type " + type);
	    System.exit(-1);
	}
	return socket; 
    } 
}
