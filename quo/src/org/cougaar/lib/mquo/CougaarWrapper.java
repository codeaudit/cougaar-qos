/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;

import org.cougaar.lib.quo.*;


import org.cougaar.core.mts.MT;
import org.cougaar.core.service.LoggingService;

interface CougaarWrapper
{
    public void connect(MT server, 
			MTInstrumented delegate,
			LoggingService loggingService) 
	throws java.rmi.RemoteException;
}

