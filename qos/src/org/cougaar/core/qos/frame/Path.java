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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * This class represents a kind of visitor pattern that can be used to
 * find the value of a slot at runtime.  It consists of an ordered
 * series of Forks and a slot.  Each Fork refers to a relation and a
 * role.  This allows any given frame to traverse a Path to a related
 * frame, arbitrarily far away.  When the final frame in the path is
 * reached, the given slot value is retrieved.
 */
public class Path
    implements UniqueObject

{
    private transient Logger log = Logging.getLogger(getClass().getName());

    static class Fork implements java.io.Serializable {
	private String role;
	private String relation;

	Fork(String role, String relation)
	{
	    this.role = role;
	    this.relation = relation;
	}

	void dump(PrintWriter writer, int indentation, int offset)
	{
	    for (int i=0; i<indentation; i++) writer.print(' ');
	    writer.println("<fork relation=\"" +relation+
			   " role=\"" +role+
			   "\"/>");
	}
    }

    private final UID uid;
    private String name;
    private Fork[] forks;
    private String override_slot;

    public Path(UID uid, String name, Fork[] forks, String slot)
    {
	this.name = name;
	this.forks = forks;
	this.override_slot = slot;
	this.uid = uid;
    }

    String getName()
    {
	return name;
    }

    Object getValue(DataFrame root, String requestor_slot)
    {
	synchronized (root.get_rlock()) {
	    root.clearRelationDependencies(requestor_slot);
	    return
		getNextValue(root, root, 0, requestor_slot);
	}
    }

    private Object getNextValue(DataFrame root, 
				DataFrame frame, 
				int index, 
				String requestor_slot)
    {
	String slot = override_slot != null ? override_slot : requestor_slot;
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
	Map frames = frame.findRelationshipFrames(entry.role, entry.relation);
	if (frames == null) {
	    if (log.isDebugEnabled())
		log.debug(frame+ " has no relations of type "
			  +entry.relation+ " role=" +entry.role);
	    return null;
	}

	if (log.isDebugEnabled())
	    log.debug(frames.size() + " matches for relation "
		      +entry.relation+ " role=" +entry.role);

	Iterator itr = frames.entrySet().iterator();
	while (itr.hasNext()) {
	    Map.Entry e = (Map.Entry) itr.next();
	    RelationFrame rframe = (RelationFrame) e.getKey();
	    synchronized (rframe) {
		// Don't allow relationship changes during the lookup.
		root.addRelationDependency(rframe, requestor_slot);
		DataFrame next = (DataFrame) e.getValue();
		Object result = getNextValue(root, next, ++index, slot);
		if (result != null) {
		    root.addRelationSlotDependency(next, requestor_slot, slot);
		    return result;
		}

		// This tree failed
		root.removeRelationDependency(rframe, requestor_slot);
	    }
	    if (index == forks.length) return null;
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

    void dump(PrintWriter writer, int indentation, int offset)
    {
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("<path name=\"" +name+ "\">");
	indentation += offset;
	for (int i=0; i<forks.length; i++) {
	    Fork fork = forks[i];
	    fork.dump(writer, indentation, offset);
	}
	if (override_slot != null)
	    writer.println("<slot-reference name=\"" +override_slot+ "\"/>");
	indentation -= offset;
	for (int i=0; i<indentation; i++) writer.print(' ');
	writer.println("</path>");
    }
}
