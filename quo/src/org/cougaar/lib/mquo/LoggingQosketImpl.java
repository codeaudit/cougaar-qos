/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.mquo;


import org.cougaar.lib.quo.*;

import org.cougaar.core.service.LoggingService;

public class LoggingQosketImpl 
    extends LoggingQosketSkel
{
    private LoggingService loggingService;
    
    public void setLoggingService(LoggingService loggingService) {
	this.loggingService = loggingService;
    }


}


