/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;

import org.cougaar.core.mts.MT;

interface CougaarWrapper
{
    public void connect(MT server, MTInstrumented delegate) 
	throws java.rmi.RemoteException;
}

