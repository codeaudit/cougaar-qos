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

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.TopologyReaderService;


import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.RSS;

import java.util.Iterator;
import java.util.Set;

public class NodeDS extends DataScope
{
    private static final String NODENAME = "nodename".intern();


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
	TopologyReaderService svc = (TopologyReaderService)
	    sb.getService(this,TopologyReaderService.class, null);
	String nodename = (String) getSymbolValue(NODENAME);
	String host = null;
	try {
	    host = svc.getParentForChild(TopologyReaderService.HOST, 
					 TopologyReaderService.NODE, 
					 nodename);
	} catch (Exception no_such_node) {
	    // print message?
	}

	// What do we do if the host isn't known?
	if (host == null) {
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

