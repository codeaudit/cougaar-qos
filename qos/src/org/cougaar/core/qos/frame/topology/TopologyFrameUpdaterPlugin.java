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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.frame.DataFrame;
import org.cougaar.core.qos.frame.FrameSet;
import org.cougaar.core.qos.frame.FrameSetService;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.ParameterizedPlugin;
import org.cougaar.core.qos.rss.AgentHostUpdater;
import org.cougaar.core.qos.rss.AgentTopologyService;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.wp.WhitePagesService;
import org.cougaar.core.thread.SchedulableStatus;
import org.cougaar.core.wp.ListAllAgents;
import org.cougaar.util.UnaryPredicate;


/**
 * This class populates a frameset with frames that represent the host-node-agent hierarchy. 
 * The plugin polls the WP service for all agents in the society. 
 * For each agents, it polls the Metrics Topology Service to get the nodes and hosts which contain the agent.
 * It then adds DateFrames for the agent, node and host and changes the containment relationship 
 * as they move around. 
 */
public class TopologyFrameUpdaterPlugin 
extends ParameterizedPlugin {
    private static final long POLL_PERIOD = 3000; // 3 seconds
    private LoggingService log;
    private FrameSet frameSet;
    private AlarmService alarmService;
    private WhitePagesService wp;
    private AgentHostUpdater agentHostUpdater;
    private AgentTopologyService topologyService;
    private Set agents;
    private Set nodes;
    private Set hosts;
    
    private Set getAgentsFromWP() {
        Set justAgents=new HashSet();
        Set rawAgents=null;
        try {
            SchedulableStatus.beginWait("WP call for all Agents");
            rawAgents = ListAllAgents.listAllAgents(wp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            SchedulableStatus.endBlocking();
        }
        String agentName;
        MessageAddress agentAddress=null;
        for (Iterator iter = rawAgents.iterator(); iter.hasNext(); ) {
            agentName = (String) iter.next();
            try {
                SchedulableStatus.beginWait("WP call for version slot");       
                if (wp.get(agentName,"version") != null) {
                    agentAddress=MessageAddress.getMessageAddress(agentName);
                    justAgents.add(agentName);
//                    log.info("Agent="+ agentName + 
//                            " Node=" + topologyService.getAgentNode(agentAddress) +
//                            " Host=" + topologyService.getAgentHost(agentAddress) );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                SchedulableStatus.endBlocking();
            }
        }
        return justAgents;
    }
    
    // Couaar Topolopy Frameset helper methods

    private DataFrame addEntityToFrameset(String type, String name) {
        Properties prop = new Properties();
        prop.setProperty("name",name);
        return frameSet.makeFrame(type, prop );
    }
    
    private DataFrame addRelationshipToFrameset(String type, String parentName, String childName) {
        Properties prop = new Properties();
        prop.setProperty("parent-value",parentName);
        prop.setProperty("child-value",childName);
        return frameSet.makeFrame(type, prop );
    }
    
    private void addIndicatorsToAgent(String agent) {
        addEntityToFrameset("cpuIndicator", agent + "Cpu" );
        addEntityToFrameset("msgInIndicator", agent + "MsgIn" );
        addEntityToFrameset("msgOutIndicator", agent + "MsgOut" );
        addRelationshipToFrameset("IndicatorOnAgent", agent, agent + "Cpu");
        addRelationshipToFrameset("IndicatorOnAgent", agent, agent + "MsgIn");
        addRelationshipToFrameset("IndicatorOnAgent", agent, agent + "MsgOut");
    }
    
    private Agent addAgent(String agentName) {
        addIndicatorsToAgent(agentName);
        return (Agent) addEntityToFrameset("agent", agentName);
    }
    
    private Node addNode(String nodeName) {
        return (Node) addEntityToFrameset("node", nodeName);
    }
    
    private Host addHost(String hostName) {
        return (Host) addEntityToFrameset("host",hostName);
    }
    
    private AgentInNode addAgentInNode(String nodeName, String agentName) {
        return (AgentInNode) addRelationshipToFrameset("AgentInNode", nodeName, agentName);
    }
    
    private NodeOnHost addNodeOnHost(String hostName, String nodeName) {
        return (NodeOnHost) addRelationshipToFrameset("NodeOnHost", hostName, nodeName);
    }
    
    private AgentInNode findOrMakeOrMoveAgentInNode(String nodeName, String agentName) {
        //Check that agent is contained in node
        AgentInNode agentRelationship= (AgentInNode) frameSet.findFrame("AgentInNode","child-value",agentName);
        if (agentRelationship == null)
                agentRelationship = addAgentInNode(nodeName,agentName);
        //TODO agentRelationship.relationshipParent() use this instead (with different query for agentonframe)
        else if (agentRelationship.getParentValue()!=nodeName) {
            agentRelationship.setParentValue(nodeName);
            if (log.isInfoEnabled()){
                log.info("Agent=" + agentName + " moved to node=" +nodeName);
            }
        }
        return agentRelationship;
    }
    
    private NodeOnHost findOrMakeOrMoveNodeOnHost(String hostName, String nodeName) {
        NodeOnHost nodeRelationship = (NodeOnHost) frameSet.findFrame("NodeOnHost","child-value",nodeName);
        if (nodeRelationship == null) 
                nodeRelationship = addNodeOnHost(hostName,nodeName);
        else if (nodeRelationship.getParentValue()!=hostName) {
            frameSet.removeFrame(nodeRelationship);
            nodeRelationship = addNodeOnHost(hostName,nodeName);
//            if (log.isInfoEnabled()){
//                log.info("Node=" + nodeName + " moved to host=" +hostName);
//            }
        }
        return nodeRelationship;
    }
    
    private Host findOrMakeHost(String hostName) {
        Host hostFrame= (Host) frameSet.findFrame("host","name",hostName);
        if (hostFrame == null) {
            hostFrame = addHost(hostName);
        }
        return hostFrame;
    }
    
    private Node findOrMakeNode(String nodeName) {
        Node nodeFrame= (Node) frameSet.findFrame("node","name",nodeName);
        if (nodeFrame == null) {
            nodeFrame=addNode(nodeName);
        }
        return nodeFrame;
    }
    
    private Agent findOrMakeAgent(String agentName) {
        Agent agentFrame= (Agent) frameSet.findFrame("agent","name",agentName);
        if (agentFrame == null) {
            agentFrame=addAgent(agentName);
        }
        return agentFrame;
    }
    
    private Indicator findIndicator(String indicatorName) {
        Indicator indicatorFrame= (Indicator) frameSet.findFrame("indicator","name",indicatorName);
        return indicatorFrame;
    }
    
    private boolean assureAgentInFrameSet(String agentName) {
        //check that agent->node->hostin WP, before adding to frameset
        MessageAddress agentAddress=MessageAddress.getMessageAddress(agentName);
        String nodeName=topologyService.getAgentNode(agentAddress);
        if (nodeName==null) return false;
        String hostName=topologyService.getAgentHost(agentAddress);
        if (hostName==null) return false;
        // Assure  agent->node->host in frameset
        Agent agentFrame=findOrMakeAgent(agentName);
        Node nodeFrame=findOrMakeNode(nodeName);
        Host hostFrame=findOrMakeHost(hostName);
        AgentInNode agentRelationship = findOrMakeOrMoveAgentInNode(nodeName,agentName);
        NodeOnHost nodeRelationship = findOrMakeOrMoveNodeOnHost(hostName,nodeName);
        return true;
    }
    
    // TODO Determine the dyamic status of an indicator
    // We have to use dynamic slots, because of indicatory slots are not defined 
    // as part of indicator prototype, to get around a bug in the frame code)
    private String indicatorStatus(Indicator indicator) {
        //JAZ no need to use the dynamic accessors
        String watchSlot = (String) indicator.getValue("watchSlot");
        Metric watchMetric = (Metric) indicator.getValue(watchSlot);
        // If credibility too low declair status unknown
        if (watchMetric == null || watchMetric.getCredibility() <= 0.1) return "unknown";
        // get thresholds
        double idleThreshold = ((Double) indicator.getValue("idleThreshold")).doubleValue();
        double calmThreshold = ((Double) indicator.getValue("calmThreshold")).doubleValue();
        double normalThreshold = ((Double) indicator.getValue("normalThreshold")).doubleValue();
        double busyThreshold = ((Double) indicator.getValue("busyThreshold")).doubleValue();
        double franticThreshold = ((Double) indicator.getValue("franticThreshold")).doubleValue();
        double watchValue= watchMetric.doubleValue();
        // test status
        if (watchValue <= idleThreshold) return "idle";
        if (watchValue <= calmThreshold) return "calm";
        if (watchValue <= normalThreshold) return "normal";
        if (watchValue <= busyThreshold) return "busy";
        if (watchValue <= franticThreshold) return "frantic";
        return "frantic";
    }
    
    private String hostStatus(Host host) {
        //JAZ no need to use the dynamic accessors
        Metric loadAverage= (Metric) host.getValue("loadAverage");
        Metric cpuCount = (Metric) host.getValue("count");
        if (loadAverage == null || cpuCount ==null ||
                (loadAverage.getCredibility() <= 0.1) ||
                (cpuCount.getCredibility() <= 0.1)   ) return "unknown";
        double norm=loadAverage.doubleValue()/cpuCount.doubleValue();
        if (norm <= 0.0) return "idle";
        if (norm <= 0.2) return "calm";
        if (norm <= 0.5) return "normal";
        if (norm <= 1.0) return "busy";
        if (norm <= 2.0) return "frantic";
        return "frantic";
    }
    
    private String nodeStatus(Node node) {
        //JAZ no need to use the dynamic accessors
        Metric cpu= (Metric) node.getValue("cpuLoadAverage");
        Metric msgIn= (Metric) node.getValue("msgIn");
        Metric msgOut = (Metric) node.getValue("msgOut");
        if (cpu == null || msgIn ==null || msgOut ==null || 
                (cpu.getCredibility() <= 0.1) ||
                (msgIn.getCredibility() <= 0.1)  ||
                (msgOut.getCredibility() <= 0.1)  ) return "unknown";
        double norm= 0.34 * cpu.doubleValue() + 0.0033 * msgIn.doubleValue() + 0.0033 * msgOut.doubleValue();
        if (norm <= 0.0) return "idle";
        if (norm <= 0.2) return "calm";
        if (norm <= 0.5) return "normal";
        if (norm <= 1.0) return "busy";
        if (norm <= 2.0) return "frantic";
        return "frantic";
    }
    
    private String agentStatus(Agent agent) {
        Metric cpu= agent.getCpuLoadAverage();
        Metric msgIn= agent.getMsgIn();
        Metric msgOut =  agent.getMsgOut();
       // JAZ no need to use the dynamic accessors
//        Metric cpu= (Metric) agent.getValue("cpuLoadAverage");
//        Metric msgIn= (Metric) agent.getValue("msgIn");
//      Metric msgOut = (Metric) agent.getValue("msgOut");
        if (cpu == null || msgIn ==null || msgOut ==null || 
                (cpu.getCredibility() <= 0.1) ||
                (msgIn.getCredibility() <= 0.1)  ||
                (msgOut.getCredibility() <= 0.1)  ) return "unknown";
        double norm= 0.34 * cpu.doubleValue() + 0.0033 * msgIn.doubleValue() + 0.0033 * msgOut.doubleValue();
        if (norm <= 0.0) return "idle";
        if (norm <= 0.2) return "calm";
        if (norm <= 0.5) return "normal";
        if (norm <= 1.0) return "busy";
        if (norm <= 2.0) return "frantic";
        return "frantic";
    }
    
    
    private void updateIndicator(Indicator indicatorFrame) {
        String oldStatus=indicatorFrame.getValue("status").toString();
        String currentStatus=indicatorStatus(indicatorFrame);
//        if (log.isInfoEnabled()){
//            log.info("indicator=" +indicatorFrame.getName() +
//                    " oldStatus=" + oldStatus +
//                    " currentStatus=" + currentStatus );
//        }
        if (! oldStatus.equals(currentStatus)){
            indicatorFrame.setStatus(currentStatus);
        }
    }
    
    private void updateAgentStatus(Agent agentFrame) {
        String oldStatus=agentFrame.getValue("status").toString();
        String currentStatus=agentStatus(agentFrame);
        if (! oldStatus.equals(currentStatus)){
            agentFrame.setStatus(currentStatus);
        }
    }
    
    private void updateNodeStatus(Node nodeFrame) {
        String oldStatus=nodeFrame.getValue("status").toString();
        String currentStatus=nodeStatus(nodeFrame);
        if (! oldStatus.equals(currentStatus)){
            nodeFrame.setStatus(currentStatus);
        }
    }
    
    private void updateHostStatus(Host hostFrame) {
        String oldStatus=hostFrame.getValue("status").toString();
        String currentStatus=hostStatus(hostFrame);
        if (! oldStatus.equals(currentStatus)){
            hostFrame.setStatus(currentStatus);
        }
    }
    
    private void updateAgentIndicators(Agent agentFrame) {
        HashMap indicators=(HashMap) agentFrame.findRelationshipFrames("child","IndicatorOnAgent");
        Iterator itr=indicators.values().iterator();
        while(itr.hasNext()) {
            Indicator indicatorFrame = (Indicator) itr.next();
            updateIndicator(indicatorFrame);
        }
    }
    
    private class Poller implements Alarm {
        private long expirationTime;
        private long period;
        private boolean expired = false;
                
        public Poller(long period) {
            super();
            this.period = period;
            this.expirationTime = System.currentTimeMillis()+period;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean hasExpired() {
            return expired;
        }

        public boolean cancel() {
            boolean was = expired;  
            expired = true;
            return was;
        }
        
        public void restart() {
            this.expirationTime = System.currentTimeMillis()+period;
            expired = false;
            alarmService.addRealTimeAlarm(this);
        }
        
        public void expire() {
            // Poll WP for all agents in society and assure they are in FrameSet.
            String agentName;
            Set agents=getAgentsFromWP();
            for (Iterator iter = agents.iterator(); iter.hasNext(); ) {
                agentName = (String) iter.next();
                boolean added=assureAgentInFrameSet(agentName);
//                if (log.isInfoEnabled()){
//                    log.info("assure Agent=" + agentName + " added=" +added);
//                }
//                if (log.isInfoEnabled() && added){
//                    Indicator msgOut = findIndicator(agentName+"MsgOut");
//                    Indicator msgIn = findIndicator(agentName+"MsgIn");
//                    Indicator cpu = findIndicator(agentName+"Cpu");
//                    log.info("agent=" +agentName+
//                            " MsgIn=" +indicatorStatus(msgIn)+
//                            " MsgOut=" +indicatorStatus(msgOut)+
//                            " CPU=" +indicatorStatus(cpu));
//                }
            }
            restart();
         }
    }
    
    public void load() {
        super.load();
        ServiceBroker sb = getServiceBroker();
        log = (LoggingService)
        sb.getService(this, LoggingService.class, null);
        alarmService = (AlarmService)  sb.getService(this, AlarmService.class, null);
        wp = (WhitePagesService)  sb.getService(this, WhitePagesService.class, null);
        agentHostUpdater = (AgentHostUpdater) sb.getService(this, AgentHostUpdater.class, null);
        topologyService = (AgentTopologyService) sb.getService(this, AgentTopologyService.class, null);
    }
    
    public void start() {
         String files = (String) getParameter("frame-set-files",
                                             "org/cougaar/core/qos/frame/topology/cougaar-topology-protos.xml");
        String name = (String) getParameter("frame-set","societyTopology");
        
        if (files != null) {
            // Get a list of files from frame-set-files parameter
            StringTokenizer tk = new StringTokenizer(files, ",");
            String[] xml_filenames = new String[tk.countTokens()];
            int i =0;
            while (tk.hasMoreTokens()) xml_filenames[i++] = tk.nextToken();
            // Create Frameset
            ServiceBroker sb = getServiceBroker();
            BlackboardService bbs = getBlackboardService();
            FrameSetService fss = (FrameSetService)
            sb.getService(this, FrameSetService.class, null);
            frameSet = fss.loadFrameSet(name, xml_filenames, sb, bbs);
            // TODO remove Test Frames
//            addHost("test");
//            addNode("NODE2");
//            addAgent("agent");
//            addNodeOnHost("test","NODE2");
//            addAgentInNode("NODE2","agent");
            // Start Poller to discover agents from WP and add them to Frameset 
            alarmService.addRealTimeAlarm(new Poller(POLL_PERIOD)); 
            sb.releaseService(this, FrameSetService.class, fss);
        } else {
            if (log.isWarnEnabled())
                log.warn("No FrameSet XML files were specified");
        }
        super.start();
    }

    // Plugin code 
    // The plugin does two things:
    // 1) Since it owns the topology frame set, it must register changes with the blackboard
    //     using the frameset.ProcessQueue();
    // 2) It also monitors  host, node, agent, indicator frames for changes, 
    //    and updates their stautus
    //    A better solution is needed for monitoring indicators.
    //    the current frameset implementation 
    //    does not register changes to frames for changes in thier parent
    
    private IncrementalSubscription indicatorSubscription;
    private IncrementalSubscription agentSubscription;
    private IncrementalSubscription nodeSubscription;
    private IncrementalSubscription hostSubscription;
    
    
    private static final UnaryPredicate indicatorPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return (o instanceof Indicator);
        }
    }; 
    
    private static final UnaryPredicate agentPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return (o instanceof Agent);
        }
    }; 
    
    private static final UnaryPredicate nodePredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return (o instanceof Node);
        }
    }; 
    
    private static final UnaryPredicate hostPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return (o instanceof Host);
        }
    }; 
      
    protected void setupSubscriptions() {
        //JAZ can't directly monitor indcators, because slot changes do not propagate to childern
        //indicatorSubscription = (IncrementalSubscription) blackboard.subscribe(indicatorPredicate);
        agentSubscription = (IncrementalSubscription) blackboard.subscribe(agentPredicate);
        nodeSubscription = (IncrementalSubscription) blackboard.subscribe(nodePredicate);
        hostSubscription = (IncrementalSubscription) blackboard.subscribe(hostPredicate);
        }


    protected void execute() {
        // Process all the Frame.set changes. this has to be done for any frameset
        frameSet.processQueue();

        // Look for changes to specific types of frames
        if (agentSubscription.hasChanged()) {
            Iterator itr=agentSubscription.getChangedCollection().iterator();
            while(itr.hasNext()) {
                Agent agentFrame = (Agent) itr.next();
                updateAgentIndicators(agentFrame);
                updateAgentStatus(agentFrame);
            }
        }
        if (nodeSubscription.hasChanged()) {
            Iterator itr=nodeSubscription.getChangedCollection().iterator();
            while(itr.hasNext()) {
                Node nodeFrame = (Node) itr.next();
                updateNodeStatus(nodeFrame);
            }
        }
        if (hostSubscription.hasChanged()) {
            Iterator itr=hostSubscription.getChangedCollection().iterator();
            while(itr.hasNext()) {
                Host hostFrame = (Host) itr.next();
                updateHostStatus(hostFrame);
            }
        }
    }
    

    
    
    
}

