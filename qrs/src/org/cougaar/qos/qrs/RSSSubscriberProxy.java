/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

 */

package org.cougaar.qos.qrs;

import java.util.Observable;
import java.util.Observer;

import org.cougaar.qos.ResourceStatus.RSSSubscriber;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.util.log.Logger;

class RSSSubscriberProxy implements Observer {
    private BoundDataFormula bdf;
    private RSSSubscriber subscriber;
    private final ResourceStatusServiceImpl service;
    private final int callback_id;
    Logger logger;

    private class Updater implements Runnable {
        private final org.cougaar.qos.ResourceStatus.DataValue corbaDataValue;

        Updater(org.cougaar.qos.ResourceStatus.DataValue value) {
            this.corbaDataValue = value;
        }

        public void run() {
            synchronized (RSSSubscriberProxy.this) {
                if (subscriber != null) {
                    try {
                        subscriber.dataUpdate(callback_id, corbaDataValue);
                    } catch (Exception ex) {
                        // silently assume remote object is dead
                        service.unsubscribe(subscriber, bdf.getDescription());
                    }
                }
            }
        }
    }

    RSSSubscriberProxy(BoundDataFormula bdf,
                       RSSSubscriber subscriber,
                       int callback_id,
                       ResourceStatusServiceImpl service) {
        this.bdf = bdf;
        this.subscriber = subscriber;
        this.callback_id = callback_id;
        this.service = service;
        logger = Logging.getLogger(RSSSubscriberProxy.class);
        bdf.addObserver(this);
    }

    public void update(Observable o, Object value) {
        DataValue v = (DataValue) value;
        if (logger.isDebugEnabled()) {
            logger.debug("Update: " + value);
        }
        org.cougaar.qos.ResourceStatus.DataValue corbaDataValue = v.getCorbaValue();
        // Do the actual CORBA call in a dedicated thread, so as not
        // to tie up the caller's thread.
        RSSUtils.schedule(new Updater(corbaDataValue), 0);
    }

    boolean hasPath(ResourceNode[] path) {
        ResourceNode[] candidate = bdf.getDescription();
        if (candidate.length != path.length) {
            return false;
        }
        for (int i = 0; i < candidate.length; i++) {
            if (!candidate[i].equals(path[i])) {
                return false;
            }
        }
        return true;
    }

    synchronized void unbind() {
        if (bdf != null) {
            bdf.deleteObserver(this);
            bdf.unsubscribe();
            bdf = null;
            subscriber = null;
        }
    }

}
