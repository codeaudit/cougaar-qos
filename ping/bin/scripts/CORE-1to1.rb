=begin experiment

name: CORE-1to1-MiniPing
description: Run MiniPing with the CORE configuration.
script: MiniPing-TEMPLATE.rb
parameters:
 - config_name: CORE-1to1
 - strategy: CreateOneToOnePing
 - ping_pairs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 35, 40, 45, 50]
 - rules:
   - $CIP/csmart/config/rules/isat
   - $CIP/csmart/config/rules/yp
   - $CIP/csmart/config/rules/isat/uc3_nosec
   - $CIP/configs/ping-dynamic/fast-startup.rule 
 - community_rules:
 - sleep_time: 600
 - sleep_delta: 0
=end
CIP = ENV['CIP']

$:.unshift File.join(CIP, 'csmart', 'acme_scripting', 'src', 'lib')
$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src', 'redist')
$:.unshift File.join(CIP, 'configs')

require 'cougaar/scripting'
Cougaar::ExperimentDefinition.register(__FILE__)
