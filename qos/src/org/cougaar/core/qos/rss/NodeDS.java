/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.wp.AddressEntry;
import org.cougaar.core.service.wp.Application;
import org.cougaar.core.service.wp.WhitePagesService;


import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.RSS;

import java.util.Iterator;
import java.util.Set;

public class NodeDS extends DataScope
{
    static final String NODENAME = "nodename".intern();
    static final Application TOPOLOGY = Application.getApplication("topology");
    static final String SCHEME = "node";

    public NodeDS(Object[] parameters, DataScope parent) 
	throws DataScope.ParameterError
    {
	super(parameters, parent);
    }


    protected boolean useParentPath() {
	return false;
    }

    // Node DataScopes can be the first element in a path.  They must
    // find or make the corresponding HostDS and return that as the
    // preferred parent.
    protected DataScope preferredParent(RSS root) {
	ServiceBroker sb = (ServiceBroker) root.getProperty("ServiceBroker");
	WhitePagesService svc = (WhitePagesService)
	    sb.getService(this, WhitePagesService.class, null);
	String nodename = (String) getSymbolValue(NODENAME);
	String host = null;
	try {
	    AddressEntry entry = svc.get(nodename, TOPOLOGY, SCHEME);
	    if (entry == null) {
		System.err.println("# Can't find host for node " +nodename);
		host = "10.0.0.0"; // nice
	    } else {
		host = entry.getAddress().getHost();
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    host = "10.0.0.0"; // nice
	}

	Object[] params = { host };
	DataScopeSpec spec = new DataScopeSpec("Host", params);
	DataScopeSpec[] path = { spec } ;
	DataScope parent = root.getDataScope(path);
	setParent(parent);
	return parent;
    }


    protected void verifyParameters(Object[] parameters) 
	throws DataScope.ParameterError
    {
	if (parameters == null || parameters.length != 1) {
	    throw new DataScope.ParameterError("NodeDS: wrong number of parameters");
	}
	if (!(parameters[0] instanceof String)) {
	    throw new DataScope.ParameterError("NodeDS: wrong parameter type");
	} else {
	    // could canonicalize here
	    String nodename = (String) parameters[0];
	    bindSymbolValue(NODENAME, nodename);
	}
    }

}

