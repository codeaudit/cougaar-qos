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

class Path
{
    static class Fork {
	Fork(String role, String relation)
	{
	    this.role = role;
	    this.relation = relation;
	}

	private String role;
	private String relation;
    }

    private String name;
    private Fork[] forks;
    private String slot;

    Path(String name, Fork[] forks, String slot)
    {
	this.name = name;
	this.forks = forks;
	this.slot = slot;
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
	if (index == forks.length) return frame.getValue(slot);

	Fork entry = forks[index];
	Set frames = frame.findRelations(entry.role, entry.relation);
	if (frames == null) return null;
	Iterator itr = frames.iterator();
	while (itr.hasNext()) {
	    Frame next = (Frame) itr.next();
	    Object result = getNextValue(next, ++index);
	    if (result != null) return result;
	}
	return null;
    }
}
