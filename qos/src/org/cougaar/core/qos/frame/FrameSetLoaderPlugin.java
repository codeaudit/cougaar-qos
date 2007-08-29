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

import java.util.StringTokenizer;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.ParameterizedPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;

/**
 * This plugin will load a frameset from xml.  It expects two parameters.
 * The "frame-set" parameter specifies the name to be given to the frameset.
 * The "frame-set-files" parameter should be a comma-separated list of files,
 * where the first file is defines the prototypes and the rest of the files 
 * provide data.
 */
public class FrameSetLoaderPlugin extends ParameterizedPlugin {
    private LoggingService log;
    private FrameSet frameset;

    public void load()    {
	super.load();
	ServiceBroker sb = getServiceBroker();
	log = (LoggingService)
           sb.getService(this, LoggingService.class, null);
    }

    public void start() {
	String files = getParameter("frame-set-files");
	String name = getParameter("frame-set");
	if (files != null) {
	    StringTokenizer tk = new StringTokenizer(files, ",");
	    String[] xml_filenames = new String[tk.countTokens()];
	    int i =0;
	    while (tk.hasMoreTokens()) xml_filenames[i++] = tk.nextToken();

	    ServiceBroker sb = getServiceBroker();
	    BlackboardService bbs = getBlackboardService();
	    FrameSetService fss = (FrameSetService)
		sb.getService(this, FrameSetService.class, null);
	    frameset = fss.loadFrameSet(name, xml_filenames, sb, bbs);
	    sb.releaseService(this, FrameSetService.class, fss);
	} else {
	    if (log.isWarnEnabled())
		log.warn("No FrameSet XML files were specified");
	}
	super.start();
    }

    protected void execute() {
	frameset.processQueue();
    }

    protected void setupSubscriptions()  {
	if (frameset != null) {
	    // Set up subscriptions for handling slot dependencies
	    frameset.initializeAggregators();
	}
    }
}

