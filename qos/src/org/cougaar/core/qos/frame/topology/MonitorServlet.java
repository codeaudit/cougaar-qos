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

package org.cougaar.core.qos.frame.topology;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.FrameSetService;
import org.cougaar.core.qos.metrics.MetricsServlet;
import org.cougaar.core.qos.metrics.ServletUtilities;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.Sortings;

/**
 */
public class MonitorServlet extends MetricsServlet 
{

    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   FrameSetService fss; 
    ServiceBroker sb;
    private LoggingService log;
    private String frameSetName;

    public MonitorServlet(ServiceBroker sb, LoggingService log,  String frameSetName)
    {
	super(sb);
	this.sb = sb;
	this.frameSetName = frameSetName;
	this.log = log;
    }

    /** @return a default path if a plugin parameter is not specified */
    @Override
   public String getPath() {
	return "/metrics/society/monitor";
    }

    @Override
   public String getTitle () {
	return "Society Monitor";
    }
    

    /** 
     * This method is called whenever the browser loads our URL. 
     * */
    @Override
   public void printPage(HttpServletRequest request, PrintWriter out){
	// Begin our HTML page response
	//response.setContentType("text/html");
	//PrintWriter out = response.getWriter();
	out.println("<html><body><p>");
	printTable(out, findOrMakeFrameSet());
	out.println("</body></html>");
    }

    // Delay looking up frameset until first call to the webpage
    private FrameSet fs;
    private FrameSet findOrMakeFrameSet() {
	if (fs != null) return fs;
	fss = sb.getService(this, FrameSetService.class, null);
	if (fss == null && log.isWarnEnabled()) {
	    log.warn("Unable to obtain FrameSetService");
	}
	FrameSet fs = fss.findFrameSet(frameSetName, null);
	return fs;
    }
    
    private static class ThingComparator  implements java.util.Comparator 
    {
	public int compare(Object x, Object y) 
	{
	    String x_name = ((Thing) x).getName();
	    String y_name = ((Thing) y).getName();
	    return x_name.compareTo(y_name);
	}

	@Override
   public boolean equals(Object thing) 
	{
	    return thing == this;
	}
    }

    private static final java.util.Comparator cmp = new ThingComparator();

    private  void printHeaderRow(PrintWriter out)
    {
	out.println("<tr frame=below>");
	out.println("<th>Host</th>");
	out.println("<th>Node</th>");
	out.println("<th>Agent</th>");
	out.println("<th>Heard From</th>");
	out.println("<th>Load Avg.</th>");
	out.println("<th>Msg In</th>");
	out.println("<th>Msg Out</th>");	
	out.println("<th>Size</th>");
	out.println("</tr>");
    }

    private  void printHostRow(PrintWriter out, Host myHost)
    {
	out.println("<tr><td colspan=3>"+ myHost.getName()+"</td>");
	out.println("<td>&nbsp;</td>"); // Heard From
	ServletUtilities.valueTable(myHost.getLoadAverage(),0.0, 1.0, true, f4_2,out);
	out.println("<td>&nbsp;</td>"); // Msg In
	out.println("<td>&nbsp;</td>"); // Msg Out
	out.println("<td>&nbsp;</td>"); // Size
	//ServletUtilities.valueTable(myHost.getEffectiveMJips(),0.0, 100000000.0, true, f7_0,out);
	out.println("</tr>");
    }

    private void printNodeRow(PrintWriter out, Node myNode)
    {
	out.println("<tr><td>&nbsp;</td>"); // Host
	out.println("<td colspan=2>" + myNode.getName() + "</td>"); // Node column
		out.println("<td>&nbsp;</td>"); // Heard From
	ServletUtilities.valueTable(myNode.getCpuLoadAverage(), 0.0, 1.0, true, f4_2,out);
	ServletUtilities.valueTable(myNode.getMsgIn(),0.0, 2.0, true, f4_2,out);
	ServletUtilities.valueTable(myNode.getMsgOut(),0.0, 2.0, true, f4_2,out);
	ServletUtilities.valueTable(myNode.getVmSize(),0.0, 100000000.0, true, f7_0,out);
	out.println("</tr>");
    }

    private void printAgentRow(PrintWriter out, Agent myAgent)
    {
	out.println("<tr><td colspan=2>&nbsp;</td>"); // Host Node
	out.println("<td>" + myAgent.getName()+ "</td>"); // Agent column
	ServletUtilities.valueTable(myAgent.getHeardFrom(),0.0, 60, true, f4_0, out);
	ServletUtilities.valueTable(myAgent.getCpuLoadAverage(),0.0, 1.0, true, f4_2,out);
	ServletUtilities.valueTable(myAgent.getMsgIn(),0.0, 2.0, true, f4_2,out);
	ServletUtilities.valueTable(myAgent.getMsgOut(),0.0, 2.0, true, f4_2,out);
	ServletUtilities.valueTable(myAgent.getPersistSizeLast(),0.0, 100000, true, f7_0,out);
	out.println("</tr>");
    }

    private void printTable(PrintWriter out, FrameSet fs)
    {
	out.println("<table border=3 cellpadding=2 rules=groups>");
	out.println("<colgroup span=3><colgroup><colgroup><colgroup><colgroup>");
	out.println("<THEAD>");
	printHeaderRow(out);
	out.println("</THEAD>");
	out.println("<TFOOT></TFOOT>");

	Set hostFrames = fs.findFrames("host", null);
	for (Iterator h = Sortings.sort(hostFrames, cmp).iterator(); h.hasNext();)
	{
	    Host host = (Host)h.next();
	    out.println("<TBODY>");
	    printHostRow(out, host);

	    Set nodeFrames = fs.findRelations(host, "child" , "NodeOnHost");
	    for (Iterator n = Sortings.sort(nodeFrames, cmp).iterator(); n.hasNext();)
	    {
		Node node = (Node)n.next();
		out.println("<TBODY>");
		printNodeRow(out, node);

		Set agentFrames = fs.findRelations(node, "child", "AgentInNode");
		for (Iterator a = Sortings.sort(agentFrames, cmp).iterator(); a.hasNext();)
		{
		    Agent agent = (Agent)a.next();
		    printAgentRow(out, agent);
		}
		//out.println("</TBODY>");
	    }
	    //out.println("</TBODY>");
	}
	out.println("</table>");
    }
}
