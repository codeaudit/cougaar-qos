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

// TODO the sender has to mark to object so it is ours to unwrap
// maybe use match function
// maybe look for UID in target address
// maybe generic, cleint for all Oneways.
abstract public class OnewayServerFacePlugin extends FacePlugin<OneWay.Server>
    implements OneWay.Matcher<Face<OneWay.EventType>> {
    
    public OnewayServerFacePlugin() {
        super(new OneWay.Server());
    }
   
    @Cougaar.Execute(on=Subscribe.ModType.ADD, when="isMyRelay")
    public void executeNewObjectToSend(SimpleRelay relay) {
       Object object = relay.getQuery();
        blackboard.publishAdd(object);
     }

    public boolean isMyRelay(UniqueObject object) {
        return match(OneWay.EventType.RECEIVE, object);
    }
}
