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

/**
 * A ResourceContext for a physical Object which is on a Process which is on a
 * Host. The available formulae ???
 */
public class ObjectDS extends DeflectingContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new ObjectDS(parameters, parent);
            }

        };
        registerContextInstantiater("Object", cinst);
    }

    // pseudo-enum
    private static final String KEY = "key";
    private static final String INTERFACE_NAME = "interfaceName";
    private static final String IMPLEMENTATION_NAME = "implementationName";

    public static final String IMPLEMENTATION_DEF = "implementationDef";

    @Override
   protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("RemoteLoadAverage")) {
            return new RemoteLoadAverage();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain three strings: the object key, interface
     * name and the implementation. The object key is in the Object reference
     * for both CORBA and RMI. The interface is in the CORBA reference and the
     * (stub of the) implemention is in the RMI reference.
     */
    @Override
   protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 3) {
            throw new ParameterError("ObjectDS: wrong number of parameters");
        } else {
            bindSymbolValue(KEY, parameters[0]);
            String iface = parameters[1];
            bindSymbolValue(INTERFACE_NAME, iface);
            String impl = parameters[2];
            bindSymbolValue(IMPLEMENTATION_NAME, impl);

            // find extant class context
            String[] args = {impl, iface};
            ResourceContext class_ctxt = RSS.instance().resolveSpec("Class", args);
            bindSymbolValue(IMPLEMENTATION_DEF, class_ctxt);
            // Use class as prototype for Object
            setDeflector(class_ctxt);
        }
    }

    private ObjectDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    /***************************************************************************
     * Only for testing.
     **************************************************************************/
    public static class RemoteLoadAverage extends DataFormula {

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            Object raw = context.getValue("calls");
            ResourceContext remote = (ResourceContext) raw;
            registerDependency(remote, "LoadAverage");
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            return values.get("LoadAverage");
        }
    }

    // Some Day Object will have formulas to calculate things like
    // class, version?

}
