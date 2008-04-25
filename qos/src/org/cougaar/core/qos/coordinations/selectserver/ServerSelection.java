/*
 *
 * Copyright 2007 by BBN Technologies Corporation
 *
 */

package org.cougaar.core.qos.coordinations.selectserver;

import org.cougaar.core.qos.coordinations.CoordinationEventType;
import org.cougaar.core.qos.coordinations.Face;
import org.cougaar.core.qos.coordinations.FaceEventMatcher;


/**
 *
 */
public class ServerSelection {
    public interface Matcher<R extends Face<EventType>> 
        extends FaceEventMatcher<R, EventType> {
    }

    public enum EventType implements CoordinationEventType {
        REQUEST,
        RESPONSE;
    }
    
    public static class Client implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.RESPONSE,
            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.REQUEST,
            };
        }
    }
    
    public static class Server implements Face<EventType> {
        public EventType[] produces() {
            return new EventType[] {
                    EventType.REQUEST,

            };
        }

        public EventType[] consumes() {
            return new EventType[] {
                    EventType.RESPONSE,
            };
        }
    }


}