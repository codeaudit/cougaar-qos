##
#  <copyright>
#   
#   Copyright 2003-2004 BBNT Solutions, LLC
#   under sponsorship of the Defense Advanced Research Projects
#   Agency (DARPA).
#  
#   You can redistribute this software and/or modify it under the
#   terms of the Cougaar Open Source License as published on the
#   Cougaar Open Source Website (www.cougaar.org).
#  
#   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#   
#  </copyright>
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
