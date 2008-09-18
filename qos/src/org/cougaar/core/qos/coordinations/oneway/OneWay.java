/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.core.qos.coordinations.oneway;

import org.cougaar.core.qos.coordinations.CoordinationEventType;
import org.cougaar.core.qos.coordinations.Face;
import org.cougaar.core.qos.coordinations.FaceEventMatcher;


/**
 * For the OneWay coordination, the Server sends a blackboard object to a Client.
 * The Clients does not send back any kind of reply.
 * Each sent object is guaranteed to get its own MTS message, have its own timeout.
 * The objects can be dropped along the way.
 * Both the Server and the Clients remove the relay from the blackboard.
 * The server can optionally remove the object from its blackboard.
 */
public class OneWay {
    public interface Matcher<R extends Face<EventType>> 
        extends FaceEventMatcher<R, EventType> {
    }

    public enum EventType implements CoordinationEventType {
        SEND,
        RECEIVE;
    }
    
    public static class Server implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.SEND,
            };
        }

        public EventType[] consumes() {
            return new EventType[] {
            };
        }
    }
    
    public static class Client implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.RECEIVE,
            };
        }
    }


}