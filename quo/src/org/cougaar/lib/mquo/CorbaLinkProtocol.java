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

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import org.cougaar.core.mts.AspectSupport;
import org.cougaar.core.mts.DestinationLink;
import org.cougaar.core.mts.LinkProtocol;
import org.cougaar.core.mts.MessageTransportClient;
import org.cougaar.core.mts.NameLookupException;
import org.cougaar.core.mts.UnregisteredNameException;
import org.cougaar.core.mts.CommFailureException;
import org.cougaar.core.mts.MisdeliveredMessageException;
import org.cougaar.core.mts.AttributedMessage;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.mts.MessageAddress;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.HashMap;

public class CorbaLinkProtocol 
    extends LinkProtocol
{
    public static final String PROTOCOL_TYPE = "-CORBA";

    // private MessageAddress myAddress;
    private MT myProxy;
    private HashMap links;
    private HashMap remoteRefs;
    private ORB orb;
    private POA poa;


    public CorbaLinkProtocol() {
	super(); 
	links = new HashMap();
	remoteRefs = new HashMap();
	String[] args = null;
	orb = ORB.init(args, null);
	try {
	    org.omg.CORBA.Object raw = 
		orb.resolve_initial_references("RootPOA");
	    poa = POAHelper.narrow(raw);
	    poa.the_POAManager().activate();
	} catch (Exception error) {
	    loggingService.error(null, error);
	}
	
    }



    private MT lookupObject(MessageAddress address) throws Exception {
	Object object = 
	    getNameSupport().lookupAddressInNameServer(address, PROTOCOL_TYPE);

	if (object == null) {
	    return null;
	}  else {
	    String ior = (String) object;
	    org.omg.CORBA.Object raw = orb.string_to_object(ior);
	    object = MTHelper.narrow(raw);
	    remoteRefs.put(address, object);
	    return getClientSideProxy(object);
	}

    }


    private synchronized void makeMT() {
	if (myProxy != null) return;
	MessageAddress myAddress = getNameSupport().getNodeMessageAddress();
	MTImpl impl = new MTImpl(myAddress, getDeliverer());
	try {
	    poa.activate_object(impl);
	} catch (Exception ex) {
	    loggingService.error(null, ex);
	}
	myProxy = getServerSideProxy(impl._this());
    }

    public final void registerMTS(MessageAddress addr) {
	makeMT();
	try {
	    Object proxy = orb.object_to_string(myProxy);
	    getNameSupport().registerAgentInNameServer(proxy,addr,PROTOCOL_TYPE);
	} catch (Exception e) {
	    loggingService.error("Error registering MessageTransport", e);
	}
    }

    public final void registerClient(MessageTransportClient client) {
	makeMT();
	try {
	    // Assume node-redirect
	    Object proxy = orb.object_to_string(myProxy);
	    MessageAddress addr = client.getMessageAddress();
	    getNameSupport().registerAgentInNameServer(proxy,addr,PROTOCOL_TYPE);
	} catch (Exception e) {
	    loggingService.error("Error registering MessageTransport", e);
	}
    }


    public final void unregisterClient(MessageTransportClient client) {
	try {
	    // Assume node-redirect
	    Object proxy = orb.object_to_string(myProxy);
	    MessageAddress addr = client.getMessageAddress();
	    getNameSupport().unregisterAgentInNameServer(proxy,addr,PROTOCOL_TYPE);
	} catch (Exception e) {
	    loggingService.error("Error unregistering MessageTransport", e);
	}
    }



    public boolean addressKnown(MessageAddress address) {
	try {
	    return lookupObject(address) != null;
	} catch (Exception e) {
	    //loggingService.error("Failed in addressKnown", e);
	}
	return false;
    }




    // Factory methods:

    public DestinationLink getDestinationLink(MessageAddress address) {
	DestinationLink link = (DestinationLink) links.get(address);
	if (link == null) {
	    link = new Link(address); // attach aspects
	    link =(DestinationLink) attachAspects(link, DestinationLink.class);
	    links.put(address, link);
	}
	return link;
    }



    private MT getClientSideProxy(Object object) {
	return (MT) attachAspects(object, MT.class);
    }


    // For now this can return an object of any arbitrary type!  The
    // corresponding client proxy code has the responsibility for
    // extracting a usable MT out of the object.
    private MT getServerSideProxy(Object object) 
    {
	return (MT) attachAspects(object, MTImpl.class);
    }


    /**
     * The DestinationLink class for this transport.  Forwarding a
     * message with this link means looking up the MT proxy for a
     * remote MTImpl, and calling rerouteMessage on it.  The cost is
     * currently hardwired at an arbitrary value of 1000. */
    class Link implements DestinationLink {
	
	private MessageAddress target;
	private MT remote;

	Link(MessageAddress destination) {
	    this.target = destination;
	}

	private void cacheRemote() 
	    throws NameLookupException, UnregisteredNameException
	{
	    if (remote == null) {
		try {
		    remote = lookupObject(target);
		}
		catch (Exception lookup_failure) {
		    throw new  NameLookupException(lookup_failure);
		}

		if (remote == null) 
		    throw new UnregisteredNameException(target);

	    }
	}

	public Class getProtocolClass() {
	    return CorbaLinkProtocol.class;
	}

	public boolean retryFailedMessage(AttributedMessage message, 
					  int retryCount) 
	{
	    return true;
	}

	public int cost (AttributedMessage message) {
	    try {
		cacheRemote();
		return 1000;
	    }
	    catch (Exception ex) {
		// not found
		return Integer.MAX_VALUE;
	    }
	}

	public MessageAddress getDestination() {
	    return target;
	}


	public MessageAttributes forwardMessage(AttributedMessage message) 
	    throws NameLookupException, 
		   UnregisteredNameException, 
		   CommFailureException,
		   MisdeliveredMessageException
	{
	    cacheRemote();
	    try {
		byte[] bytes = Zippy.toByteArray(message);
		byte[] res = remote.rerouteMessage(bytes);
		return (MessageAttributes) Zippy.fromByteArray(res);
	    } 
	    catch (CorbaMisdeliveredMessage mis) {
		// force recache of remote
		remote = null;
		throw new MisdeliveredMessageException(message);
	    }
	    catch (Exception ex) {
		// force recache of remote
		remote = null;
		// Assume anything else is a comm failure
		throw new CommFailureException(ex);
	    }
	}


	public Object getRemoteReference() {
	    return remoteRefs.get(target);
	}

	

    }


}
   
