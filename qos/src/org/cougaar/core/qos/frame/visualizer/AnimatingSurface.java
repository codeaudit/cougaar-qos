/*
 * @(#)AnimatingSurface.java	1.8 04/07/26
 * 
 * Copyright (c) 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * @(#)AnimatingSurface.java	1.8 04/07/26
 */


package org.cougaar.core.qos.frame.visualizer;

import java.awt.Dimension;

import org.cougaar.core.service.ThreadService;
import org.cougaar.core.thread.Schedulable;



public abstract class AnimatingSurface extends Surface implements Runnable {

    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   public Schedulable thread;

    public abstract void step(int w, int h);

    public abstract void reset(int newwidth, int newheight);


    public void start(ThreadService tsvc) {
        if (thread == null && !dontThread) {
            thread = tsvc.getThread(this, this, name);
            thread.schedule(0);
        }
    }


    public synchronized void stop() {
        if (thread != null) thread.cancel();
    }


    public void run() {
        if  (getSize().width == 0) {
            thread.schedule(200);
        } else {
            if (isShowing())
                repaint();
            else {
                Dimension d = getSize();
                step(d.width, d.height);
            }
            thread.schedule(sleepAmount);
        }
    }
}
