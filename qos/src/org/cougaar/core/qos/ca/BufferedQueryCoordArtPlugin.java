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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.cougaar.core.service.community.Community;
import org.cougaar.core.service.community.Entity;
import org.cougaar.core.util.UID;

/**
 * This extension of {@link QueryCoordArtPlugin} buffers replies until
 * all active members have responded, finally transforming the
 * collection responses rather than each response as it arrives.  The
 * number of expected responses is derived by counting the number of
 * Community members that match a given role.  The role itself is
 * domain specific and must therefore be provided by instantiable
 * extensions.  Similarly, the transformation is domain specific and
 * must also be provided by instantiable extensions.  The
 * transformResponse method is therefore implemented here and handles
 * the buffering, while domain specific extensions support a new
 * method to transform the entire collection.
 */
abstract public class BufferedQueryCoordArtPlugin
    extends QueryCoordArtPlugin
{
    public BufferedQueryCoordArtPlugin() 
    {
    }

    abstract protected class BufferedQueryFacet extends QueryFacet {

	private ArrayList pendingResponses;
	private UID outstandingQuery;
	private int communitySize;
	private String responderRole;

	protected BufferedQueryFacet(ConnectionSpec spec, RolePlayer player)
	{
	    super(spec, player);	
	    pendingResponses = new ArrayList();
	    Properties role_params = spec.role_parameters;
	    responderRole = 
		role_params.getProperty(RESPONDERS_COMMUNITY_ROLE_ATTRIBUTE);
	}

	/**
	 * Since the domain-specific extensions will now see completed
	 * collections of responses rather than each one as it arrives, a
	 * new relay-&gt;fact method is required.  This is that method.
	 * The argument maps the MessageAddresse for each responder to the
	 * body of its response.
	 */

	public abstract Fact transformBufferedResponse(HashMap replies);

	public Fact transformResponse(ResponseRelay response)
	{
	    if (outstandingQuery == null) return null;

	    UID uid = response.getOrigUID();
	    if (!uid.equals(outstandingQuery)) return null;  // ignore these

	    synchronized (pendingResponses) {
		pendingResponses.add(response);
		if (checkForCompletion()) {
		    HashMap replies = new HashMap();
		    for (int i=0; i<pendingResponses.size(); i++) {
			ResponseRelay r = (ResponseRelay) 
			    pendingResponses.get(i);
			replies.put(r.getSource(), r.getQuery());
		    }
		    pendingResponses.clear();
		    return transformBufferedResponse(replies);
		}
	    }

	    // Not finished yet
	    return null;

	}

	protected void publishQuery(QueryRelay query)
	{
	    outstandingQuery = query.getUID();
	    communitySize = 0;
	    Community community = getCommunity();
	    Collection entities =  community.getEntities();
	    Iterator itr = entities.iterator();
	    while (itr.hasNext()) {
		Entity ent = (Entity) itr.next();
		// The rest of this is domain-specific
		Attributes attrs = ent.getAttributes();
		Attribute role = attrs.get("Role");
		if (role.contains(responderRole)) ++communitySize;
	    }
	    super.publishQuery(query);
	}

	private boolean checkForCompletion()
	{
	    return pendingResponses.size() >= communitySize;
	}
    
    }


}