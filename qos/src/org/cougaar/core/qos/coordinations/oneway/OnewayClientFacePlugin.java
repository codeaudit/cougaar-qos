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
 * $Revision: 1.3 $
 * $Date: 2008-09-19 16:14:46 $
 * $Author: jzinky $
 *
 * =============================================================================
 */
package org.cougaar.core.qos.coordinations.oneway;

import org.cougaar.core.qos.coordinations.Face;
import org.cougaar.core.qos.coordinations.FacePlugin;
import org.cougaar.core.relay.SimpleRelay;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.Subscribe;

/**
* The One way Client Face waits for relays to appear on its blackboard and pulls the content out.
* No reply is sent back to  Server
* 
* Subclasses should implement {@link #match}.
*/

//TODO the sender has to mark to object so it is ours to unwrap
//maybe use match function
//maybe look for UID in target address
//maybe generic, client for all Oneways.
abstract public class OnewayClientFacePlugin extends FacePlugin<OneWay.Client>
    implements OneWay.Matcher<Face<OneWay.EventType>> {
    
    public OnewayClientFacePlugin() {
        super(new OneWay.Client());
    }
   
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isMyRelay")
    public void executeNewObjectToSend(SimpleRelay relay) {
       Object object = relay.getQuery();
        blackboard.publishAdd(object);
     }

    public boolean isMyRelay(SimpleRelay relay) {
        //unpack content from relay
        Object relayContents = relay.getQuery();
        if (relayContents instanceof UniqueObject) {
            return match(OneWay.EventType.RECEIVE, (UniqueObject) relayContents);
        } else {
            return false;
        }
    }
}
