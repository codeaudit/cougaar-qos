/* 
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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


import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;

/**
 * Implementation of a very simple Relay.
 * <p>
 * Components must compare their local agent's address to the
 * "getSource()" and "getTarget()" addresses to decide whether they
 * are the sender or recipient.
 */
abstract public class SimplestRelayImpl 
    implements Relay, Relay.Source, Relay.Target, Serializable 
{

    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final UID uid;
    private final MessageAddress source;
    private final MessageAddress target;

    private Object content;

    private transient Set _targets;
    private transient Relay.TargetFactory _factory;

    /**
     * Create an instance.
     *
     * @param uid unique object id from the UIDService 
     * @param source the local agent's address 
     * @param target the remote agent's address 
     * @param content optional initial value, which can be null
     */
    public SimplestRelayImpl(
			   UID uid,
			   MessageAddress source,
			   MessageAddress target,
			   Object content) {
	this.uid = uid;
	this.source = source;
	this.target = target;
	this.content = content;
	cacheTargets();
    }

    abstract Relay.TargetFactory makeFactory(MessageAddress target);

    // SimpleRelay:

    public UID getUID() {
	return uid;
    }
    public void setUID(UID uid) {
	throw new UnsupportedOperationException();
    }

    public MessageAddress getSource() {
	return source;
    }

    public MessageAddress getTarget() {
	return target;
    }



    // Relay.Source:

    private void cacheTargets() {
	_targets = Collections.singleton(target);
	_factory = makeFactory(target);
    }
    public Set getTargets() {
	return _targets;
    }
    public Object getContent() {
	return content;
    }
    public Relay.TargetFactory getTargetFactory() {
	return _factory;
    }
    public int updateResponse(
			      MessageAddress target, Object response) {
	throw new RuntimeException("Attempt to update response!");
    }

    // Relay.Target:

    public Object getResponse() {
	return null;
    }
    public int updateContent(Object content, Token token) {
	this.content = content;
	return Relay.CONTENT_CHANGE;
    }

    // Object:

    @Override
   public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (o instanceof SimplestRelayImpl) { 
	    UID u = ((SimplestRelayImpl) o).uid;
	    return uid.equals(u);
	} else {
	    return false;
	}
    }
    @Override
   public int hashCode() {
	return uid.hashCode();
    }
    private void readObject(java.io.ObjectInputStream os) 
	throws ClassNotFoundException, java.io.IOException {
	os.defaultReadObject();
	cacheTargets();
    }
    @Override
   public String toString() {
	return 
	    "(SimpleRelayImpl"+
	    " uid="+uid+
	    " source="+source+
	    " target="+target+
	    " content="+content+
	    ")";
    }


}
