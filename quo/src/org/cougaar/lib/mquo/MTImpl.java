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

import org.cougaar.core.mts.AttributedMessage;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.mts.MessageDeliverer;
import org.cougaar.core.mts.MessageSecurityException;
import org.cougaar.core.mts.MisdeliveredMessageException;
import org.cougaar.core.mts.SerializationUtils;

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


    private void securityException(MessageSecurityException mex)
	throws CorbaMessageSecurityException
    {
	try {
	    byte[] exception = SerializationUtils.toByteArray(mex);
	    throw new CorbaMessageSecurityException(exception);
	} catch  (java.io.IOException iox) {
	}
	
	throw new CorbaMessageSecurityException();
    }

    public byte[] rerouteMessage(byte[] message_bytes) 
	throws CorbaMisdeliveredMessage, CorbaMessageSecurityException
    {
	AttributedMessage message = null;
	try {
	    message = (AttributedMessage) 
		SerializationUtils.fromByteArray(message_bytes);
	} catch (MessageSecurityException mex) {
	    securityException(mex);
	} catch (java.io.IOException iox) {
	} catch (ClassNotFoundException cnf) {
	}


	MessageAttributes metadata = null;
	try {
	    metadata = deliverer.deliverMessage(message, message.getTarget());
	} catch (MisdeliveredMessageException ex) {
	    throw new CorbaMisdeliveredMessage();
	}

	byte[] reply_bytes = null;
	try {
	    reply_bytes = SerializationUtils.toByteArray(metadata);
	} catch (MessageSecurityException mex) {
	    securityException(mex);
	} catch (java.io.IOException iox) {
	}

	return reply_bytes;

    }
  
}
