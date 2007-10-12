/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

 */

package org.cougaar.qos.qrs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.cougaar.qos.qrs.sysstat.DirectSysStatSupplier;
import org.cougaar.util.log.Logger;

/** The root of the ResourceContext tree */
public final class RSS extends ResourceContext {
    static final String GUI_PROPERTY = "org.cougaar.qos.qrs.gui";
    private static RSS instance;

    private final Map<String, DataFeed> data_feeds;
    private SitesDB sites;
    private final String sitesurl_string;
    private final Properties props;
    private final Object sites_lock = new Object();
    private final EnumMap<Event,Set<EventSubscriber>> event_subscribers = 
        new EnumMap<Event,Set<EventSubscriber>>(Event.class);
    // This is a universal cache of all ResourceContexts which appear
    // as the root of a path.
    private final Map<String,Map<Object,ResourceContext>> global_cache = 
        new HashMap<String,Map<Object,ResourceContext>>();
    
    
    /**
     * This constructor will try to instantiate the feeds listed in the given
     * Properties collection.
     */
    private RSS(Properties props) throws ParameterError {
        super(new String[0], null);
        Logger static_logger = Logging.getLogger(RSS.class);
        if (static_logger.isDebugEnabled()) {
            static_logger.debug("RSS Property=" + props);
        }
        this.props = props;
        instance = this;
        this.sitesurl_string = props.getProperty("rss.SitesURL");
        data_feeds = new Hashtable<String, DataFeed>();

        String raw_feed_data = props.getProperty("rss.DataFeeds");
        if (raw_feed_data != null) {
            StringTokenizer tokenizer = new StringTokenizer(raw_feed_data, " ");
            while (tokenizer.hasMoreTokens()) {
                String feed_name = tokenizer.nextToken().trim();
                makeFeed(feed_name, props);
            }
        }

        String sysstat_interval_str = props.getProperty("rss.sysstat");
        if (sysstat_interval_str != null) {
            int sysstat_interval = Integer.parseInt(sysstat_interval_str);
            TimerQueueingDataFeed feed = new TimerQueueingDataFeed();
            registerFeed(feed, "DirectEntry");
            DirectSysStatSupplier supplier = new DirectSysStatSupplier(null, feed);
            supplier.schedule(sysstat_interval);
        }

        String gui_id = props.getProperty(GUI_PROPERTY);
        if (gui_id != null) {
            new org.cougaar.qos.qrs.gui.MainWindow(gui_id);
        }

    }
    
    protected DataFormula instantiateFormula(String kind) {
        return null;
    }

    public String toString() {
        return "RSS root";
    }

    void eventNotification(ResourceContext context, Event event_type) {
        Set<EventSubscriber> set;
        synchronized (event_subscribers) {
            set = event_subscribers.get(event_type);
        }
        if (set != null) {
            synchronized (set) {
                for (EventSubscriber sub : set) {
                    sub.rssEvent(context, event_type);
                }
            }
        }
    }

    public boolean subscribeToEvent(EventSubscriber sub, Event event_type) {
        Set<EventSubscriber> set = null;
        synchronized (event_subscribers) {
            set = event_subscribers.get(event_type);
            if (set == null) {
                set = new HashSet<EventSubscriber>();
                event_subscribers.put(event_type, set);
            }
        }
        synchronized (set) {
            set.add(sub);
        }
        return true;
    }

    public void unsubscribeToEvent(EventSubscriber sub, Event event_type) {
        Set<EventSubscriber> set = null;
        synchronized (event_subscribers) {
            set = event_subscribers.get(event_type);
        }
        if (set != null) {
            synchronized (set) {
                set.remove(sub);
            }
        }
    }

    public void setProperty(String tag, Object value) {
        props.put(tag, value);
    }

    public Object getProperty(String tag) {
        return props.get(tag);
    }

    public SitesDB getSitesDB() {
        synchronized (sites_lock) {
            if (sites == null) {
                sites = new SitesDB();
                if (sitesurl_string != null) {
                    java.net.URL sitesURL = null;
                    try {
                        sitesURL = new java.net.URL(sitesurl_string);
                        // TODO: Check for the configfinder protocol in the URL
                    } catch (java.net.MalformedURLException ex) {
                        Logger logger = Logging.getLogger(RSS.class);
                        logger.error(null, ex);
                    }
                    sites.populate(sitesURL);
                } else {
                    // This is not always an error because sometimes
                    // the caller of this method may populate the db
                }
            }
        }
        return sites;
    }

    protected void verifyParameters(String[] parameters) throws ParameterError {
    }

   

    public void deleteContext(ResourceContext context) {
        synchronized (global_cache) {
            Map<Object,ResourceContext> classcache = global_cache.get(context.getContextKind());
            if (classcache != null) {
                classcache.remove(context.getID());
            }
        }
        context.delete();
    }

    // Delete a top-level context
    public void deleteContext(String kind, String[] params) {
        ResourceContext context = null;
        synchronized (global_cache) {
            Map<Object, ResourceContext> classcache = global_cache.get(kind);
            if (classcache != null) {
                String id = (String) getIdentifier(kind, params);
                context = classcache.get(id);
                if (context != null) {
                    classcache.remove(id);
                }
            }
        }
        if (context != null) {
            context.delete();
        }
    }

    protected void addChild(Object id, ResourceContext child) {
        ResourceContext preferredParent = child.preferredParent(this);
        if (preferredParent == null) {
            Logger logger = Logging.getLogger(RSS.class);
            logger.error(child + " cannot be the root of a path");
        } else if (preferredParent == this) {
            super.addChild(id, child);
        } else {
            synchronized (global_cache) {
                Map<Object, ResourceContext> classcache = global_cache.get(child.getContextKind());
                if (classcache == null) {
                    classcache = new HashMap<Object, ResourceContext>();
                }
                classcache.put(id, child);
                global_cache.put(child.getContextKind(), classcache);
            }
            preferredParent.addChild(id, child);
        }
    }

    public ResourceContext resolveSpec(String kind, String[] parameters) {
        Logger logger = Logging.getLogger(RSS.class);
        if (logger.isDebugEnabled()) {
            String paramString = "";
            if (parameters != null && parameters.length > 0) {
                paramString += "[" + parameters[0];
                for (int i = 1; i < parameters.length; i++) {
                    paramString += ", " + parameters[i];
                }
                paramString += "]";
            }
            logger.debug("Resolving " + kind + paramString);
        }
        Object id = getIdentifier(kind, parameters);
        ResourceContext context;
        synchronized (global_cache) {
            Map<Object, ResourceContext> classcache = global_cache.get(kind);
            if (classcache != null) {
                context = classcache.get(id);
                if (context != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found " + context + " in global cache");
                    }
                    return context;
                }
            }
        }
        return super.resolveSpec(id, kind, parameters);
    }

    /**
     * Returns an enumeration of all known feeds.
     */
    public Iterable<DataFeed> feeds() {
        return data_feeds.values();
    }

    public void registerFeed(DataFeed store, String name) {
        store.setName(name);
        data_feeds.put(name, store);
    }

    public void unregisterFeed(String name) {
        data_feeds.remove(name);
    }

    public DataFeed getFeed(String key) {
        return data_feeds.get(key);
    }

    // Reflection!!!

    private void makeFeed(String name, Properties props) {
        Logger logger = Logging.getLogger(RSS.class);

        String classname = props.getProperty(name + ".class");
        if (classname == null) {
            System.err.println("No .class property for feed " + name);
            return;
        }

        String[] args = null;

        String args_string = props.getProperty(name + ".args");
        if (args_string != null) {
            StringTokenizer tk = new StringTokenizer(args_string, " ");
            args = new String[tk.countTokens()];
            int i = 0;
            while (tk.hasMoreTokens()) {
                args[i++] = tk.nextToken();
            }
        } else {
            Logger static_logger = Logging.getLogger(RSS.class);
            if (static_logger.isDebugEnabled()) {
                static_logger.debug("MakeFeed " + name + " has no .arg");
            }
        }

        Object[] constructorArgs = new Object[1];
        constructorArgs[0] = args;

        Class<?>[] constructorParamTypes = {String[].class};

        Object rawfeed = null;
        try {
            Class<?> cl = Class.forName(classname);
            if (cl != null) {
                java.lang.reflect.Constructor<?> cons = cl.getConstructor(constructorParamTypes);
                rawfeed = cons.newInstance(constructorArgs);
            }

            if (!(rawfeed instanceof DataFeed)) {
                logger.error(name + " is not a DataFeed");
            } else {
                DataFeed feed = (DataFeed) rawfeed;
                feed.setName(name);
                registerFeed(feed, name);
                if (logger.isInfoEnabled()) {
                    logger.info("Created DataFeed '" + feed.getName() + "' of "
                            + rawfeed.getClass());
                }
            }
        } catch (Exception ex) {
            logger.error(null, ex);
        }
    }

    public synchronized static RSS instance() {
        return instance;
    }

    public synchronized static RSS makeInstance(Properties properties) {
        Logger static_logger = Logging.getLogger(RSS.class);
        if (instance != null) {
            if (static_logger.isDebugEnabled()) {
                static_logger.debug("#### RSS instance already created!");
            }
            return instance;
        }

        try {
            instance = new RSS(properties);
        } catch (ParameterError cant_happen) {
        }

        return instance;
    }

    public synchronized static RSS makeInstance(String name) {
        Logger static_logger = Logging.getLogger(RSS.class);
        Properties properties = new Properties();
        InputStream is = null;
        try {
            URL url = new URL(name);
            is = url.openStream();
        } catch (Exception ex) {
            // try it as a filename
            try {
                is = new FileInputStream(name);
            } catch (Exception e) {
                static_logger.error("Error opening " + name + ": " + ex);
            }
        }

        if (is != null) {
            try {
                properties.load(is);
                is.close();
            } catch (java.io.IOException ex) {
                static_logger.error("Error loading RSS properties from " + name + ": " + ex);
            }
        }

        return makeInstance(properties);
    }

    public synchronized static RSS makeInstance(String[] args) {
        ResourceStatusServiceImpl impl = null;
        for (String arg : args) {
            if (arg.equals("-rss.corba")) {
                impl = new ResourceStatusServiceImpl();
            }
        }

        CorbaUtils.main(args, impl);
        return instance();
    }
    
    public static String getGUI_PROPERTY() {
        return GUI_PROPERTY;
    }
    
    public enum Event {
        CREATION_EVENT;
    }
}
