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


package org.cougaar.core.qos.rss;

import com.bbn.quo.event.status.HeartBeatUtils;
import com.bbn.quo.event.status.StatusSupplierSharedSender;
import com.bbn.quo.event.status.corba.StatusPayloadStruct;
import com.bbn.quo.event.status.corba.payload_value_union;
import com.bbn.quo.event.hb.corba.HeartBeatStruct;

public class Heartbeater 
    implements Runnable
{
    public final static String HEARTBEAT_TYPE = "Heartbeat";
    private HeartBeatStruct hb;
    private StatusPayloadStruct payload;
    private STECSender sender;

    public Heartbeater(STECSender sender) {
	this.sender = sender;
	hb = new HeartBeatStruct();
	HeartBeatUtils.initialize(hb);
	payload = new StatusPayloadStruct();
	payload.value = new payload_value_union();
	payload.units = "";
	payload.signature = new byte[0];
	payload.heart_beat = hb;
	payload.type = HEARTBEAT_TYPE;
	payload.credibility = 1.0;
	payload.key = hb.collectorID;
    }

    public void run() {
	++hb.sequenceNum;
	hb.collectorTimestamp = System.currentTimeMillis();
	payload.value.d_value(hb.collectorTimestamp);
	sender.sendMessage(payload);
    }


}
