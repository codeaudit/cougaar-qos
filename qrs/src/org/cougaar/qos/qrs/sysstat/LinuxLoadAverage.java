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

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

public class LinuxLoadAverage extends SysStatHandler {
    private FileReader fr;
    private BufferedReader br;
    private String key;

    public void initialize(String host, int pid) {
        key = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR + "loadavg";
    }

    private void close() {
        try {
            br.close();
        } catch (java.io.IOException ioe_ex) {
        }
    }

    public void getData(Map<String, DataValue> map) {
        // Use the OperatingSystemMXBean if possible.
        Double load = getLoadAvgFromOS();
        if (load != null) {
            map.put(key, new DataValue(load, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
            return;
        }
        
        // If we get here, either getSystemLoadAverage is unavailable, or the call failed.
        // Use /proc file system instead
        String line = null;
        fr = null;
        try {
            fr = new FileReader("/proc/loadavg");
        } catch (java.io.FileNotFoundException fnf_ex) {
            return;
        }

        br = new BufferedReader(fr);
        try {
            line = br.readLine();
        } catch (java.io.IOException io_ex) {
            return;
        } finally {
            close();
        }

        int index = line.indexOf(' ');
        String doub = null;
        if (index == -1) {
            doub = line;
        } else {
            doub = line.substring(0, index);
        }
        map.put(key, new DataValue(Double.parseDouble(doub),
                                   SECOND_MEAS_CREDIBILITY,
                                   "",
                                   PROVENANCE));
    }

}
