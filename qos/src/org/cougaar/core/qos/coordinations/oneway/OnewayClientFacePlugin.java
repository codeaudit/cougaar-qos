/* =============================================================================
 *
 *                  COPYRIGHT 2007 BBN Technologies Corp.
 *                  10 Moulton St
 *                  Cambridge MA 02138
 *                  (617) 873-8000
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * =============================================================================
 *
 * Created : Sept 18, 2008
 * Workfile: OnewayServerFacePlugin
 * $Revision: 1.1 $
 * $Date: 2008-09-18 20:10:48 $
 * $Author: jzinky $
 *
 * =============================================================================
 */
package org.cougaar.core.qos.coordinations.oneway;

import org.cougaar.core.mts.AttributeConstants;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.MessageAddressWithAttributes;
import org.cougaar.core.mts.MessageAttributes;
import org.cougaar.core.mts.SimpleMessageAttributes;
import org.cougaar.core.qos.coordinations.Face;
import org.cougaar.core.qos.coordinations.FacePlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.relay.SimpleRelaySource;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
 * The One way Server Face pulls matched objects off the blackboard and sends them to the Client.
 * No reply is expected from the client. 
 * Each object is guaranteed its own message.
 * The objects can be dropped along the way.
 * Optionally, the object can be removed from the black board
 * 
 * Subclasses should implement {@link #match}.
 */
abstract public class OnewayClientFacePlugin extends FacePlugin<OneWay.Server>
    implements OneWay.Matcher<Face<OneWay.EventType>> {
    
    public OnewayClientFacePlugin() {
        super(new OneWay.Server());
    }
    @Cougaar.Arg(name="clientName", required=true)
    public MessageAddress clientAddress;
    
    @Cougaar.Arg(name="defaultTimoutMillis", defaultValue="1000")
    public int defaultTimoutMillis;
    
    @Cougaar.Arg(name="deleteOnSend", defaultValue="true")
    public boolean deleteOnSend;
   
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isSendObject")
    public void executeNewObjectToSend(Object objectToSend) {
        UID uid = uids.nextUID();
        // make the target address with attributes
        MessageAttributes attrs = new SimpleMessageAttributes();
        // TODO add priority and other QoS attributes
        attrs.setAttribute(AttributeConstants.MESSAGE_SEND_TIMEOUT_ATTRIBUTE, defaultTimoutMillis);
        //HACK: Relay Directives are put into different MTS messages, if their target is different
        // We add an unique attribute value to the target address
        // so that the relay is guaranteed to go in its own message
        attrs.setAttribute("UID", uid);
        MessageAddress targetAddress =
                MessageAddressWithAttributes.getMessageAddressWithAttributes(clientAddress, attrs);
       
        // Make relay that will be sent once and 
        SimpleRelay relay = new SimpleRelaySource(uid, agentId, targetAddress, null);
        relay.setQuery(objectToSend);
        blackboard.publishAdd(relay);
        //Cleanup: 
        //delete relay right away
        blackboard.publishRemove(relay);
        // Remove blackboard object itself
        if (deleteOnSend) {
            blackboard.publishRemove(objectToSend);
        }
    }

    public boolean isSendableObject(UniqueObject object) {
        return match(OneWay.EventType.SEND, object);
    }
}
