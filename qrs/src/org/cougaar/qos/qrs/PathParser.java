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

import java.util.StringTokenizer;

import org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException;
import org.cougaar.qos.ResourceStatus.ResourceNode;

public class PathParser {

    public static ResourceNode parseNode(String spec) {
        String[] args;
        int left_paren = spec.indexOf('(');
        int right_paren = spec.indexOf(')');
        String kind;
        if (left_paren < 0 || right_paren < 0) {
            kind = spec;
            args = new String[0];
        } else {
            kind = spec.substring(0, left_paren);
            if (left_paren + 1 < right_paren) {
                String arg_spec = spec.substring(left_paren + 1, right_paren);
                StringTokenizer tk = new StringTokenizer(arg_spec, ",");
                int count = tk.countTokens();
                args = new String[count];
                for (int i = 0; i < count; i++) {
                    args[i] = tk.nextToken();
                }
            } else {
                args = new String[0];
            }
        }
        ResourceNode node = new ResourceNode();
        node.kind = kind;
        node.parameters = args;
        return node;
    }

    public static ResourceNode[] parsePath(String spec) throws ResourceDescriptionParseException {
        // kind(arg, ..., arg):...:kind(arg, ..., arg)
        ResourceNode[] path = null;
        try {
            StringTokenizer tk = new StringTokenizer(spec, ":");
            int count = tk.countTokens();
            path = new ResourceNode[count];
            for (int i = 0; i < count; i++) {
                path[i] = parseNode(tk.nextToken());
            }
        } catch (Exception ex) {
            throw new ResourceDescriptionParseException(spec, ex.getMessage());
        }

        return path;
    }

}
