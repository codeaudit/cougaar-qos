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

final class ReadOnlyFrameSetProxy 
    implements FrameSet
{
    private FrameSet frameSet;
    ReadOnlyFrameSetProxy(FrameSet frameSet)
    {
	this.frameSet = frameSet;
    }

    public String getName()
    {
	return frameSet.getName();
    }

    public Frame findFrame(String kind, String slot, Object value)
    {
	return frameSet.findFrame(kind, slot, value);
    }

    public Frame findFrame(UID uid)
    {
	return frameSet.findFrame(uid);
    }

    public Frame getParent(Frame frame)
    {
	return frameSet.getParent(frame);
    }

    public Frame getPrototype(Frame frame)
    {
	return frameSet.getPrototype(frame);
    }


    // The rest are disallowed
    public void valueUpdated(Frame frame, String attribute, Object value)
    {
	throw new RuntimeException("Write operation on read-only object");
    }

    public Frame makeFrame(String kind, Properties attributes)
    {
	throw new RuntimeException("Write operation on read-only object");
    }

    public Frame makeFrame(String kind, Properties attributes, UID uid)
    {
	throw new RuntimeException("Write operation on read-only object");
    }

    public Frame makePrototype(String kind, Properties properties)
    {
	throw new RuntimeException("Write operation on read-only object");
    }

    public void removeFrame(Frame frame)
    {
	throw new RuntimeException("Write operation on read-only object");
    }

}
