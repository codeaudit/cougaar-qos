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
import org.cougaar.core.persist.NotPersistable;

/**
 * Implementation of ResponseRelay.
 * <p>
 * Members in a community subscribe to Relays and respond with
 * snapshots of their own node-level matrices with a ResposneRelay. 
 */
public final class ResponseRelayImpl
implements ResponseRelay, Relay.Source, Relay.Target, 
	   Serializable, NotPersistable 
{

    private final UID uid;
    private final MessageAddress source;
    // the target is transient to avoid resend on rehydration
    private transient final MessageAddress target;

    private Object reply;

    private transient Set _targets;
    private transient Relay.TargetFactory _factory;

    private long timestamp;

    /**
     * Create an instance.
     *
     * @param uid unique object id from the UIDService 
     * @param source the local agent's address 
     * @param target the remote agent's address 
     * @param reply optional initial value, which can be null
     */
    public ResponseRelayImpl(UID uid,
			     MessageAddress source,
			     MessageAddress target,
			     Object reply,
			     long timestamp) 
    {
	this.uid = uid;
	this.source = source;
	this.target = target;
	this.reply = reply;
	this.timestamp = timestamp;
	cacheTargets();
    }
    

    public UID getUID() 
    {
	return uid;
    }

    public void setUID(UID uid) 
    {
	throw new UnsupportedOperationException();
    }
    
    public MessageAddress getSource() 
    {
	return source;
    }

    public MessageAddress getTarget() 
    {
	return target;
    }

    public Object getReply() 
    {
	return reply;
    }

    public void setReply(Object reply) 
    {
	this.reply = reply;
    }

    // Relay.Source:

    private void cacheTargets() 
    {
	_targets = Collections.singleton(target);
	_factory = new ResponseRelayImplFactory(target, timestamp);
    }

    public Set getTargets() 
    {
	return _targets;
    }

    public Object getContent() 
    {
	return reply;
    }

    public Relay.TargetFactory getTargetFactory()
    {
	return _factory;
    }
    
    public int updateResponse(MessageAddress target, Object response) 
    {
	if (response == null ? reply == null : response.equals(reply)) {
	    return Relay.NO_CHANGE;
	}
	this.reply = response;
	return Relay.RESPONSE_CHANGE;
    }

    // Relay.Target:

    public Object getResponse() 
    {
	return reply;
    }
    /* public int updateContent(Object content, Token token) {
    // assert content != null
    this.query = content;
    return Relay.CONTENT_CHANGE;
    }
    */
    public int updateContent(Object content, Token token) 
    {
	// assert content != null
	if (content == null ? reply == null : content.equals(reply)) {
	    return Relay.NO_CHANGE;
	}

	this.reply = content;
	return Relay.CONTENT_CHANGE;
    }


    // Object:

    public boolean equals(Object o) 
    {
	if (o == this) {
	    return true;
	} else if (o instanceof ResponseRelayImpl) { 
	    UID u = ((ResponseRelayImpl) o).uid;
	    return uid.equals(u);
	} else {
	    return false;
	}
    }

    public int hashCode() 
    {
	return uid.hashCode();
    }

    private void readObject(java.io.ObjectInputStream os) 
	throws ClassNotFoundException, java.io.IOException 
    {
	os.defaultReadObject();
	cacheTargets();
    }

    public String toString() 
    {
	return 
	    "(ResponseRelayImpl"+
	    " uid="+uid+
	    " source="+source+
	    " target="+target+
	    " reply="+reply+
	    " timestamp="+timestamp+
	    ")";
    }

    public long getTimestamp() 
    {
	return timestamp;
    }

    // factory method:

    private static class ResponseRelayImplFactory 
	implements Relay.TargetFactory, Serializable {
	private final MessageAddress target;
	private final long timestamp;
	public ResponseRelayImplFactory(MessageAddress target,
					long timestamp) 
	{
	    this.target = target;
	    this.timestamp = timestamp;
	}
	public Relay.Target create(
				   UID uid, MessageAddress source, Object content,
				   Relay.Token token) {
	    // 	long timestamp = System.currentTimeMillis();
	    // 	UID origUID = null;
	    // bug 3824, pass null aba-target to avoid n^2 peer copies
	    return new ResponseRelayImpl(uid, source, null, content, timestamp);
	}
    }
}
