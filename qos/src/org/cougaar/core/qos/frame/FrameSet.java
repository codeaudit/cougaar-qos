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
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import org.cougaar.core.qos.frame.aggregator.SlotAggregator;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.util.UID;
import org.xml.sax.Attributes;

/**
 * This interface is an abstract specification of an organized
 * collection of {@link Frame}s.  The main job of a FrameSet is to
 * handle inter-Frame relationships.
 */

public interface FrameSet {
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
    public boolean descendsFrom(DataFrame frame, String prototype);

    /**
     * Returns true iff the given frame is an extension the given
     * prototype, directly or indirectly.
     */
    public boolean descendsFrom(PrototypeFrame frame, String prototype);

    /**
     * Returns the DataFrame that matches the given triple, ie, whose
     * prototype matches the given kind and whose value for the given
     * slot matches the given value.  If more than one frame matches,
     * the first match is returned.
     */
    public DataFrame findFrame(String kind, String slot, Object value);
    
    /**
     * 
     * @param name The name of a prototype
     * @return the corresponding PrototypeFrame
     */
    public PrototypeFrame findPrototypeFrame(String name);

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
     * Returns a collection of DataFrames that match all the
     * slot/value pairs.
     */
    public Set<DataFrame> findFrames(String kind, Properties slot_value_pairs);
    
    /**
     * 
     * @return true iff both parent and child are available
     */
    public boolean isResolved(RelationFrame frame);

    /**
     * Returns a collection of {@link DataFrame}s representing frames
     * related to the given one via a relationship matching the given
     * prototype, and in which the given frame plays the given role
     * ("parent" or "child").
     */
    public Set<DataFrame> findRelations(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto);
    /**
     * Returns a {@link DataFrame} that's related to the given one
     * via a relationship matching the given prototype, and in which 
     * the given frame plays the given role ("parent" or "child").
     * 
     * If there's more than one such frame, a random one will be
     * returne.  If there are no such frames, the return value
     * is null.
     */
    public DataFrame findFirstRelation(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto);
    
    /**
     * Returns a count of frames that are related to the given one
     * via a relationship matching the given prototype, and in which 
     * the given frame plays the given role ("parent" or "child").
     * 
     */
    public int countRelations(Frame frame, // should be DataFrame
	    String role,
	    String relation_proto);
    
    

    /**
     * Returns a collection of {@link RelationFrame}s representing
     * relationships of the given prototype in which the given frame
     * plays the given role ("parent" or "child").
     */
    public Map<RelationFrame,DataFrame> findRelationshipFrames(DataFrame frame,
	    String role, 
	    String relation_proto);

    /**
     * Adds a previously constucted DataFrame to this FrameSet.  The
     * Frame's current frameSet should be null.  As a result of this
     * operation the Frame will be published to the Blackboard.
     */
    public DataFrame makeFrame(DataFrame frame);

    /**
     * Creates a DataFrame of the given kind and with the given intial
     * values, and adds it to this FrameSet.  A UID for the frame will
     * be generated automatically.  As a result of this operation the
     * Frame will be published to the Blackboard.
     */
    public DataFrame makeFrame(String kind, Properties slots);

    /**
     * Creates a DataFrame of the given kind and with the given intial
     * values, and adds it to this FrameSet.  The UID for the frame as
     * specified.  As a result of this operation the Frame will be
     * published to the Blackboard.
     */
    public DataFrame makeFrame(String kind, Properties slots, UID uid);
    
    /**
     * Optimized way to create a new relationship that avoids the lookup
     * of the parent and child.
     */
    public RelationFrame makeRelationship(String kind, Properties slots,
	    DataFrame parent, DataFrame child);

    /**
     * Creates a PrototypeFrame with the given name, parent prototype
     * and intial values, and adds it to this FrameSet.  A UID for the
     * frame will be generated automatically.  As a result of this
     * operation the Frame will be published to the Blackboard.
     */
    public PrototypeFrame makePrototype(String kind, String parent,  
	                                Attributes attrs, Map<String,Attributes> slots);

    /**
     * Creates a PrototypeFrame with the given name, parent prototype,
     * UID, and intial values, and adds it to this FrameSet.  As a
     * result of this operation the Frame will be published to the
     * Blackboard.
     */
    public PrototypeFrame makePrototype(String kind, String parent,
	                                Attributes attrs,  Map<String,Attributes> slots, 
	                                UID uid);

    /**
     * Creates a Path and adds it to the FrameSet As a result of this
     * operation the Path will be published to the Blackboard.
     */
    public Path makePath(String name, Path.Fork[] forks, String slot);

    /**
     * Removes the given Frame from the FrameSet.  As a result of this
     * operation the Frame will also be removed from the Blackboard.
     */
    public void removeFrame(DataFrame frame);

    /**
     * Returns the containing Frame of the given Frame, if any.  The
     * exact meaning of 'containment' is specific to the FrameSet
     * instance. 
     */
    public DataFrame getContainer(DataFrame frame);

    /**
     * Returns the given DataFrame's PrototypeFrame.
     */
    public PrototypeFrame getPrototype(Frame frame);

    /**
     * Returns the matching 'parent' Frame in the given relationship.
     */
    public DataFrame getRelationshipParent(RelationFrame relationship);

    /**
     * Returns the matching 'child' Frame in the given relationship.
     */
    public DataFrame getRelationshipChild(RelationFrame relationship);
    
    /**
     * Inform the FrameSet that the given frame was changed in the
     * given way.  As a result of this operation, a change will be
     * published to the Blackboard.
     */
    public void valueUpdated(DataFrame frame, String slot, Object value);

    /**
     * Force the FrameSet to process any queued Blackboard operations.
     * This call will block if a transaction is in progress.
     * 
     * @see #runInTransaction(Runnable)
     */
    public void processQueue();
    
    /**
     * Run a body of code using a lock that prevents a
     * simultaneous invocation of {@link #processQueue}.
     */
    public void runInTransaction(Runnable r);

    /**
     * Returns a collection of all PrototypeFrames in the FrameSet,
     */
    public Collection<PrototypeFrame> getPrototypes();

    /**
     * Return the generated class for the given prototype
     */
    public Class classForPrototype(String prototypeName);
    
    /**
     * Return the generated class for the given prototype
     */
    public Class classForPrototype(PrototypeFrame prototype);

    /**
     * Dump all DataFrames as XML.
     */
    public void exportFrames(File file) throws IOException;
    
    /**
     * Dump all DataFrames of the given prototypes as XML
     */
    public void exportFrames(File file, Set<String> prototypes) throws IOException;

    /**
     * Load DataFrames at the given location
     * 
     * @param location A file or resource path
     */
    public void importFrames(URL location) throws IOException;
    
    /**
     * Subscribes a DataFrame to a value in the MetricsService
     */
    public void subscribeToMetric(DataFrame frame, 
				  Observer observer, 
				  String path);
    /**
     * 
     * Queries the metric service for a value relative to a frame
     */
    public Metric getMetricValue(DataFrame frame, String path);
    
    
    
    // Slot dependencies
    
    /**
     * Create an aggregator to compute and update the given slot.  Ordinarily this happens
     * automatically as part of loading the prototypes and wouldn't need to be called
     * explicitly in user code.
     * 
     * @param slot The slot whose value is computed by a {@link SlotAggregator}.
     * @param relatedSlot  If non-null, and the value of this slot changes in a related entity, 
     * the {@link SlotAggregator} will be reinvoked.
     * @param relation The name of the RelationPrototype that's used to collect the related
     * entites.
     * @param className The name of the {@link SlotAggregator}.  If it's qualified with
     * a package the name is used as is.  If not, the class should be in the frameset
     * package, or in org.cougaar.core.qos.frame.aggregator.
     */
    public void addAggregator(String slot, String relatedSlot, String relation, String role,
	    String className);
    
    /**
     * Set up subscriptions for all aggregators that haven't done so already.
     * Ordinarily this only needs to be called once after the prototypes are
     * loaded.  This is usually handled by {@link FrameSetLoaderPlugin}.
     *
     */
    public void initializeAggregators();

}
