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
require 'cougaar/scripting'

module Cougaar
  module Actions

    # Inject a DosAttack by poking the ARManager's DosServlet
    # This is a general Dos Attack
    class DynamicUC3Attack < Cougaar::Action
      PRIOR_STATES = ["SocietyRunning"]
      DOCUMENTATION = Cougaar.document {
        @description = "Dynamically trigger attack via DosServlet."
        @example = "do_action 'DynamicUC3Attack'"
      }
      #attr_accessor :agent, level

      # Take the role and end date to change at this agent
      def initialize(run, agent, level)
        super(run)
	@agent = agent
	@level = level
	@protocol = "http"
      end
      
      def perform
        cougaar_agent = @run.society.agents[@agent]
        if cougaar_agent
          result = Cougaar::Communications::HTTP.get("#{cougaar_agent.uri}/dos?action=Add&level=#{@level}") 
          @run.error_message "Failed to trigger Dos Attack on #{cougaar_agent.uri}" unless result
        else
          @run.error_message "Dos Attack, unknown agent: #{@agent}"
        end
      end
    end
  
  class RemoveUC3Attack < Cougaar::Action
      PRIOR_STATES = ["SocietyRunning"]
      DOCUMENTATION = Cougaar.document {
        @description = "Dynamically remove attack via DosServlet."
        @example = "do_action 'RemoveUC3Attack'"
      }
      #attr_accessor :agent, level

      # Take the role and end date to change at this agent
      def initialize(run, agent)
        super(run)
	@agent = agent
	@protocol = "http"
      end
      
      def perform
        cougaar_agent = @run.society.agents[@agent]
        if cougaar_agent
          result = Cougaar::Communications::HTTP.get("#{cougaar_agent.uri}/dos?action=Remove") 
          @run.error_message "Failed to remove Dos Attack for #{cougaar_agent.uri}" unless result
        else
          @run.error_message "Dos Attack Removal, unknown agent: #{@agent}"
        end
      end
    end

    # Change the provider roles at an agent
    class DetectUC3Attack < Cougaar::Action
      PRIOR_STATES = ["SocietyRunning"]
      DEFAULT_TIMEOUT = 10.minutes
      DOCUMENTATION = Cougaar.document {
        @description = "Detect UC3 Response: Compression, Bandwidth, SSL, via CougaarEvents."
        @example = "do_action 'DetectUC3Attack'"
      }      
      def perform
        @run.comms.on_cougaar_event do |event|
        if event.component=="CompressingStreamsAspect"
          match = /.*Compression, ([^,]*), protocol=([^,]*), destination=([^0-9]*)/.match(event.data)
          if match
            value, protocol, dest = match[1,3]
            puts event
            if value == "true"
	      puts "Compression UC3 response detected!"
              attacks[1,3]=true
            end
          end
        elsif event.component=="TrafficMaskAspect"
          match = /.*TrafficMasking, ([^,]*), Node=([^,]*),/.match(event.data)
          if match
            value, node = match[1,2]
            puts event
            if value == "true"
	      puts "TrafficMasking UC3 response detected!"
              attacks[2,3]=true
            end
          end
        end
      end
    end

  end
end
end