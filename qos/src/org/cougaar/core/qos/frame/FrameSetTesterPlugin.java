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

import java.util.Properties;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;

/**
 * Simple tester for FrameSets
 */
public class FrameSetTesterPlugin
    extends ParameterizedPlugin
{
    private class MyAlarm implements Alarm {
	long expiresAt;
	boolean expired = false;

	public MyAlarm (long expirationTime) 
	{
	    expiresAt = System.currentTimeMillis()+expirationTime;
	}

	public long getExpirationTime() 
	{
	    return expiresAt; 
	}

	public synchronized void expire() 
	{
	    if (!expired) {
		expired = true;
		{
		    BlackboardService bbs = getBlackboardService();
		    if (bbs != null) bbs.signalClientActivity();
		}
	    }
	}
	public boolean hasExpired()
	{ 
	    return expired; 
	}

	public synchronized boolean cancel() 
	{
	    boolean was = expired;
	    expired = true;
	    return was;
	}
	
    }


    private LoggingService log;
    private FrameSet frameSet;
    private MyAlarm alarm;
    private Frame host1;
    private int delayCycles = 5;

    public void load()
    {
	super.load();

	ServiceBroker sb = getServiceBroker();

	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
    }



    // plugin
    protected void execute()
    {
	if (alarm == null) {
	    newAlarm();
	} else if (alarm.hasExpired()) {
	    if (delayCycles > 0) {
		--delayCycles;
	    } else if (delayCycles == 0) {
		initializeBlackboard();
		--delayCycles;
	    } else if (frameSet != null && host1 != null) {
		Long now = new Long(System.currentTimeMillis());
		if (log.isDebugEnabled())
		    log.debug("Updated host1 \"time\" slot");
		host1.setValue("time", now);
	    }
	    newAlarm();
	}
    }

    private void newAlarm()
    {
	alarm = new MyAlarm(5000);
	alarmService.addRealTimeAlarm(alarm);
    }
    
    private void initializeBlackboard()
    {

	String xml_filename = (String) getParameter("frame-set");
	if (xml_filename != null) {
	    ServiceBroker sb = getServiceBroker();
	    BlackboardService bbs = getBlackboardService();
	    FrameSetService fss = (FrameSetService)
		sb.getService(this, FrameSetService.class, null);
	    frameSet = fss.makeFrameSet(xml_filename, sb, bbs);
	    host1 = frameSet.findFrame("host", "name", "host1");
	    sb.releaseService(this, FrameSetService.class, fss);
	} else {
	    if (log.isWarnEnabled())
		log.warn("No FrameSet XML file was specified");
	}

    }

    protected void setupSubscriptions() 
    {
    }



}
