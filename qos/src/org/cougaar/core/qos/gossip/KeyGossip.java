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

class KeyGossip  extends Gossip
{
    private static class Data 
	implements java.io.Serializable, GossipPropagation
    {
	int propagation_distance;

	Data(int propagation_distance) {
	    this.propagation_distance = propagation_distance;
	}

	public int getDistance() {
	    return propagation_distance;
	}

	public boolean equals(Object candidate) {
	    if (candidate == null || !(candidate instanceof Data)) return false;
	    return ((Data) candidate).propagation_distance ==
		propagation_distance;
	}

    }

    // clone all
    KeyGossip cloneGossip()
    {
	return cloneGossip(Integer.MAX_VALUE);
    }

    synchronized KeyGossip cloneGossip(int max)
    {
	KeyGossip result = new KeyGossip();
	Iterator itr = iterator();
	Map.Entry entry;
	String key;
	Data value;
	int count = 0;
	while (itr.hasNext()) {
	    if (max == count++) return result;
	    entry = (Map.Entry) itr.next();
	    key = (String) entry.getKey();
	    value = (Data) entry.getValue();
	    result.addEntry(key, value); // Data is immutable [?]
	}
	return result;
    }


    synchronized boolean add(String key, int propagationDistance) {
	boolean result = true;
	Data old = (Data) lookupValue(key);
	if (old == null) {
	    addEntry(key, new Data(propagationDistance));
	} else if (old.propagation_distance < propagationDistance) {
	    addEntry(key, new Data(propagationDistance));
	} else {
	    // no changes
	    result = false;
	}
	return result;
    }

    // union?
    synchronized void add(KeyGossip gossip) { 
	Iterator itr = gossip.iterator();
	while (itr.hasNext()) {
	    Map.Entry entry = (Map.Entry) itr.next();
	    String key = (String) entry.getKey();
	    Data value = (Data) entry.getValue();
	    add(key, value.propagation_distance);
	}
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
	    buf.append(Integer.toString(data.propagation_distance));
	}
	return buf.toString();
    }


    synchronized void commitChanges(KeyGossip sent)
    {
	Iterator itr = sent.iterator();
	Map.Entry entry = null;
	String key = null;
	GossipPropagation propagation = null;
	GossipPropagation old_prop = null;
	while (itr.hasNext()) {
	    entry = (Map.Entry) itr.next();
	    key = (String) entry.getKey();
	    propagation = (GossipPropagation) entry.getValue();
	    old_prop = (GossipPropagation) lookupValue(key);
	    // The reference test is intentional, because
	    // GossipPropagations are immutable.
	    if (old_prop == propagation) removeEntry(key);
	}
    }

}
