#########################################
# Stimulate and Detect stresses on a MiniPing society
# Choose from 3 configurations of ping layouts: 
#  -one-to-one, src-to-multiple-sinks, or multiple-srcs-to-sink
# Can be used as a model to stress-out the larger society
#
#
CIP = ENV['CIP']
RULES = File.join(CIP, 'csmart','config','rules')

$:.unshift File.join(CIP, 'csmart', 'acme_scripting', 'src', 'lib')
$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src', 'redist')
$:.unshift File.join(CIP, 'configs')

require 'cougaar/scripting'
require 'ultralog/scripting'
require 'ping-dynamic/scripting'


# Vars
PING_HOME = "#{CIP}/configs/ping-dynamic"
METRICS_Home = "#{CIP}/csmart/config/rules/metrics"
UC3_HOME = "#{CIP}/csmart/config/rules/robustness/UC3"
DEBUG_HOME = "#{CIP}/csmart/config/rules/debug_rules"
COMMUNITY_RULE = "#{CIP}/csmart/config/rules/robustness/communities/community.rule"

HOSTS_FILE = Ultralog::OperatorUtils::HostManager.new.get_hosts_file
#HOSTS_FILE = "#{CIP}/operator/ARsmall-hosts.xml"

Cougaar::ExperimentMonitor.enable_stdout
Cougaar::ExperimentMonitor.enable_logging

security=true
runcount=10
bandwidth=[30.0, 10.0, 3.0, 1.0, 0.03, 0.01]
Cougaar.new_experiment("Mini-Ping-Security").run(1) {

  do_action "LoadSocietyFromXML", "#{PING_HOME}/Empty.xml"

  # Select your society config here
  do_action "CreateOneToOnePing", 5, "true"
  #do_action "CreateSrcToMultSinkPing", 3, "true"
  #do_action "CreateMultSrcToSinkPing", 3, "true"

  do_action "MapHosts", HOSTS_FILE

  # UC3 rules and rss info
  do_action "TransformSociety", false,
    "#{RULES}/isat",
    "#{RULES}/metrics/basic",
    "#{RULES}/metrics/sensors",
    "#{RULES}/metrics/serialization",
    "#{RULES}/robustness/manager.rule",
#    "#{RULES}/isat/uc3_nosec",
    "#{RULES}/metrics/rss/tic",
    "#{RULES}/robustness/UC3/UC3_managers.rule",
    "#{RULES}/robustness/UC3/UC3_node_compress.rule",
    "#{RULES}/robustness/UC3/UC3_node_detectors.rule",
    "#{RULES}/robustness/UC3/UC3_node_fuse.rule",
    "#{RULES}/robustness/debug_rules/taskServlet.rule",
    "#{RULES}/robustness/UC3/UC3_node_sysconds.rule",
    "#{RULES}/robustness/UC3/UC3_logging.rule",
#    "#{RULES}/robustness/UC3/UC3_node_trafficmask.rule", 
#    "#{RULES}/robustness/UC3/UC3_node_ssl.rule",
#    "#{RULES}/robustness/UC3/UC3_node_adaptable_ssl.rule", 
    "#{RULES}/robustness/manager.rule",
    "#{PING_HOME}/fast-startup.rule",
    "#{RULES}/robustness/debug_rules/quo-servlet.rule",
    "#{RULES}/security",
    "#{RULES}/robustness/uc1"
    #"#{RULES}/robustness/debug_rules/quo.rule"

  do_action "TransformSociety", false,
    "#{COMMUNITY_RULE}"

  # These are good for deubugging 
  do_action "SaveCurrentSociety", "mySociety.xml"
  do_action "SaveCurrentCommunities", "myCommunities.xml"
  
  do_action "StartJabberCommunications"
  do_action "VerifyHosts"
  do_action "CleanupSociety"
  do_action "ConnectOperatorService"
  do_action "ClearPersistenceAndLogs"
  do_action "DeployCommunitiesFile"   


=begin
  ############## Bandwidth attack #############
  # Start the Network Shaping
  do_action "EnableNetworkShaping"
  do_action "DefineWANLink", "link 2-4","BW-router" , 202, 204
  do_action "DefineWANLink", "link 4-2","BW-router" , 204, 202
  do_action "Sleep", 30.seconds

  # Restore Bandwidth in case it's squeezed from the last run. 
  #do_action "SetBandwidth", "link 2-4", 0.5
  #do_action "Sleep", 10.seconds
  #do_action "SetBandwidth", "link 4-2", 0.5

  #iperf check
  wait_for "Command", "ok"

  # Set the bandwidth to 56K (0.05M)
  #do_action "SetBandwidth", "link 2-4", 0.1 #bandwidth[runcount]
  #do_action "Sleep", 10.seconds
  #do_action "SetBandwidth", "link 4-2", 0.1 #bandwidth[runcount]

  # iperf check
  #do_action "InfoMessage", "Check the bandwidth between hosts with iperf now.."
  #wait_for "Command", "ok"
=end

  # Start up society with bandwidth shaping capability  
  #do_action "StartSociety", 300, true

  # Start up society
  do_action "StartSociety"

=begin
  do_action "InfoMessage", "Stress out the society"
  do_action "InfoMessage", "We need to wait @2minutes for society initialization"
  do_action "Sleep", 2.minutes
  do_action "InfoMessage", "Ready to stress the society manually?"  
  wait_for "Command", "ok"

  ########## Manual Attack ############
  # Performs general Dos Attack
  do_action "InfoMessage", "stressing manually..."
  do_action "DynamicUC3Attack", "FWD-ARManager", "10"
  do_action "Sleep", 20.seconds  # wait on slow http get 
  do_action "DetectUC3Attack"
  do_action "InfoMessage", "you should look at the ARManager's DosServlet for confirmation"
  do_action "RemoveUC3Attack", "FWD-ARManager"
  do_action "Sleep", 20.seconds  
  do_action "InfoMessage", "look once more at the DosServlet"
  do_action "InfoMessage", "ready to stress with Bandwidth shaping? (say ok)"
  wait_for "Command", "ok" 
=end

  ######### Bandwidth shaping Attack ########
  # Only do when society is up and stable, else won't work!
  #do_action "InfoMessage", "stressing with Bandwidth shaping..."

=begin
  # already shaped to 0.1, so just inform society of bandwidth
  do_action "UploadNetworkMetrics", "#{PING_HOME}/socVMiniPing-network.xml", "All", 0.2
  do_action "InfoMessage", "you can grep log files for Compress events or Bandwidth attacks(not yet implemented)"
  #wait_for "Command", "ok"
=end

  ######### Port Attack #########
  # Unfortunately the stressors are not yet implemented
  # sorry
  # same with socket attack

  #wait_for "Command", "shutdown"
  do_action "Sleep", 8.minutes
  do_action "StopSociety"
  do_action "ArchiveLogs"
  do_action "StopCommunications"

  runcount-=1
}
