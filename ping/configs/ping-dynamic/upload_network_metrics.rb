##
#  <copyright>
#  Copyright 2003 BBN Technologies, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
#
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>
#
CIP = ENV['CIP']

$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src')
$:.unshift File.join(CIP, 'csmart', 'config', 'rules')
$:.unshift File.join(CIP, 'configs')

require 'plugins/acme_net_shape/vlan.rb'
require 'cougaar/scripting'
require 'ultralog/scripting'
# require code to read network.xml file

require 'ping-dynamic/networkXMLtoMetricParser.rb'

module Cougaar
  module Actions
    # upload network XML metrics to one or more nodes.
    class UploadNetworkMetrics < Cougaar::Action
      PRIOR_STATES = ["SocietyRunning"]
      DOCUMENTATION = Cougaar.document {
        @description = "Upload network XML metrics to one or more nodes."
        @parameters = [
          {:filename => "required, xml file to read"},
          {:node => "required, Name of agent (or 'All' for all nodes) ."},
          {:bandwidth => "optional, the bandwidth shape of the miniping vlan in the network config file."},
        ]
        @example = "do_action 'UploadNetworkMetrics', 'socVA-network.xml', 'All', 0.05"
      }

      attr_accessor :filename, :node, :bandwidth

      def initialize(run, filename, node, bandwidth=0.05)
        super(run)
          @filename = filename
          @node = node
	  @bandwidth = bandwidth
      end

      def perform
        # read xml file
        metrics = read_xml_metrics(filename, bandwidth)
        unless metrics
          @run.error_message "UploadNetworkMetrics failed. No metrics data: #{one_node}"
          return;
        end

        # upload to node(s)
        if @node == "All"
          @run.society.each_node do |n|
            send_metrics n.name, metrics
          end
        else
          send_metrics @node, metrics
        end
      end

      # Process a single node (node)
      def send_metrics (one_node, metrics)

        # FIXME: Do a transform_society for any reason?

        cougaar_node = @run.society.nodes[one_node]

        # If didn't find it as an node, try as a node
        unless cougaar_node
          cougaar_node = @run.society.nodes[one_node]
          unless cougaar_node
            @run.error_message "UploadNetworkMetrics failed. Unknown node: #{one_node}"
            return;
          end
        end

        # find the correct host:port address
=begin
	  uri=Cougaar::Communications::HTTP.get("#{cougaar_node.uri}/$#{cougaar_node.name}/list")
          unless uri
            @run.error_message "UploadNetworkMetrics failed to redirect to node: #{one_node}"
	    puts "Result went from #{cougaar_node.uri}/$#{cougaar_node.name}/list to #{uri}"
            sleep(100)
            continue;
          end
=end

	$COUGAAR_DEBUG=true;

        # send metrics
	prefix="#{cougaar_node.uri}/$#{one_node}/metrics/writer";
        #prefix="#{uri.scheme}://#{uri.host}:#{uri.port}/$#{one_node}";
        metrics.each { |metric|
          query="#{prefix}#{metric}";
          puts "Query is: #{query}" 
          result = Cougaar::Communications::HTTP.get(query) 
          puts "result from metrics service = #{result}"
        }
      end

    end
  end
end
