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

package org.cougaar.core.qos.ca;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
    protected synchronized void execute()
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

	
    private synchronized FrameSet makeSet(String xml_filename, 
					  ServiceBroker sb,
					  BlackboardService bbs)
					  
    {
	SaxParser parser = new SaxParser(sb, bbs);
	FrameSet set = parser.parseFrameSetFile(xml_filename);
	sets.put(xml_filename, set);

	BlackboardService my_bbs = getBlackboardService();
	my_bbs.signalClientActivity();

	return set;
    }

    private void doCallback(FrameSetService.Callback cb,
			    String xml_filename,
			    FrameSet set)
    {
	// Should give the callback a read-only proxy, since writes
	// will invoke operations on the creator's BBS.
	cb.frameSetAvailable(xml_filename, new ReadOnlyFrameSetProxy(set));
    }

    private synchronized void handleCallbacks()
    {
	Iterator itr = pending.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String xml_filename = (String) entry.getKey();
	    FrameSet set = (FrameSet) entry.getValue();
	    HashSet callbacks = (HashSet) pending.get(xml_filename);
	    if (callbacks != null) {
		Iterator sub_itr = callbacks.iterator();
		while (sub_itr.hasNext()) {
		    FrameSetService.Callback cb = (FrameSetService.Callback)
			sub_itr.next();
		    doCallback(cb, xml_filename, set);
		}
		pending.remove(xml_filename);
	    }
	}
    }


    private synchronized void doRequest(String xml_filename, 
					FrameSetService.Callback cb)
    {
	FrameSet set = (FrameSet) sets.get(xml_filename);
	if (set != null) {
	    doCallback(cb, xml_filename, set);
	} else {
	    HashSet callbacks = (HashSet) pending.get(xml_filename);
	    if (callbacks == null) {
		callbacks = new HashSet();
		pending.put(xml_filename, callbacks);
	    }
	    callbacks.add(cb);
 	}
    }

    private class Impl implements FrameSetService
    {
	public void findFrameSet(String xml_filename, Callback cb)
	{
	    doRequest(xml_filename, cb);
	}

	public FrameSet makeFrameSet(String xml_filename, 
				     ServiceBroker sb,
				     BlackboardService bbs)
	{
	    return makeSet(xml_filename, sb, bbs);
	}
    }

}

