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

public class LinuxSockStat extends SysStatHandler {
    private static final String TcpLineKey = "TCP: inuse ";
    private static final String UdpLineKey = "UDP: inuse ";

    private FileReader fr;
    private BufferedReader br;
    private String tcp_key, udp_key;

    public void initialize(String host, int pid) {
        tcp_key =
                "Host" + KEY_SEPR + host + KEY_SEPR + "Network" + KEY_SEPR + "TCP" + KEY_SEPR
                        + "sockets" + KEY_SEPR + "inuse";
        udp_key =
                "Host" + KEY_SEPR + host + KEY_SEPR + "Network" + KEY_SEPR + "UDP" + KEY_SEPR
                        + "sockets" + KEY_SEPR + "inuse";
    }

    private void close() {
        try {
            br.close();
        } catch (java.io.IOException ioe_ex) {
        }
    }

    private DataValue parse(String line_key, String line) {
        int start = line_key.length();
        int end = line.indexOf(' ', start);
        String doub = null;
        if (end == -1) {
            doub = line.substring(start);
        } else {
            doub = line.substring(start, end);
        }
        return new DataValue(Double.parseDouble(doub), SECOND_MEAS_CREDIBILITY, "", PROVENANCE);
    }

    public void getData(Map<String, DataValue> map) {
        String line = null;
        fr = null;
        try {
            fr = new FileReader("/proc/net/sockstat");
        } catch (java.io.FileNotFoundException fnf_ex) {
            return;
        }

        br = new BufferedReader(fr);

        while (true) {
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(TcpLineKey)) {
                    map.put(tcp_key, parse(TcpLineKey, line));
                } else if (line.startsWith(UdpLineKey)) {
                    map.put(udp_key, parse(UdpLineKey, line));
                }
            } catch (java.io.IOException io_ex) {
                close();
                return;
            }
        }

        close();
    }

}
