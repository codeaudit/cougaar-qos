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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cougaar.core.util.UID;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This extension to {@link Frame} is the runtime representation of a prototype.
 * It's main job at runtime is to hold non-static default values (static
 * defaults are code-generated into constants) and to process {@link Path} slot
 * values.
 */
public class PrototypeFrame extends Frame {
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final String prototype_name;
    private final String parent_prototype;
    private final transient List<String> inheritableSlots;
    private transient final Logger log = Logging.getLogger(getClass().getName());
    private final transient Map<String, Object> dynamic_values;
    private final Attributes attrs;
    private final Map<String, Path> path_cache;
    private final Map<String, Attributes> slots;

    PrototypeFrame(FrameSet frameSet,
                   String prototype_name,
                   String parent,
                   UID uid,
                   Attributes attrs,
                   Map<String, Attributes> slots) {
        super(frameSet, uid);
        this.inheritableSlots = new ArrayList<String>();
        for (Map.Entry<String, Attributes> entry : slots.entrySet()) {
            String inheritance = entry.getValue().getValue("inheritable-through");
            if (inheritance == null || inheritance.equals("all")) {
                inheritableSlots.add(entry.getKey());
            }
        }
        this.parent_prototype = parent;
        this.prototype_name = prototype_name;
        this.slots = slots;
        this.dynamic_values = new HashMap<String, Object>();
        this.attrs = attrs;
        this.path_cache = new HashMap<String, Path>();
    }

    @Override
   public String getKind() {
        return parent_prototype;
    }

    public Properties getSlotDefinitions() {
        // return a copy
        Properties defs = new Properties();
        for (Map.Entry<String, Attributes> entry : slots.entrySet()) {
            Object key = entry.getKey();
            Attributes attrs = entry.getValue();
            defs.put(key, new AttributesImpl(attrs));
        }
        return defs;
    }

    public List<String> getInheritedSlots() {
        return inheritableSlots;
    }

    @Override
   public Properties getLocalSlots() {
        Properties result = new VisibleProperties();
        for (Map.Entry<String, Attributes> entry : slots.entrySet()) {
            String slot_name = entry.getKey();
            Attributes attrs = entry.getValue();
            Object slot_value = dynamic_values.get(slot_name);
            if (slot_value != null) {
                result.put(slot_name, slot_value);
            } else {
                slot_value = attrs.getValue("default-value");
                if (slot_value != null) {
                    result.put(slot_name, slot_value);
                }
            }
        }
        return result;
    }

    @Override
   public void setValue(String slot, Object value) {
        dynamic_values.put(slot, value);
    }

    @Override
   Object getValue(Frame origin, String slot_name) {
        // Look for a dynamically set default first.
        Object slot_value = dynamic_values.get(slot_name);
        if (slot_value != null) {
            return slot_value;
        }

        Attributes attrs = slots.get(slot_name);
        if (attrs != null) {
            // Declared at this level -- either get a real value or
            // complain and return null.
            Object result = null;
            String value = attrs.getValue("default-value");
            String warnp = attrs.getValue("warn");
            if (warnp == null && attrs.getValue("metric-path") != null) {
                // if no warn attribute is specified and metric_path is specified, don't warn
                warnp = "false";
            }
            String path_name = attrs.getValue("path");
            if (value != null) {
                result = value;
            } else if (path_name != null) {
                if (origin instanceof PrototypeFrame) {
                    // Makes no sense to follow a path in this case.
                    return null;
                }
                Path path;
                synchronized (path_cache) {
                    path = path_cache.get(path_name);
                    if (path == null) {
                        path = getFrameSet().findPath(path_name);
                        path_cache.put(path_name, path);
                    }
                }
                result = path.getValue((DataFrame) origin, slot_name);
            } else if (warnp == null || warnp.equalsIgnoreCase("true")) {
                if (log.isWarnEnabled()) {
                    log.warn("Slot " + slot_name + " is required by prototype " + prototype_name
                            + " but was never provided in frame " + origin);
                }
            }
            return result;
        } else {
            // Not owned by us, check super
            return getInheritedValue(origin, slot_name);
        }
    }

    public String getName() {
        return prototype_name;
    }

    public String getPrototypeName() {
        return prototype_name;
    }

    public String getAttribute(String key) {
        return attrs.getValue(key);
    }

    @Override
   public String toString() {
        return "<Prototype " + prototype_name + " " + getUID() + ">";
    }

    @Override
   public boolean isa(String kind) {
        if (frameSet == null) {
            return kind.equals(prototype_name);
        }
        return frameSet.descendsFrom(this, kind);
    }

}
