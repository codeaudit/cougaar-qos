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

package org.cougaar.core.qos.frame;

import java.util.Set;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.Service;
import org.cougaar.core.service.BlackboardService;

/**
 * This service finds and makes FrameSets.
 */
public interface FrameSetService extends Service
{
    /**
     * The implementation of findFrameSet uses this as its callbacj
     * interface. 
     */
    public interface Callback {
	public void frameSetAvailable(String name, FrameSet set);
    }

    /**
     * Finds the given FrameSet, returning it immediately if available
     * or invoking the callback when it becomes availale if if isn't
     * at the the time of the call.  The callback can be null if the
     * caller doesn't want a callback.
     */
    public FrameSet findFrameSet(String name, Callback callback);

    /**
     * Creates a FrameSet with the given name, populating it from the
     * given xml file.  The given BlackboardService will be used by
     * the FrameSet to publish Frames and modifications.
     */
    public FrameSet loadFrameSet(String name,
				 String xml_filename, 
				 ServiceBroker sb,
				 BlackboardService bbs);

    /**
     * Creates a FrameSet with the given name, populating it from the
     * given xml files, loaded in order.  The given BlackboardService
     * will be used by the FrameSet to publish Frames and
     * modifications.
     */
    public FrameSet loadFrameSet(String name,
				 String[] xml_filenames, 
				 ServiceBroker sb,
				 BlackboardService bbs);

    /**
     * Returns a collection of the names of the all the FrameSets
     * known to the service.
    */
    public Set getNames();
}

