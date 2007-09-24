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

package org.cougaar.qos.qrs.sysstat;

import org.cougaar.qos.qrs.TimerQueueingDataFeed;

public class SysStatDataFeed extends TimerQueueingDataFeed {
    private final DirectSysStatSupplier supplier;

    public SysStatDataFeed(String[] args) {
        int i = 0;
        int interval = 0;
        String[] kinds = null;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.equals("-interval")) {
                String interval_string = args[i++];
                interval = Integer.parseInt(interval_string);
            } else if (arg.equals("-kinds")) {
                int remaining = args.length - i;
                kinds = new String[remaining];
                for (int j = 0; j < remaining; j++) {
                    kinds[j] = args[i++];
                }
                break;
            }
        }
        supplier = new DirectSysStatSupplier(kinds, this);
        supplier.schedule(interval);
    }

    public DirectSysStatSupplier getSupplier() {
        return supplier;
    }

}
