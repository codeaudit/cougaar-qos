
=begin script

include_path: ping-include.rb
description: Establishes MiniPing between Sources & Sinks

=end

require 'ping.rb'

insert_before :transformed_society do
  do_action "MultiAddPing", parameters[:sources], parameters[:targets], parameters[:ping_counts], {'eventMillis' => '1000'}
  do_action "SetupPingTimers", 1000
end

insert_after :end_of_run do
  do_action "PostMiniPingResults", "u111", "miniping", "ping", "p0ng", parameters[:ping_counts], parameters[:sources].size, parameters[:targets].size
end
