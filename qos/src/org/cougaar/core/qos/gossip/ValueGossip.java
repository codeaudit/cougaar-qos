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

package org.cougaar.core.qos.gossip;

import java.util.Iterator;
import java.util.Map;

import org.cougaar.core.qos.metrics.Metric;

/**
 * Gossip object which holds Key/Value Pairs
 */
class ValueGossip 
    extends Gossip
{

    private static class Data implements java.io.Serializable {
	Data(Metric metric) {
	    this.metric = metric;
	}

	private Metric metric;
    }

    synchronized void update(GossipUpdateService updateService) {
	// get the key/metric pairs and pass them through
	Iterator itr = iterator();
	while (itr.hasNext()) {
	    Map.Entry elt = (Map.Entry) itr.next();
	    String key = (String) elt.getKey();
	    Data data = (Data) elt.getValue();
	    updateService.updateValue(key, data.metric);
	}
    }


    synchronized void add(String key, Metric data) {
	addEntry(key, new Data(data));
    }


    synchronized String prettyPrint() {
	StringBuffer buf = new StringBuffer();
	Iterator itr = iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String key = (String) entry.getKey();
	    Data data = (Data) entry.getValue();
	    buf.append("\n\t");
	    buf.append(key);
	    buf.append("->");
	    buf.append(data.metric.toString());
	}
	return buf.toString();
    }

}



