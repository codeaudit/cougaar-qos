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

import java.util.Iterator;
import java.util.Set;

import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;


public class Path
    implements UniqueObject

{
    private transient Logger log = Logging.getLogger(getClass().getName());

    static class Fork implements java.io.Serializable {
	Fork(String role, String relation)
	{
	    this.role = role;
	    this.relation = relation;
	}

	private String role;
	private String relation;
    }

    private final UID uid;
    private String name;
    private Fork[] forks;
    private String slot;

    public Path(UID uid, String name, Fork[] forks, String slot)
    {
	this.name = name;
	this.forks = forks;
	this.slot = slot;
	this.uid = uid;
    }

    String getName()
    {
	return name;
    }

    Object getValue(Frame root)
    {
	return getNextValue(root, 0);
    }

    private Object getNextValue(Frame frame, int index)
    {
	if (log.isDebugEnabled())
	    log.debug("Walking path " +name+
		      " index=" +index+
		      " frame=" +frame+
		      " length=" +forks.length);

	if (index == forks.length) {
	    Object value = frame.getValue(slot);
	    if (log.isDebugEnabled())
		log.debug("End of path at " +frame+
			  " value of " +slot+ " = "
			  +value);
	    return value;
	}

	Fork entry = forks[index];
	Set frames = frame.findRelations(entry.role, entry.relation);
	if (frames == null) {
	    if (log.isDebugEnabled())
		log.debug(frame+ " has no relations of type "
			  +entry.relation+ " role=" +entry.role);
	    return null;
	}

	if (log.isDebugEnabled())
	    log.debug(frames.size() + " matches for relation "
		      +entry.relation+ " role=" +entry.role);

	Iterator itr = frames.iterator();
	while (itr.hasNext()) {
	    Frame next = (Frame) itr.next();
	    Object result = getNextValue(next, ++index);
	    if (result != null) return result;
	}
	return null;
    }

    // UniqueObject
    public UID getUID()
    {
	return uid;
    }

    public void setUID(UID uid)
    {
	if (!uid.equals(this.uid))
	    throw new RuntimeException("UID already set");
    }

}
