/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.core.qos.frame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.BlackboardService;

/**
 * This plugin provides the {@link FrameSetService}
 * service, which is implemented by an inner class.
 */
public class FrameSetServicePlugin
    extends ParameterizedPlugin
    implements ServiceProvider
{
    private FrameSetService impl;
    private LoggingService log;
    private HashMap sets;
    private HashMap pending;

    public void load()
    {
	super.load();

	sets = new HashMap();
	pending = new HashMap();

	ServiceBroker sb = getServiceBroker();
	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);

	impl = new Impl();
	sb.addService(FrameSetService.class, this);

    }

    // plugin
    protected void execute()
    {
	handleCallbacks();
    }

    protected void setupSubscriptions() 
    {
    }


     // ServiceProvider Interface
    public Object getService(ServiceBroker sb, 
			     Object requestor, 
			     Class serviceClass) 
    {
	if (serviceClass == FrameSetService.class) {
	    return impl;
	} else {
	    return null;
	}
    }

    public void releaseService(ServiceBroker sb, 
			       Object requestor, 
			       Class serviceClass, 
			       Object service)
    {
    }

	
    private FrameSet makeSet(String xml_filename, 
			     ServiceBroker sb,
			     BlackboardService bbs)
					  
    {
	FrameSet set = null;
	synchronized (sets) {
	    FrameSetParser parser = new FrameSetParser(sb, bbs);
	    set = parser.parseFrameSetFile(xml_filename);
	    sets.put(set.getName(), set);
	}
	BlackboardService my_bbs = getBlackboardService();
	my_bbs.signalClientActivity();

	return set;
    }

    private void doCallback(FrameSetService.Callback cb,
			    String name,
			    FrameSet set)
    {
	cb.frameSetAvailable(name, set);
    }

    private void handleCallbacks()
    {
	synchronized (pending) {
	    Iterator itr = pending.entrySet().iterator();
	    while (itr.hasNext()) {
		Map.Entry entry = (Map.Entry) itr.next();
		String name = (String) entry.getKey();
		HashSet callbacks = (HashSet) entry.getValue();
		FrameSet set = null;
		synchronized (sets) {
		    set = (FrameSet) sets.get(name);
		}
		if (set == null || callbacks == null) continue;

		Iterator sub_itr = callbacks.iterator();
		while (sub_itr.hasNext()) {
		    FrameSetService.Callback cb = (FrameSetService.Callback)
			sub_itr.next();
		    doCallback(cb, name, set);
		}
		itr.remove();
	    }
	}
    }


    private FrameSet doRequest(String name, FrameSetService.Callback cb)
    {
	FrameSet set = null;
	synchronized (sets) {
	    set = (FrameSet) sets.get(name);
	}
	if (set != null) {
	    return set;
	} else if (cb != null) {
	    synchronized (pending) {
		HashSet callbacks = (HashSet) pending.get(name);
		if (callbacks == null) {
		    callbacks = new HashSet();
		    pending.put(name, callbacks);
		}
		callbacks.add(cb);
	    }
	    return null;
 	} else {
	    return null;
	}
    }

    private class Impl implements FrameSetService
    {
	public FrameSet findFrameSet(String name, Callback cb)
	{
	    return doRequest(name, cb);
	}

	public FrameSet loadFrameSet(String xml_filename, 
				     ServiceBroker sb,
				     BlackboardService bbs)
	{
	    return makeSet(xml_filename, sb, bbs);
	}

	public Set getNames()
	{
	    Set result = null;
	    synchronized (sets) {
		result = new HashSet(sets.keySet());
	    }
	    return result;
	}
    }

}

