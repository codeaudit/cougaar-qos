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

package org.cougaar.core.qos.gossip;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.servlet.ServletFrameset;
/**
 * Servlet to display Gossip overhead statistics.
 */
public class GossipServlet 
    extends ServletFrameset
{

    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private GossipStatisticsService statisticsService;

    public GossipServlet(ServiceBroker sb) {
	super(sb);
	statisticsService = sb.getService(this, GossipStatisticsService.class, null);
    }

    @Override
   public String getPath() {
	return "/metrics/gossip";
    }

    @Override
   public String getTitle() {
	return getNodeID() + " Gossip Statistics";
    }

    private void printRow(PrintWriter out,String label, int count) {
	out.print("<tr><b>");
	out.print("<td><b>");
	out.print(label);
	out.print("</b></td>");
	out.print("<td><b>");
	out.print(count);
	out.print("</b></td>");
	out.print("</b></tr>");
    }
    @Override
   public void printPage(HttpServletRequest request,
			  PrintWriter out)
    {
// 	String reset_string = request.getParameter("reset");
// 	boolean reset = reset_string != null && 
// 	    reset_string.equalsIgnoreCase("true");

	GossipTrafficRecord stats = null;
	if (statisticsService!=null) {
	    stats = statisticsService.getStatistics();
	}
	if (stats == null) {
	    out.print("<p><b>");
	    out.print("ERROR: Gossip Statistics Service is not Available\n");
	    out.print("</b><p> org.cougaar.core.mts.GossipStatisticsServiceAspect ");
	    out.print("should be loaded into Node \n");
	    return;
	}
	out.print("<h2>Gossip for node ");
	out.print(getNodeID());
	out.println("</h2>");
	out.print("<table border=1>\n");

	printRow(out,"Requests Sent",stats.getRequestsSent());
	printRow(out,"Requests Received",stats.getRequestsReceived());
	printRow(out,"Values Sent",stats.getValuesSent());
	printRow(out,"Values Received",stats.getValuesReceived());
	printRow(out,"Messages with Gossip Sent",
		 stats.getMessagesWithGossipSent());
	printRow(out,"Messages with Gossip Received",
		 stats.getMessagesWithGossipReceived());
	printRow(out,"Total Messages Sent",
		 stats.getMessagesSent());
	printRow(out,"Total Messages Received",
		 stats.getMessagesReceived());
	out.println("</table>");

    }
}
