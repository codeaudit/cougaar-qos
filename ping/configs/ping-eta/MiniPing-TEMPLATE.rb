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

Cougaar.new_experiment().run(parameters[:run_count]) {
  do_action "LoadSocietyFromScript", parameters[:society_file]
  do_action "LayoutSociety", parameters[:layout_file], HOSTS_FILE
  
  do_action "TransformSociety", false, *parameters[:rules]
  if (!parameters[:community_rules].nil?)
    do_action "TransformSociety", false, *parameters[:community_rules]
  end

at :transformed_society  
 # These are good for deubugging 
  do_action "SaveCurrentSociety", "mySociety.xml"
  do_action "SaveCurrentCommunities", "myCommunities.xml"
  
  do_action "StartCommunications"
  do_action "CleanupSociety"  
  do_action "VerifyHosts"
  do_action "ConnectOperatorService"
  do_action "ClearPersistenceAndLogs"
  do_action "DeployCommunitiesFile"   

at :setup_run

  do_action "StartSociety", 300, true

at :wait_for_initialization
at :society_running
at :during_stage_1
  do_action "Sleep", parameters[:sleep_time].to_i + (runcount * parameters[:sleep_delta].to_i)

at :end_of_run
at :society_frozen 
  do_action "StopSociety"
at :society_stopped
  do_action "ArchiveLogs"
  do_action "CleanupSociety"  
  do_action "StopCommunications"

  runcount+=1
}
