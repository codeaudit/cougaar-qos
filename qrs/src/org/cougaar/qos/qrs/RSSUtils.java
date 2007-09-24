/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
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

package org.cougaar.qos.qrs;

import org.cougaar.qos.ResourceStatus.ResourceNode;

public class RSSUtils implements Constants {

    private static TaskScheduler scheduler;
    // Pretty-Printers
    public static void nodeToString(StringBuffer buf, ResourceNode node) {
        buf.append("<Resource ");
        buf.append(node.kind);
        buf.append("(");
        for (int i = 0; i < node.parameters.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(node.parameters[i]);
        }
        buf.append(")>");
    }

    public static String nodeToString(ResourceNode node) {
        StringBuffer buf = new StringBuffer();
        nodeToString(buf, node);
        return buf.toString();
    }

    public static void pathToString(StringBuffer buf, ResourceNode[] path) {
        buf.append("[");
        for (int i = 0; i < path.length; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            nodeToString(buf, path[i]);
        }
        buf.append("]");
    }

    public static String pathToString(ResourceNode[] path) {
        StringBuffer buf = new StringBuffer();
        pathToString(buf, path);
        return buf.toString();
    }

    // Semi-abstract scheduling
    public static synchronized void setScheduler(TaskScheduler scheduler) {
        RSSUtils.scheduler = scheduler;
    }

    private static synchronized TaskScheduler getScheduler() {
        if (scheduler == null) {
            scheduler = new TaskSchedulerImpl();
        }
        return scheduler;
    }

    public static Object schedule(Runnable body, long delay, long period) {
        return getScheduler().schedule(body, delay, period);
    }

    public static Object schedule(Runnable body, long delay) {
        return getScheduler().schedule(body, delay);
    }

    public static void unschedule(Object task) {
        getScheduler().unschedule(task);
    }

}
