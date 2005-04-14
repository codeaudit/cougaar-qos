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

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.util.UID;

/**
 * This interface is an abstract specification of an organized
 * collection of {@link Frame}s.  The main job of a FrameSet is to
 * handle inter-Frame relationships.
 */

public interface FrameSet
{
    /**
     * Returns the name of the FrameSet.
     */
    public String getName();

    /**
     * Returns the package used by the {@link FrameGen} code generator
     * when it writes Java classes for prototypes.
     */
    public String getPackageName();

    /**
     * Returns true iff the given frame matches the given prototype,
     * directly or indirectly.
     */
    public boolean descendsFrom(Frame frame, String prototype);

    /**
     * Returns the frame that matches the given triple, ie, whose
     * prototype matches the given kind and whose value for the given
     * slot matches the given value.  If more than one frame matches,
     * the first match is returned.
     */
    public Frame findFrame(String kind, String slot, Object value);

    /**
     * Returns the frame with the given UID. There can be at most one.
     */
    public Frame findFrame(UID uid);

    /**
     * Returns the path with the given name.  There can be at most one.
     */
    public Path findPath(String name);

    /**
     * Returns the path with the given UID.  There can be at most one.
     */
    public Path findPath(UID uid);

    /**
     * Returns a collection of all frames that match all the
     * slot/value pairs.  {@link PrototypeFrame}s are ignored if
     * includePrototypes is false.
     */
    public Set findFrames(String kind, 
			  Properties slot_value_pairs,
			  boolean includePrototypes);

    /**
     * Returns a collection of {@link DataFrame}s representing
     * relationships of the given prototype in which the given frame
     * plays the given role ("parent" or "child").
     */
    public Set findRelations(Frame root, String role, String relation);

    /**
     * Adds a previously constucted DataFrame to this FrameSet.  The
     * Frame's current frameSet should be null.  As a result of this
     * operation the Frame will be published to the Blackboard.
     */
    public Frame makeFrame(Frame frame);

    /**
     * Creates a DataFrame of the given kind and with the given intial
     * values, and adds it to this FrameSet.  A UID for the frame will
     * be generated automatically.  As a result of this operation the
     * Frame will be published to the Blackboard.
     */
    public Frame makeFrame(String kind, Properties slots);

    /**
     * Creates a DataFrame of the given kind and with the given intial
     * values, and adds it to this FrameSet.  The UID for the frame as
     * specified.  As a result of this operation the Frame will be
     * published to the Blackboard.
     */
    public Frame makeFrame(String kind, Properties slots, UID uid);

    /**
     * Creates a PrototypeFrame with the given name, parent prototype
     * and intial values, and adds it to this FrameSet.  A UID for the
     * frame will be generated automatically.  As a result of this
     * operation the Frame will be published to the Blackboard.
     */
    public PrototypeFrame makePrototype(String kind, String parent, 
					Properties properties);

    /**
     * Creates a PrototypeFrame with the given name, parent prototype,
     * UID, and intial values, and adds it to this FrameSet.  As a
     * result of this operation the Frame will be published to the
     * Blackboard.
     */
    public PrototypeFrame makePrototype(String kind, String parent, 
					Properties properties, UID uid);

    /**
     * Creates a Path and adds it to the FrameSet As a result of this
     * operation the Path will be published to the Blackboard.
     */
    public Path  makePath(String name, Path.Fork[] forks, String slot);

    /**
     * Removes the given Frame from the FrameSet.  As a result of this
     * operation the Frame will also be removed from the Blackboard.
     */
    public void removeFrame(Frame frame);

    /**
     * Returns the containing Frame of the given Frame, if any.  The
     * exact meaning of 'containment' is specific to the FrameSet
     * instance. 
     */
    public Frame getParent(Frame frame);

    /**
     * Returns the given DataFrame's PrototypeFrame.
     */
    public Frame getPrototype(Frame frame);

    /**
     * Returns the matching 'parent' Frame in the given relationship.
     */
    public Frame getRelationshipParent(Frame relationship);

    /**
     * Returns the matching 'child' Frame in the given relationship.
     */
    public Frame getRelationshipChild(Frame relationship);
    
    /**
     * Inform the FrameSet that the given frame was changed in the
     * given way.  As a result of this operation, a change will be
     * published to the Blackboard.
     */
    public void valueUpdated(Frame frame, String slot, Object value);

    /**
     * Forcs the FrameSet to process any queued Blackboard operations.
     */
    public void processQueue();

    /**
     * Returns a collection of all PrototypeFrames in the FrameSet,
     */
    public Set getPrototypes();

    /**
     * Dump the DataFrames as XML.
     */
    public void dumpDataFrames(File file) throws java.io.IOException;
}
