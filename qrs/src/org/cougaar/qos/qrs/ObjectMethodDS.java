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

public class ObjectMethodDS extends DeflectingContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new ObjectMethodDS(parameters, parent);
            }

        };
        registerContextInstantiater("ObjectMethod", cinst);
    }

    private static final String METHOD_NAME = "MethodName";
    private MethodDS methodDefinition;

    @Override
   protected DataFormula instantiateFormula(String kind) {
        return null;
    }

    /**
     * The parameters should contain one string, the name of the method
     */
    @Override
   protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("MethodDS: wrong number of parameters");
        } else {
            bindSymbolValue(METHOD_NAME, parameters[0]);

            ResourceContext parent = getParent();
            ResourceContext class_ctxt =
                    (ResourceContext) parent.getSymbolValue(ObjectDS.IMPLEMENTATION_DEF);
            methodDefinition = (MethodDS) class_ctxt.resolveSpec("Method", parameters);
            setDeflector(methodDefinition);
        }
    }

    private ObjectMethodDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

}
