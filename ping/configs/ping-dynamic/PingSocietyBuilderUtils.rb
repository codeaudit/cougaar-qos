##
#  <copyright>
#  Copyright 2002 InfoEther, LLC
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

require 'cougaar/communities'
require 'ultralog/enclaves'

#############################
# Adds ping to pinger agents
#############################
def addPing(source, dest, numAgents=1, args=nil)
  srcAgent = @run.society.agents[source]
  destAgent = @run.society.agents[dest]
  puts "srcAgent = #{source}" 
  puts "destAgent = #{dest}"
  
  if args.nil?
    if numAgents < 100
      args =
	{'eventMillis' => '100000',    # delay between cougaar events
	'delayMillis' => '0',        # delay between pings
	'sendFillerSize' => '1000',     # extra bytes on send
	'sendFillerRand' => 'false',  # randomize send bytes
	'echoFillerSize' => '1000',     # extra bytes on ack
	'echoFillerRand' => 'false', # randomize ack bytes
	'startMillis' => '+60000'}  # time to start pinging  
      
    else  # We have LOTS of pingers, i.e. >=101, lengthen times
      args =
	{'eventMillis' => '100000',    # delay between cougaar events
	'delayMillis' => '0',        # delay between pings
	'sendFillerSize' => '1000',     # extra bytes on send
	'sendFillerRand' => 'false',  # randomize send bytes
	'echoFillerSize' => '1000',     # extra bytes on ack
	'echoFillerRand' => 'false', # randomize ack bytes
	'startMillis' => "+".concat((1000*numAgents).to_s) }  # time to start pinging  
    end
  end
  
  if srcAgent && destAgent
    srcAgent.add_component("org.cougaar.ping.PingAdderPlugin") do |c|
      c.classname = "org.cougaar.ping.PingAdderPlugin"
      c.add_argument("target=#{dest}")
      args.each_pair {|key, value|
        c.add_argument("#{key}=#{value}")
      }
    end
  end
end 

############################
# Manages Pings - wake time
############################
def managePings(wake_time)
  @run.society.each_agent do |agent|
    if agent.has_component?("org.cougaar.ping.PingAdderPlugin")
      unless agent.has_component?("org.cougaar.ping.PingTimerPlugin")
	c = agent.add_component("org.cougaar.ping.PingTimerPlugin")
	c.classname = "org.cougaar.ping.PingTimerPlugin"
	c.add_argument("#{wake_time}")
      end
    end
  end
end

#########################################################
# Make Enclaves with Manager / Security if loaded
# Add new host for Manager Agent & make it the NameServer
##########################################################
def addMgntNode(host, security)
  nsHost = @run.society.hosts[host]
  if !nsHost
    puts "No HOST2 to assign NameServer" 
  end
  
  if nsHost
    puts "NameServer host = #{host}" 
    # AR Manager
    nsHost.add_node('FWD-MGMT-NODE') do |node|
      node.add_facet({'role' => 'AR-Management'})
      puts "Added FWD-MGMT-NODE"
    end
    
    if security=="true"
      # AS Manager
      nsHost.add_node('SEC-MGMT-NODE') do |node|
        node.add_facet({'role' => 'AS-Management'})
        puts "Added SEC-MGMT-NODE"
      end

     # Temp make CA node on 'HOST3'
     @run.society.add_host('HOST3') do |host|
       host.add_node('ROOT-CA-NODE') do |node|
        node.add_facet({'role' => 'RootCertificateAuthority'})
        puts "Added ROOT-CA-NODE"
       end
       host.add_node('CA-NODE') do |node|
        node.add_facet({'role' => 'CertificateAuthority'}) 
        puts "Added CA-NODE"
       end
     end
    end

    # put all hosts in FWD
    @run.society.each_host do |host|
      host.add_facet({'enclave' => 'FWD'})
    end

    # make HOST2 the name server
    @run.society.nodes['FWD-MGMT-NODE'].add_facet({'role' => 'NameServer'})
  end
end

##############################################
# Builds all three styles of ping societies
##############################################
module Cougaar
  module Actions
    
    #######################
    # Map hosts dynamically   
    #######################
    class MapHosts < ::Cougaar::Action
      PRIOR_STATES = ["SocietyLoaded"]
      DOCUMENTATION = Cougaar.document {
        @description = "Map hosts file."
        @parameters = [
          {:hosts => "default=nil, If present, uses the hosts from this file instead of those in the layout file."}
        ]
        @example = "do_action 'LayoutSociety', 'sa-hosts.xml'"
      }
      
      def initialize(run, hosts)
        super(run)
        @hosts = hosts
      end
      
      def perform
        host_society = Cougaar::SocietyBuilder.from_xml_file(@hosts).society
        allhosts = []
        acmehosts = []
        host_society.each_host { |host| allhosts << host }
        host_society.each_service_host("acme") { |host| acmehosts << host }

        @run.society.each_host do |host|
          ahost = acmehosts.shift
          raise "Not enought hosts running acme in #{@hosts} for society" unless ahost
          host.name = ahost.name
          ahost.each_facet { |facet| host.add_facet(facet.clone) }
          allhosts.delete ahost
        end
        allhosts.each do |host|
          @run.society.add_host(host.name) do |newhost|
            host.each_facet { |facet| newhost.add_facet(facet.clone) }
          end
        end
      end
    end
    
    
    ####################################
    # Start configuration building here
    ####################################
    
    # Two hosts/nodes, multiple one-to-one pinging agent pairs
    class CreateOneToOnePing < Cougaar::Action
      numAgents=0	
      PRIOR_STATE = "SocietyLoaded"
      RESULTANT_STATE = "SocietyLoaded"      
      DOCUMENTATION = Cougaar.document {
        @description = "Create a one-to-one ping society definition."
        @parameters = [
          {:numAgents => "required, The number of agents in this corset-style society configuration"},
          {:security => "optional, boolean to mark security Management Node loaded"},
          {:singlenode => "optional, boolean to to indicate whether this should be run on One Node, One Host"}
        ]
        @example = "do_action 'CreateOneToOnePing', 'numAgents', 'security', 'singlenode'"
      }
      def initialize(run, numAgents, security="false", singlenode="false")
        super(run)
        @numAgents = numAgents
        @security = security
	@singlenode = singlenode
      end
      def perform
	if @singlenode == "true"
	  puts "Running on One Host----"
	  @run.society.add_host('HOST1') do |host|
	    host.add_node('NodeA') do |node|
	      i=0
	      puts("numAgents = #{@numAgents}")
	      while i < @numAgents
		node.add_agent("src#{i}")
		i+=1
	      end
	      
	      i=0
	      while i < @numAgents
		node.add_agent("sink#{i}")
		
		# Add pings here
		# Check for lots of pingers & pass into addPing
		addPing("src#{i}", "sink#{i}", @numAgents)
		
		# wake once every second to check ping timeouts
		managePings('1000')
		i+=1
	      end
	      
	      # Add Nameserver facet to NodeA
	      # Not adding Mgmt node ever
	      @run.society.nodes['NodeA'].add_facet({'role' => 'NameServer'})
	    end
	  end
	else
	  puts "Running on Two Hosts----"
	  @run.society.add_host('HOST1') do |host|
	    host.add_node('NodeA') do |node|
	      i=0
	      puts("numAgents = #{@numAgents}")
	      while i < @numAgents
		node.add_agent("src#{i}")
		i+=1
	    end
	    end
	  end
	  @run.society.add_host('HOST2') do |host|
	    host.add_node('NodeB') do |node|
	      i=0
	      while i < @numAgents
		node.add_agent("sink#{i}")
		# Add pings here
		addPing("src#{i}", "sink#{i}")
		# wake once every second to check ping timeouts
		managePings('1000')
		i+=1
	      end
	    end
	  end
	  # Add Manager Node / Agent & Nameserver
	  addMgntNode("HOST2", @security)
	end  #if
      end     # perform
    end     # CreateOneToOnePing
    
    ##
    # Two hosts One src node/agent to multiple nodes/agents 
    ## 
    class CreateSrcToMultSinkPing < Cougaar::Action
      numSinks=0
      PRIOR_STATE = "SocietyLoaded"
      RESULTANT_STATE = "SocietyLoaded"      
      DOCUMENTATION = Cougaar.document {
	@description = "Create a single-src to multiple-sink ping society definition."
	@parameters = [
	  {:numSinks => "required, the number of sinks in this single-src ping society configuration"},
          {:security => "optional, boolean to mark security loaded"},
	  {:hosts => "optional, boolean to split up sinks on their own hosts"}
	]
	@example = "do_action 'CreateSrcToMultSinkPing', 'numSinks', 'security', 'hosts'"
      }
      def initialize(run, numSinks, security="false", hosts="false")
	super(run)
	@numSinks = numSinks
	@security = security
	@hosts = hosts
      end
      def perform
	@run.society.add_host('HOST1') do |host|
	  host.add_node('NodeA') do |node|
	    # Add single src agent
	    node.add_agent("src")
	  end
	end
	puts "hosts boolean is: #{@hosts}"
	if @hosts=="true"
	  puts "Splitting of sinks on separate hosts"	 
	  # Index HOST# to be 1+i
	  i=0
	  tmp=0
	  while i < @numSinks
	    tmp=i+2
	    @run.society.add_host("HOST#{tmp}") do |host|
	      puts "Adding host: HOST#{tmp}"
	      puts "Adding SinkNode#{i}"
	      host.add_node("SinkNode#{i}") do |node|
		node.add_agent("sink#{i}")
		# Add pings here
		addPing("src", "sink#{i}")
		# wake once every second to check ping timeouts
		managePings('1000')
		i+=1
	      end
	    end
	  end
	else
	  @run.society.add_host('HOST2') do |host|
	    i=0
	    while i < @numSinks
	      puts "Adding SinkNode#{i}; not inside multsinks"
	      host.add_node("SinkNode#{i}") do |node|
		node.add_agent("sink#{i}")
		# Add pings here
		addPing("src", "sink#{i}")
		# wake once every second to check ping timeouts
		managePings('1000')
		i+=1
	      end
	    end
	  end
	end  # if
	# Add Manager Node / Agent & Nameserver
	addMgntNode("HOST2", "false")
      end     # perform
    end     # CreateSrcToMultSinkPing
    
    ##
    # Multiple sink nodes/agents to one node/agent
    ## 
    class CreateMultSrcToSinkPing < Cougaar::Action
      numSrcs=0
      PRIOR_STATE = "SocietyLoaded"
      RESULTANT_STATE = "SocietyLoaded"      
      DOCUMENTATION = Cougaar.document {
	@description = "Create multiple src-to-single-sink ping society definition."
	@parameters = [
	  {:numSrcs => "required, The number of srcs in this single-sink ping society configuration"},
          {:security => "optional, boolean to mark security loaded"},
	  {:hosts => "boolean to split up srcs on their own hosts"}
	]
	@example = "do_action 'CreateMultSrcToSinkPing', numSrcs, 'security', 'hosts'"
      }
      def initialize(run, numSrcs, security="false", hosts="false")
	super(run)
	@numSrcs = numSrcs
	@security = security
	@hosts = hosts
      end
      def perform
	if @hosts=="true"
	  puts "Splitting up srcs on multiple hosts"
	  # Add sink
	  @run.society.add_host('HOST1') do |host|
	    puts "Adding SinkNode"
	    host.add_node("SinkNode") do |node|
	      # Add src agent
	      node.add_agent("sink")
	    end
	  end
	  # Add srcs and pings
	  # Index HOST# to be 1+i
	  i=0
	  tmp=0
	  while i < @numSrcs
	    tmp=i+2
	    @run.society.add_host("HOST#{tmp}") do |host|
	      puts "Adding SrcNode#{i}"
	      host.add_node("SrcNode#{i}") do |node|
		node.add_agent("src#{i}")
		# Add pings here
		addPing("src#{i}", "sink")
		# wake once every second to check ping timeouts
		managePings('1000')
		i+=1
	      end
	    end
	  end
	else
	  @run.society.add_host('HOST1') do |host|  
	    host.add_node("SinkNode") do |node|
	      node.add_agent("sink")
	    end
	  end
	  @run.society.add_host('HOST2') do |host|  
	    # Add pings here
	    i=0
	    while i < @numSrcs
	      puts "Adding SrcNode#{i}"
	      host.add_node("SrcNode#{i}") do |node|
		node.add_agent("src#{i}")
		addPing("src#{i}", "sink")
		# wake once every second to check ping timeouts
		managePings('1000')
		i+=1
	      end
	    end
	  end
	end
	# Add Manager Node / Agent & Nameserver
	addMgntNode("HOST2", "false")
      end   # perform
    end    # CreateMultSrcToSinkPing
    
    ##
    # Multiple Pinger Pairs per hosts
    ## 
    class CreateMultPingPairsPerHost < Cougaar::Action
      numPingers=[]
      anchor="src"
      PRIOR_STATE = "SocietyLoaded"
      RESULTANT_STATE = "SocietyLoaded"      
      DOCUMENTATION = Cougaar.document {
	@description = "Create multiple pinger-pair per host society definition."
	@parameters = [
	  {:numPingers => "required, array with each cell defining the number of pinger pairs per host/node, default is one pair"},
	  {:anchor => "optional, specifies a src or destination anchor"}	
	]
	@example = "do_action 'CreateMultPingPairsPerHost', 'numPingers', 'anchor'"
      }
      def initialize(run, numPingers=[], anchor="src")
	super(run)
	@numPingers = numPingers
	@anchor = anchor
      end
      def perform
	if @numPingers.empty?
	  puts "Can't create society with empty pinger array. Specify how you want the ping pairs distributed across hosts. \n"
	  puts "e.g. [1,2,1]"
	else	  
	  # determine whether sink or src is the anchor
	  if @anchor=="src" 
	    puts "Splitting up src-anchor ping pairs on multiple hosts"
	    
	    # Add srcs
	    @run.society.add_host('HOST1') do |host|
	      puts "Adding SrcNode"
	      host.add_node("SrcNode") do |node|
		# Add src agent
		node.add_agent("src")
	      end
	    end
	    
	    # Add sinks
	    # Index HOST# to be tmp+2 to start at 'HOST2' 
	    tmp=2
	    i=0
	    while i < @numPingers.size
	      @run.society.add_host("HOST#{tmp}") do |host|
		puts "Adding SinkNode#{i}"
		host.add_node("SinkNode#{i}") do |node|
		  
		  j=0
		  while j < @numPingers[i]
		    node.add_agent("sink#{i}_#{j}")
		    # Add pings here
		    addPing("src", "sink#{i}_#{j}")
		    # wake once every second to check ping timeouts
		    managePings('1000')
		    j+=1
		  end
		end
	      end
	      tmp+=1
	      i+=1
	    end
	  else
	    puts "Splitting up dest-anchor ping pairs on multiple hosts"
	    
	    # Add sink
	    @run.society.add_host('HOST1') do |host|
	      puts "Adding SinkNode"
	      host.add_node("SinkNode") do |node|
		# Add sink agent
		node.add_agent("sink")
	      end
	    end
	    
	    # Add srcs
	    # Index HOST# to be tmp+2 to start at 'HOST2' 
	    tmp=2
	    i=0
	    while i < @numPingers.size
	      @run.society.add_host("HOST#{tmp}") do |host|
		puts "Adding SrcNode#{i}"
		host.add_node("SrcNode#{i}") do |node|
		  
		  j=0
		  while j < @numPingers[i]
		    node.add_agent("src#{i}_#{j}")
		    # Add pings here
		    addPing("sink", "src#{i}_#{j}")
		    # wake once every second to check ping timeouts
		    managePings('1000')
		    j+=1
		  end
		end
	      end
	      tmp+=1
	      i+=1
	    end	  
	  end
	end
	
	# Add Manager Node / Agent & Nameserver
	addMgntNode("HOST2", "false")
      end   # perform   
    end  # CreateMultPingPairsPerHost
    
  end   # Actions
end # Cougaar
