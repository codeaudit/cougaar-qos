/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.Servant;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

public class CorbaUtils {

    public static ORB orb;
    public static POA poa;
    public static NamingContextExt ns_root;

    private static NamingContext findOrMakeContext(NamingContext parent, NameComponent cname)
            throws NotFound, CannotProceed, InvalidName {
        Logger logger = Logging.getLogger(CorbaUtils.class);
        NameComponent[] name = {cname};
        try {
            parent.bind_new_context(name);
            if (logger.isInfoEnabled()) {
                logger.info("Made context " + cname.id);
            }
        } catch (AlreadyBound ab) {
            // ignore this error
            if (logger.isInfoEnabled()) {
                logger.info("Context " + cname.id + " already exists");
            }
        }
        org.omg.CORBA.Object ref = parent.resolve(name);
        return NamingContextHelper.narrow(ref);
    }

    private static void bind(NamingContext root, NameComponent[] name, org.omg.CORBA.Object ref)
            throws NotFound, CannotProceed, InvalidName, AlreadyBound {
        int context_length = name.length - 1;
        NamingContext context = root;
        for (int i = 0; i < context_length; i++) {
            context = findOrMakeContext(context, name[i]);
        }
        NameComponent[] tname = {name[context_length]};
        context.rebind(tname, ref);
    }

    public static void nsBind(NameComponent[] name, Servant servant) {
        try {
            org.omg.CORBA.Object ref = poa.servant_to_reference(servant);
            nsBind(name, ref);
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(CorbaUtils.class);
            logger.error(null, ex);
        }
    }

    public static void nsBind(NameComponent[] name, org.omg.CORBA.Object ref) {
        ensure_ns();
        if (ns_root == null) {
            return;
        }
        Logger logger = Logging.getLogger(CorbaUtils.class);
        try {
            // Depends on -ORBInitRef having been set properly.
            bind(ns_root, name, ref);
            if (logger.isInfoEnabled()) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(ref.toString());
                buffer.append(" bound to ");
                buffer.append(name[0].id);
                for (int i = 1; i < name.length; i++) {
                    buffer.append("/");
                    buffer.append(name[i].id);
                }
                logger.info(buffer.toString());
            }
        } catch (Exception ex) {
            logger.error(null, ex);
        }
    }

    public static org.omg.CORBA.Object nsResolve(NameComponent[] name) {
        ensure_ns();
        if (ns_root == null) {
            return null;
        }
        Logger logger = Logging.getLogger(CorbaUtils.class);
        org.omg.CORBA.Object ref = null;
        try {
            ref = ns_root.resolve(name);
            if (logger.isInfoEnabled()) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(name[0].id);
                for (int i = 1; i < name.length; i++) {
                    buffer.append("/");
                    buffer.append(name[i].id);
                }
                buffer.append(" resolves to ");
                buffer.append(ref);
                logger.info(buffer.toString());
            }
        } catch (Exception ex) {
            // no stack dump here, since this is often an expected
            // condition
            StringBuffer buffer = new StringBuffer();
            buffer.append(name[0].id);
            for (int i = 1; i < name.length; i++) {
                buffer.append("/");
                buffer.append(name[i].id);
            }
            buffer.append(" not found: ");
            buffer.append(ex.toString());
            logger.error(buffer.toString());
        }
        return ref;
    }

    public static void writeIOR(String iorfile, org.omg.CORBA.Object reference) {
        try {
            FileWriter file = new FileWriter(iorfile);
            PrintWriter writer = new PrintWriter(file);
            String ior = orb.object_to_string(reference);
            writer.println(ior);
            writer.close();
        } catch (java.io.IOException e) {
            Logger logger = Logging.getLogger(CorbaUtils.class);
            logger.error(null, e);
        }
    }

    public static org.omg.CORBA.Object readIOR(String iorfile) {
        org.omg.CORBA.Object reference = null;
        try {
            FileReader reader = new FileReader(iorfile);
            BufferedReader rdr = new BufferedReader(reader);
            String ior = rdr.readLine();
            reference = orb.string_to_object(ior);
            rdr.close();
        } catch (java.io.IOException e) {
            Logger logger = Logging.getLogger(CorbaUtils.class);
            logger.error(null, e);
        }
        return reference;
    }

    public static void registerContexts() {
        AlarmDS.register();
        ClassDS.register();
        HostDS.register();
        IntegraterDS.register();
        IpFlowDS.register();
        MethodDS.register();
        MvaDS.register();
        ObjectDS.register();
        ObjectMethodDS.register();
        ProcessDS.register();
        SiteFlowDS.register();
    }

    static private String hostname;
    static private String hostaddr;

    static {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getCanonicalHostName();
            hostaddr = addr.getHostAddress();
        } catch (Exception ex) {
        }
    }

    public static String hostname() {
        return hostname;
    }

    public static String hostaddr() {
        return hostaddr;
    }

    private static synchronized void ensure_ns() {
        if (ns_root != null) {
            return;
        }
        try {
            org.omg.CORBA.Object o = orb.resolve_initial_references("NameService");
            ns_root = NamingContextExtHelper.narrow(o);
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(CorbaUtils.class);
            logger.warn("Unable to resolve NameService ORBInitRef:" + ex.getMessage());
        }
    }

    public static void makeORB(String[] args) {
        orb = ORB.init(args, null);
        try {
            org.omg.CORBA.Object raw = orb.resolve_initial_references("RootPOA");
            poa = POAHelper.narrow(raw);
            poa.the_POAManager().activate();
            ensure_ns();
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(CorbaUtils.class);
            logger.warn("Unable to resolve RootPOA: " + ex.getMessage());
        }
    }

    public static void main(String[] args, ResourceStatusServiceImpl impl) {
        String iorfile = null;
        String logging_props_file = null;
        String config = null;
        boolean gui = false;

        int i = 0;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.equals("-ior")) {
                iorfile = args[i++];
            } else if (arg.equals("-logging.props")) {
                logging_props_file = args[i++];
            } else if (arg.equals("-gui")) {
                gui = true;
            } else if (arg.equals("-rss.config")) {
                config = args[i++];
            } else {
                System.out.println("Do not understand switch " + arg);
            }
        }

        if (logging_props_file != null) {
            PropertyConfigurator.configure(logging_props_file);
        }

        Logger logger = Logging.getLogger(CorbaUtils.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Arg Switches " + " -logging.props=" + logging_props_file + " -gui=" + gui
                    + " -rss.config=" + config + " -ior=" + iorfile);
        }

        // make RSS, using conf
        Properties props = new Properties();
        if (gui && impl != null) {
            String gui_title = impl.guiTitle() + " on " + hostname();
            props.setProperty(RSS.GUI_PROPERTY, gui_title);
        }

        if (config != null) {
            InputStream is = null;
            try {
                URL url = new URL(config);
                is = url.openStream();
            } catch (Exception ex) {
                // try it as a filename
                try {
                    is = new FileInputStream(config);
                } catch (Exception e) {
                    logger.error("Error opening " + config + ": " + ex);
                }
            }

            if (is != null) {
                try {
                    props.load(is);
                    is.close();
                } catch (java.io.IOException ex) {
                    logger.error("Error loading RSS properties from " + config + ": " + ex);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Properties=" + props);
        }

        RSS.makeInstance(props);

        registerContexts();

        if (impl != null) {
            makeORB(args);
            Servant servant = impl.getServant();
            org.omg.CORBA.Object reference = null;
            try {
                poa.activate_object(servant);
                reference = poa.servant_to_reference(servant);
            } catch (Exception ex) {
                logger.error(ex);
            }

            if (iorfile != null) {
                writeIOR(iorfile, reference);
            }

            impl.postInit();

            System.out.println("RSS ready");
            if (iorfile != null) {
                System.out.println("IOR in " + iorfile);
            }
            orb.run();
        }
    }

}