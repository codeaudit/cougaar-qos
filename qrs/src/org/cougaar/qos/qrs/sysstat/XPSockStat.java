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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;
import org.cougaar.util.log.Logger;

public class XPSockStat extends SysStatHandler {
    private static final String command = 
	"typeperf \"\\TCP\\Connections Established\" -sc 1";

    private String tcp_key;

    @Override
   public void initialize(String host, int pid) {
	tcp_key =
	    "Host" + KEY_SEPR + host + KEY_SEPR + "Network" + KEY_SEPR + "TCP" + KEY_SEPR
	    + "sockets" + KEY_SEPR + "inuse";
    }

    @Override
   public void getData(Map<String, DataValue> map) {
        Logger logger = org.cougaar.qos.qrs.Logging.getLogger(XPLoadAverage.class);
        String line = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream stdOut = process.getInputStream();
            InputStreamReader rdr = new InputStreamReader(stdOut);
            BufferedReader bufferedStdOut = new BufferedReader(rdr);
            line = bufferedStdOut.readLine(); // handy blank line -- ignore
            line = bufferedStdOut.readLine(); //headers --- ignore
            line = bufferedStdOut.readLine();
            bufferedStdOut.close();
            process.destroy();
        } catch (java.io.IOException ioex) {
            logger.warn("Error running typeperf: " + ioex.getMessage());
            return;
        }

        String[] csv = line.split(",");
        if (csv.length == 2) {
            String connectionCount = csv[1];
            // remove the extra pair of surrounding quotes
            connectionCount = connectionCount.substring(1, connectionCount.length()-2);
            double count = Double.parseDouble(connectionCount); 
            map.put(tcp_key, new DataValue(count, SECOND_MEAS_CREDIBILITY, "", PROVENANCE));
        }
    }
}
