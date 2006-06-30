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

package org.cougaar.core.qos.tmatrix;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.servlet.ServletFrameset;


/*
 * Servlet which reflects the TrafficMatrix traffic flow between agents. 
 */
public class AgentFlowServlet
    extends ServletFrameset
{
    private LoggingService logging;
    private TrafficMatrix agentFlowSnapshot;

    private final java.text.DecimalFormat f4_2 = 
	new java.text.DecimalFormat("#0.00");
 
    private CommunityTrafficMatrixService agentFlowService;
    
    public AgentFlowServlet(ServiceBroker sb) {
	super(sb);
    
	logging = (LoggingService)
            sb.getService(this, LoggingService.class, null);
	
	// TrafficMatrix accessor service
	agentFlowService = (CommunityTrafficMatrixService)
	    sb.getService(this, CommunityTrafficMatrixService.class, null);
	if (agentFlowService == null) {
	    if (logging.isErrorEnabled()) {
		logging.error("Can't find CommunityTrafficMatrixService. This plugin must be loaded at Low priority");
	    }
	}
    }
    
    public String getPath() {
	return "/agent/traffic";
    }
    
    public String getTitle () {
	return "Detailed Community Traffic";
    }
  

    private void printCell(PrintWriter out, Object x)
    {
	out.print("<td>");
	out.print(x);
	out.print("</td>");
    }


    private void printCell(PrintWriter out, double x)
    {
	out.print("<td>");
	out.print(f4_2.format(x));
	out.print("</td>");
    }
	    

    public void printMatrix(PrintWriter out) {


	out.println("TrafficMatrix:<p>");
	out.println("<table border=1>");

	TrafficMatrix.TrafficIterator itr = agentFlowSnapshot.getIterator();
	TrafficMatrix.TrafficRecord record;
        String orig;
	String target;
	
	out.println("<tr>");
	out.println("<th>Source</th>");
	out.println("<th>Destination</th>");
	out.println("<th>Msg/sec</th>");
	out.println("<th>Bytes/sec</th>");
	out.println("</tr>");
	while (itr.hasNext()) {
	    out.println("<tr>");
	    record = (TrafficMatrix.TrafficRecord) itr.next();	    	    
	    orig = itr.getOrig().toString();
	    target = itr.getTarget().toString();
	    printCell(out, orig);
	    printCell(out, target);
	    printCell(out, record.msgCount);
	    printCell(out, record.byteCount);
	    out.println("</tr>");
	}
	out.println("</table>");
    }

    public void printPage(HttpServletRequest request, PrintWriter out) {
	agentFlowSnapshot = agentFlowService.snapshotMatrix();
	printMatrix(out);
    }
}
