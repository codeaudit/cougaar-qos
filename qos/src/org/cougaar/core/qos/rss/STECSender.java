/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import org.cougaar.core.qos.metrics.Metric;

import com.bbn.quo.event.status.HeartBeatUtils;
import com.bbn.quo.event.status.StatusSupplierSharedSender;
import com.bbn.quo.event.status.corba.StatusPayloadStruct;
import com.bbn.quo.event.hb.corba.HeartBeatStruct;
import com.bbn.quo.event.status.corba.payload_value_union;

import org.omg.PortableServer.POA;

class STECSender extends StatusSupplierSharedSender 
{
    private int sequenceNum = 0;

    STECSender(String url, String host, POA poa) {
	super(url, null, host);
	try {
	    poa.activate_object(this);
	} catch (Exception e) {
	    System.err.println("POA error: " + e);
	    e.printStackTrace();
	    return;
	}
	makeProxy();
    }

    void send(String key, String type, Metric value) {
	StatusPayloadStruct payload = new StatusPayloadStruct();
	payload.key = key;
	payload.type = type;
	payload.value = new payload_value_union();
	Object raw = value.getRawValue();
	if (raw instanceof Number) {
	    payload.value.d_value(((Number) raw).doubleValue());
	} else if (raw instanceof Boolean) {
	    payload.value.s_value((String) raw);
	} else if (raw instanceof String) {
	    payload.value.b_value(((Boolean) raw).booleanValue());
	} else {
	    System.err.println("Illegal value " + raw);
	    return;
	}
	payload.units = value.getUnits();
	payload.credibility = value.getCredibility();
	payload.signature = new byte[0];
	HeartBeatStruct hb = new HeartBeatStruct();
	HeartBeatUtils.initialize(hb);
	hb.collectorTimestamp = System.currentTimeMillis();
	hb.sequenceNum = sequenceNum++;
	payload.heart_beat = hb;
	sendMessage(payload);
    }

}
