#########################################
# Stimulate and Detect stresses on a MiniPing society
# Choose from 3 configurations of ping layouts: 
#  -one-to-one, src-to-multiple-sinks, or multiple-srcs-to-sink
# Can be used as a model to stress-out the larger society
#
#
CIP = ENV['CIP']

$:.unshift File.join(CIP, 'csmart', 'acme_scripting', 'src', 'lib')
$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src', 'redist')
$:.unshift File.join(CIP, 'ping', 'configs')

require 'cougaar/scripting'
require 'ultralog/scripting'
require 'ping-dynamic/scripting'
#require 'polaris/tools'

include Cougaar

# Vars
PING_HOME = "#{CIP}/ping/configs/ping-dynamic"
UC3_HOME = "#{CIP}/csmart/config/rules/robustness/UC3"
COMMUNITY_RULE = "#{CIP}/csmart/config/rules/robustness/communities/community.rule"
runcount=0

HOSTS_FILE = Ultralog::OperatorUtils::HostManager.new.get_hosts_file

Cougaar::ExperimentMonitor.enable_stdout
Cougaar::ExperimentMonitor.enable_logging
#Polaris::Monitor.enable_polaris("u183", 80)

Cougaar.new_experiment().run(parameters[:ping_pairs].length) {
  do_action "LoadSocietyFromXML", "#{PING_HOME}/Empty.xml"

  # Select your society config here
  do_action "InfoMessage", "#{parameters[:strategy]}, #{parameters[:ping_pairs][runcount]}, #{parameters[:security]}, #{parameters[:hosts]}"
  do_action parameters[:strategy], parameters[:ping_pairs][runcount].to_i, "#{parameters[:security]}", "#{parameters[:hosts]}"
  
  do_action "MapHosts", HOSTS_FILE

  do_action "TransformSociety", false, *parameters[:rules]
  if (!parameters[:community_rules].nil?)
    do_action "TransformSociety", false, *parameters[:community_rules]
  end

  # These are good for deubugging 
  do_action "SaveCurrentSociety", "mySociety.xml"
  do_action "SaveCurrentCommunities", "myCommunities.xml"
  
  do_action "StartCommunications"
  do_action "CleanupSociety"  
  do_action "VerifyHosts"
  do_action "ConnectOperatorService"
  do_action "ClearPersistenceAndLogs"
  do_action "DeployCommunitiesFile"   
  do_action "StartSociety", 300, true

  do_action "Sleep", parameters[:sleep_time].to_i + (runcount * parameters[:sleep_delta].to_i)
  #wait_for "Command", "shutdown"
  do_action "PostMiniPingResults", "u111", "miniping", "ping", "p0ng", parameters[:ping_pairs][runcount]
  do_action "StopSociety"
  do_action "ArchiveLogs"
  do_action "CleanupSociety"  
  do_action "StopCommunications"

  runcount+=1
}
