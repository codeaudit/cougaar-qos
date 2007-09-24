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
 * A ResourceContext for a Process which is on a Host. For now the Process named
 * by its port, but latter it needs to have a list of ports and a PID. The
 * available formulae ???
 */
public class ProcessDS extends ResourceContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new ProcessDS(parameters, parent);
            }

        };
        registerContextInstantiater("Process", cinst);
    }

    private static final String PID = "pid";
    private static final String PORT = "port";

    protected DataFormula instantiateFormula(String kind) {
        return null;
    }

    /**
     * The parameters should contain one string, the port of the host being
     * monitored.
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("ProcessDS: wrong number of parameters");
        } else {
            String port_string = parameters[0];
            try {
                int port = Integer.parseInt(port_string);
                bindSymbolValue(PORT, port);

                // To be done
                bindSymbolValue(PID, 0);
            } catch (NumberFormatException not_a_num) {
                throw new ParameterError("ProcessDS: port is not an int");
            }
        }

    }

    private ProcessDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    // Some Day Process will have formulas to calculate things like
    // nice value.

}
