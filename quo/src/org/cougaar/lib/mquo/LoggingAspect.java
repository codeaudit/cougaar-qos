/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ULTRALOG (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.lib.mquo;


import org.cougaar.lib.quo.*;

import org.cougaar.core.mts.*;

public class LoggingAspect extends StandardAspect
{
    public Object getDelegate(Object delegatee, Class type)  {
	if (type == DestinationLink.class) {
	    return new DestinationLinkDelegate((DestinationLink) delegatee);
	} else {
	    return null;
	}
    }

    private class DestinationLinkDelegate
	extends DestinationLinkDelegateImplBase 
    {
	private DestinationLinkDelegate (DestinationLink delegatee) {
	    super(delegatee);
	}

	public void forwardMessage(AttributedMessage message) 
	    throws UnregisteredNameException, 
		   NameLookupException, 
		   CommFailureException,
		   MisdeliveredMessageException
	{
	    long startTime = System.currentTimeMillis();
	    super.forwardMessage(message);
	    long endTime = System.currentTimeMillis();
	    Utils.logMessage(startTime, endTime, message);
	}

    }
}
