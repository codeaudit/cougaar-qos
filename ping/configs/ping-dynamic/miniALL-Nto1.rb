=begin experiment

name: miniALL-Nto1-MiniPing
description: Run MiniPing with the N-1 configuration.  One Many srcs to one sink.
script: MiniPing-TEMPLATE.rb
parameters:
 - config_name: miniALL-Nto1
 - strategy: CreateOneToOnePing
 - ping_pairs: [100, 200, 4]
 - rules:
   - $CIP/csmart/config/rules/isat
   - $CIP/csmart/config/rules/yp
   - $CIP/configs/ping-dynamic/fast-startup.rule
   - $CIP/configs/ping-dynamic/quietLog.rule 
   - $CIP/csmart/config/rules/robustness/manager.rule
   - $CIP/csmart/config/rules/robustness/debug_rules/taskServlet.rule
   - $CIP/csmart/config/rules/metrics/basic
   - $CIP/csmart/config/rules/metrics/sensors
   - $CIP/csmart/config/rules/metrics/serialization/metrics-only-serialization.rule
   - $CIP/csmart/config/rules/metrics/rss/tic
 - community_rules:
   - $CIP/csmart/config/rules/robustness/communities/community.rule
 - sleep_time: 1200
 - sleep_delta: 0
=end
CIP = ENV['CIP']

$:.unshift File.join(CIP, 'csmart', 'acme_scripting', 'src', 'lib')
$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src', 'redist')
$:.unshift File.join(CIP, 'configs')

require 'cougaar/scripting'
Cougaar::ExperimentDefinition.register(__FILE__)
