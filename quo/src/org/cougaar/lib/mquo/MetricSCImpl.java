/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.lib.mquo;

import java.util.Observable;
import java.util.Observer;

import org.cougaar.core.qos.metrics.MetricsService;
import org.cougaar.core.qos.rss.DataWrapper;

import com.bbn.quo.ValueSCImpl;

public class MetricSCImpl
    extends ValueSCImpl
    implements Observer, MetricSCOperations
{

    private String path;
    private MetricsService svc;
    private Object key;

    public void update(Observable obs, Object val) {
	DataWrapper wrapper = (DataWrapper) val;
	if (val != null)  setValueInternal(wrapper.getDataValue());
    }

    public synchronized void init(MetricsService svc) {
	this.svc = svc;
    }
    
    public synchronized void newPath(String path) {
	if (key != null) svc.unsubscribeToValue(key);
	this.path = path;
	this.key = svc.subscribeToValue(path, this);
    }

}
