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

public class GossipTrafficRecord
{
    int requests_sent;
    int requests_rcvd;
    int values_sent;
    int values_rcvd;
    int msg_with_gossip_sent;
    int msg_with_gossip_rcvd;
    int msg_sent;
    int msg_rcvd;

    GossipTrafficRecord() {
    }

    GossipTrafficRecord(GossipTrafficRecord other) {
	this.requests_sent = other.requests_sent;
	this.requests_rcvd =  other.requests_rcvd;
	this.values_sent =  other.values_sent;
	this.values_rcvd =  other.values_rcvd;
	this.msg_with_gossip_sent = other.msg_with_gossip_sent;
	this.msg_with_gossip_rcvd = other.msg_with_gossip_rcvd;
	this.msg_sent = other.msg_sent;
	this.msg_rcvd = other.msg_rcvd;

    }
    

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("<requests_sent=");
	buf.append(requests_sent);
	buf.append(" requests_rcvd=");
	buf.append(requests_rcvd);
	buf.append(" values_sent=");
	buf.append(values_sent);
	buf.append(" values_rcvd=");
	buf.append(values_rcvd);
	buf.append(" msg_with_gossip_sent=");
	buf.append(msg_with_gossip_sent);
	buf.append(" msg_with_gossip_rcvd=");
	buf.append(msg_with_gossip_rcvd);
	buf.append(" msg_with_gossip_sent=");
	buf.append(msg_sent);
	buf.append(" msg_rcvd=");
	buf.append(msg_rcvd);
	buf.append('>');
	return buf.toString();
    }

    public int getRequestsSent() {
	return requests_sent;
    }

    public int getRequestsReceived() {
	return requests_rcvd;
    }

    public int getValuesSent() {
	return values_sent;
    }

    public int getValuesReceived() {
	return values_rcvd;
    }

    public int getMessagesWithGossipSent() {
	return msg_with_gossip_sent;
    }

    public int getMessagesWithGossipReceived() {
	return msg_with_gossip_rcvd;
    }

    public int getMessagesSent() {
	return msg_sent;
    }

    public int getMessagesReceived() {
	return msg_rcvd;
    }

}
