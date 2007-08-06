/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;
import org.cougaar.qos.ResourceStatus.RSSSubscriberPOA;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.ResourceStatus.ResourceStatusService;
import org.cougaar.qos.ResourceStatus.ResourceStatusServiceHelper;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;


/**
 * RelayDataFeeds are designed to move data from one RSS to
 * another.  This first one is simple but inefficient (the same
 * formulas will be calculated at multiple levels) and not all
 * particular about what it  passes along (everything).
 */

public class PromiscuousRelayDataFeed 
    extends AbstractDataFeed
    implements Constants
{
    private class Subscriber extends RSSSubscriberPOA {
	String key;
	ResourceStatusService rss;

	Subscriber(String key, ResourceStatusService rss) {
	    this.key = key;
	    this.rss = rss;
	}

	void connect() {
	    ResourceNode scope_ref = new ResourceNode();
	    String[] parameters = { key };
	    scope_ref.kind = "Integrater";
	    scope_ref.parameters = parameters;
	    ResourceNode formula_ref = new ResourceNode();
	    formula_ref.kind = "Formula";
	    formula_ref.parameters = new String[0];
	    ResourceNode[] description = { scope_ref, formula_ref };
	    rss.unqualifiedSubscribe(_this(), description, 0);
	}

	public void dataUpdate(int callback_id,
			       final org.cougaar.qos.ResourceStatus.DataValue value) 
	{
	    // run this in a different thread?
	    Runnable task = new Runnable() {
		    public void run() {
			newData(key, value);
		    }
		};
	    RSSUtils.schedule(task, 0);
	}

	
    }

    private HashMap listeners;
    private HashMap data;
    private HashSet keys;
    private ArrayList services;

    public PromiscuousRelayDataFeed()
    {
	super();
	listeners = new HashMap();
	data = new HashMap();
	keys = new HashSet();
	services = new ArrayList();
    }

    public void addService(ResourceStatusService rss)
    {
	synchronized (services) {
	    services.add(rss);
	}
	synchronized (keys) {
	    Iterator itr = keys.iterator();
	    while (itr.hasNext()) {
		String key = (String) itr.next();
		Subscriber subscriber = new Subscriber(key, rss);
		try {
		    CorbaUtils.poa.activate_object(subscriber);
		    subscriber.connect();
		} catch (Exception ex) {
		    Logger logger = Logging.getLogger(PromiscuousRelayDataFeed.class);
		    logger.error(null, ex);
		}
	    }
	}
    }

    public void removeService(ResourceStatusService rss)
    {
	synchronized (services) {
	    services.remove(rss);
	    // much more to do here.
	}
    }

    private BufferedReader open(String location) 
    {
	String ior = null;
	InputStreamReader rdr = null;
	try {
	    try {
		URL url = new URL(location);
		rdr = new InputStreamReader(url.openStream());
	    } catch (java.net.MalformedURLException mal) {
		// try it as a filename
		rdr = new FileReader(location);
	    }
		
	    return  new BufferedReader(rdr);
	} catch (java.io.IOException e) {
	    Logger logger = Logging.getLogger(PromiscuousRelayDataFeed.class);
	    logger.error(null, e);
	}
	
	return null;
    }

    // Caller should synchronize on keys
    private void makeNewSubscribersForKey(String key) {
	Subscriber subscriber;
	synchronized (services) {
	    Iterator itr = services.iterator();
	    while (itr.hasNext()) {
		ResourceStatusService rss = (ResourceStatusService) itr.next();
		subscriber = new Subscriber(key, rss);
		try {
		    CorbaUtils.poa.activate_object(subscriber);
		    subscriber.connect();
		} catch (Exception ex) {
		    Logger logger = Logging.getLogger(PromiscuousRelayDataFeed.class);
		    logger.error(null, ex);
		}
	    }
	}
    }

    public void removeListenerForKey(DataFeedListener listener, 
				     String key) 
    {
	HashSet key_listeners = (HashSet) listeners.get(key);
	if (key_listeners != null) {
	    synchronized (key_listeners) {
		key_listeners.remove(listener);
	    }
	}
    }

    public void addListenerForKey(DataFeedListener listener,
				  String key) 
    {
	HashSet key_listeners = null;
	synchronized (listeners) {
	    key_listeners = (HashSet) listeners.get(key);
	    if (key_listeners == null) {
		key_listeners = new HashSet();
		listeners.put(key, key_listeners);
	    }
	}
	synchronized (key_listeners) {
	    key_listeners.add(listener);
	}
    }



    

    private void notifyListeners(String key, DataValue value)
    {
	HashSet key_listeners = (HashSet) listeners.get(key);
	if (key_listeners != null) {
	    synchronized (key_listeners) {
		Iterator i = key_listeners.iterator();
		while (i.hasNext()) {
		    DataFeedListener listener = (DataFeedListener) i.next();
		    listener.newData(this, key, value);
		}
	    }
	}
    }


    public DataValue lookup(String key) {
	synchronized (keys) {
	    if (!keys.contains(key)) {
		makeNewSubscribersForKey(key);
		keys.add(key);
	    }
	}
	return  (DataValue) data.get(key);
    }



    private void newData(final String key, 
			 org.cougaar.qos.ResourceStatus.DataValue corba_value) 
    {
	synchronized (data) {
	    DataValue old_value = lookup(key);
	    double credibility = corba_value.credibility;
	    if (old_value == null || old_value.getCredibility() <= credibility) {
		final DataValue new_value = new DataValue(corba_value);
		data.put(key, new_value);
		notifyListeners(key, new_value);
	    }
	}

    }

}
