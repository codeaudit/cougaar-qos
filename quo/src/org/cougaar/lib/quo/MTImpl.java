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

package org.cougaar.lib.quo;

import org.cougaar.core.mts.MessageDeliverer;
import org.cougaar.core.mts.MisdeliveredMessageException;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;

public class MTImpl extends MTPOA
{
    private MessageAddress address;
    private MessageDeliverer deliverer;


    public MTImpl(MessageAddress addr,  MessageDeliverer deliverer) 
    {
	super();
	address = addr;
	this.deliverer = deliverer;
    }

    public void rerouteMessage(byte[] message_bytes) 
	throws CorbaMisdeliveredMessage
    {
	Message message = (Message) Zippy.fromByteArray(message_bytes);
	try {
	    deliverer.deliverMessage(message, message.getTarget());
	} catch (MisdeliveredMessageException ex) {
	    throw new CorbaMisdeliveredMessage();
	}
    }
  
}
