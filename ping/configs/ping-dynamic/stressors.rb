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