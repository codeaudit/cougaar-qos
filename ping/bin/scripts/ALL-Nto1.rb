=begin experiment

name: ALL-Nto1-MiniPing
description: Run MiniPing with the ALL configuration.  One Source pair to many sinks.
script: MiniPing-TEMPLATE.rb
parameters:
 - config_name: ALL-Nto1
 - strategy: CreateMultSrcToSinkPing
 - ping_pairs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 35, 40, 45, 50]
 - rules:
   - $CIP/csmart/config/rules/isat
   - $CIP/csmart/config/rules/yp
   - $CIP/configs/ping-dynamic/fast-startup.rule 
   - $CIP/csmart/config/rules/robustness/manager.rule
   - $CIP/csmart/config/rules/robustness/debug_rules/taskServlet.rule
   - $CIP/csmart/config/rules/robustness/uc1
   - $CIP/csmart/config/rules/robustness/UC3
   - $CIP/csmart/config/rules/robustness/uc4
   - $CIP/csmart/config/rules/robustness/uc7
   - $CIP/csmart/config/rules/robustness/uc9
   - $CIP/csmart/config/rules/metrics/basic
   - $CIP/csmart/config/rules/metrics/sensors
   - $CIP/csmart/config/rules/metrics/serialization/metrics-only-serialization.rule
   - $CIP/csmart/config/rules/metrics/rss/tic
   - $CIP/csmart/config/rules/security
 - community_rules:
   - $CIP/csmart/config/rules/security/communities
   - $CIP/csmart/config/rules/robustness/communities/community.rule
 - sleep_time: 600
 - sleep_delta: 0
=end
CIP = ENV['CIP']

$:.unshift File.join(CIP, 'csmart', 'acme_scripting', 'src', 'lib')
$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src', 'redist')
$:.unshift File.join(CIP, 'configs')

require 'cougaar/scripting'
Cougaar::ExperimentDefinition.register(__FILE__)
