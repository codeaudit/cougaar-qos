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

import java.util.Properties;

import org.cougaar.core.util.UID;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class DataFrame 
    extends Frame
{
    private static transient Logger log = 
	Logging.getLogger(org.cougaar.core.qos.frame.DataFrame.class);

    protected DataFrame(FrameSet frameSet, 
			String kind, 
			UID uid)
    {
	super(frameSet, kind, uid);
    }

    public static DataFrame newFrame(FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties values)
    {
	String pkg = frameSet.getPackageName();
	return newFrame(pkg, frameSet, proto, uid, values);
    }

    private static final Class[] CTYPES = { FrameSet.class, 
					    String.class,
					    UID.class};

    public static DataFrame newFrame(String pkg,
				     FrameSet frameSet,
				     String proto, 
				     UID uid,
				     Properties values)
    {
	// use reflection here!
	DataFrame frame = null;
	try {
	    Object[] args = { frameSet, proto, uid };
	    String fixed_proto = FrameGen.fix_name(proto, true);
	    String classname = pkg +"."+ fixed_proto;
	    Class klass = Class.forName(classname);
	    java.lang.reflect.Constructor cons = klass.getConstructor(CTYPES);
	    frame = (DataFrame) cons.newInstance(args);
	    if (log.isInfoEnabled())
		log.info("Made frame " +frame);
	} catch (Exception ex) {
	    log.error("Error making frame", ex);
	    return null;
	}
	frame.setValues(values);
	return frame;
    }

}
