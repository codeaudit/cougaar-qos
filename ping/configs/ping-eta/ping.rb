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

require 'mysql'
require 'tempfile'

#
# Utility methods for creating a ping society
#

PingStat = Struct.new( "PingStat",
    :id, :visit, :time, :uid, :count,
    :limit, :min_rtt, :max_rtt, :mean_rtt, :dev_rtt,
    :delta_time, :delta_count, :sum_squares )

class PingDatabase
  def initialize( host, database, user, pass )
    @mysql = Mysql.new( host, user, pass, database )
  end

  def register_run( cip, name, num_pings, num_source, num_sink )
    sql = []
    sql << "insert into  runs "
    sql << "( install_path, run_name, num_pairs, num_source, num_sink ) values "
    sql << "( '#{cip}', '#{name}', #{num_pings}, #{num_source}, #{num_sink} )"

    @mysql.query(sql.join(" "))
    @mysql.insert_id()
  end

  def post_log( run_id, ps )
    timestamp = ps.time.strftime("%Y%m%d%H%M%S")

    sql = []
    sql << "insert into raw_data "
    sql << "( run_id, ping_id, visit, time, "
    sql << "ping_uid, ping_count, lmt, "
    sql << "min_rtt, max_rtt, mean_rtt, dev_rtt, "
    sql << "delta_time, delta_count, sum_squares )"
    sql << " values "
    sql << "( #{run_id}, #{ps.id}, #{ps.visit}, #{ps.time.to_f}, "
    sql << "'#{ps.uid}', #{ps.count}, #{ps.limit}, "
    sql << "#{ps.min_rtt}, #{ps.max_rtt}, #{ps.mean_rtt}, #{ps.dev_rtt}, "
    sql << "#{ps.delta_time}, #{ps.delta_count}, #{ps.sum_squares} )"

    @mysql.query( sql.join(" "))
  end
end

module Cougaar
  module Actions
    class AddPing < Cougaar::Action
      PRIOR_STATES = ["SocietyLoaded"]
      DOCUMENTATION = Cougaar.document {
        @description = "Transform society with added ping components"
        @parameters = [
          {:source=> "required, Source Agent"},
          {:dest=> "required, Destination Agent"},
          {:args=> "optional, hash of arguments"}
        ]
        @example = "do_action 'AddPing', 'AgentA', 'AgentB', {'eventMillis' => '10000'}"
      }
      def initialize(run, source, dest, args)
        super(run)
        @source = source
        @dest = dest
        @args = args
      end
      def perform
        srcAgent = @run.society.agents[@source]
        if !srcAgent
          raise "Agent #{@source} does not exist in society"
        end
        destAgent = @run.society.agents[@dest]
        if !destAgent
          raise "Agent #{@dest} does not exist in society"
        end

        srcAgent.add_component("org.cougaar.ping.PingAdderPlugin") do |c|
          c.classname = "org.cougaar.ping.PingAdderPlugin"
          c.add_argument("target=#{@dest}")
          @args.each_pair {|key, value|
            c.add_argument("#{key}=#{value}")
          }
          c.add_argument("ddpPingId=#{cnt}")
        end
      end
    end

    class MultiAddPing < Cougaar::Action
      DOCUMENTATION = Cougaar.document {
        @description = "Adds multiple pings from multiple sources to multiple sinks."
        @parameters = [
          {:sources => "required, Array of source agents for pings."},
          {:targets => "required, Array of target agents for pings."},
          {:ping_counts => "required, Array of # pings to create (index by runcount)."},
          {:args => "optional, Hash of arguments to pass to PingTimerPlugins."}]}

      def initialize( run, sources, targets, ping_counts, args )
        super( run)
        @sources = sources
        @targets = targets
        @ping_counts = ping_counts
        @args = args
      end

      def to_s
        "#{super}([#{@sources.join(' ,')}], [#{@targets.join(' ,')}], [#{@ping_counts.join(' ,')}], #{args})"
      end

      def perform
        num_pings = @ping_counts[@run.count]
        num_pings.times do |cnt|
          @sources.each do |source|
            srcAgent = @run.society.agents[source]
            @targets.each do |target|
              @run.info_message "Adding Ping between: #{source}/#{target}\##{cnt}"

              srcAgent.add_component("org.cougaar.ping.PingAdderPlugin|PING-#{cnt}") do |c|
                c.name = "org.cougaar.ping.PingAdderPlugin|PING-#{cnt}"
                c.classname = "org.cougaar.ping.PingAdderPlugin"
                c.add_argument("target=#{target}")
                @args.each_pair {|key, value|
                  c.add_argument("#{key}=#{value}")
                }
		c.add_argument("ddp_ping_no=#{cnt}")
              end
            end
          end
        end
      end
    end

    class SetupPingTimers < Cougaar::Action
      PRIOR_STATES = ["SocietyLoaded"]
      DOCUMENTATION = Cougaar.document {
        @description = "Transform society with ping components"
        @parameters = [
          {:wake_time=> "required, Time between ping checks"}
        ]
        @example = "do_action 'SetupPingTimers', '1000'"
      }
      def initialize(run, wake_time)
        super(run)
        @wake_time = wake_time
      end

      def perform
        @run.society.each_agent do |agent|
          if agent.has_component?("org.cougaar.ping.PingAdderPlugin|PING-0")
            unless agent.has_component?("org.cougaar.ping.PingTimerPlugin")
              c = agent.add_component("org.cougaar.ping.PingTimerPlugin")
              c.classname = "org.cougaar.ping.PingTimerPlugin"
              c.add_argument("#{@wake_time}")
            end
          end
        end
      end
    end

    class SetupCommunityPlugins < Cougaar::Action
      PRIOR_STATES = ["SocietyLoaded"]
      DOCUMENTATION = Cougaar.document {
        @description = "Transform society with community plugins"
        @parameters = []
        @example = "do_action 'SetupCommunityPlugins'"
      }
      def perform
        @run.society.each_agent(false) do |agent|
          unless agent.has_component?('org.cougaar.community.CommunityPlugin')
            agent.add_component('org.cougaar.community.CommunityPlugin') do |c|
              c.classname = 'org.cougaar.community.CommunityPlugin'
            end
          end
        end
      end
    end

    class PostMiniPingResults < ::Cougaar::Action
      DOCUMENTATION = Cougaar.document {
        @description = "Scan log files and post MiniPing data to database."
        @parameters = [
          {:hostname => "Hostname of the MySQL database."},
          {:database => "Database name of the MiniPing database."},
          {:username => "Username for the MiniPing database."},
          {:password => "Password for the MiniPing database."},
          {:ping_cnt => "Array of Number of Ping Pairs."},
          {:source_cnt => "Number of Sources."},
          {:sink_cnt => "Number of Sinks."}]
      }

      def initialize( run, hostname, database, username, password, ping_cnt, source_cnt, sink_cnt )
        super( run )
        @run = run
        @ping_db = PingDatabase.new( hostname, database, username, password )
        @hostname = hostname
        @database = database
        @username = username
        @password = password
        @ping_cnt = ping_cnt
        @src_cnt = source_cnt
        @snk_cnt = sink_cnt
      end

      def to_s
        "#{super}(#{@hostname}, #{@database}, #{@username}, XXX, #{@ping_cnt}, #{@src_cnt}, #{@snk_cnt})"
      end

      def process_pingstats( runId, pingStatsFile )
        pingRE = /(\S* \S*) INFO  - PingTimerPlugin - [^u]*(.*)/
        trimRE = /\s* (\S*)\s*/
        tsRE = /(\d\d\d\d)-(\d\d)-(\d\d) (\d\d):(\d\d):(\d\d),(\d\d\d)/
        pings = []
        ping_visit = []

        pingStatsFile.each_line { |line|
          match = pingRE.match( line )
          unless (match.nil?) then
            timestamp = match[1]
            rawdata = match[2]

            pairs = rawdata.split(/,/)
            data = Hash.new

            pairs.each { |pair|
              key, value = pair.split(/=/)
              t_match = trimRE.match( key )
              key = trimRE.match( key )[1] unless t_match.nil?
              data[key] = value
            }

            tsMatch = tsRE.match( timestamp )
            time = Time.utc( tsMatch[1].to_i, tsMatch[2].to_i, tsMatch[3].to_i, tsMatch[4].to_i, tsMatch[5].to_i, tsMatch[6].to_i, tsMatch[7].to_i )

            ps = PingStat.new
        
            idx = pings.index( data['uid'] )
            if idx then
               ping_visit[idx] = ping_visit[idx] + 1
            else
              pings << data['uid']
              idx = pings.index( data['uid'] )
              ping_visit[idx] = 0
            end

            ps.id = pings.index( data['uid'] )
            ps.visit = ping_visit[idx]
            ps.time = time
            ps.uid = data['uid']
            ps.count = data['count']
            ps.limit = data['limit']
            ps.min_rtt = data['minRTT']
            ps.max_rtt = data['maxRTT']
            ps.mean_rtt = data['meanRTT']
            ps.dev_rtt = data['stddevRTT']
            ps.delta_time = data['deltaTime']
            ps.delta_count = data['deltaCount']
            ps.sum_squares = data['sumSumSqrRTT']

            @ping_db.post_log( runId, ps ) unless ps.visit.to_i < 2
          end
        }
      end

      def perform
        src_cnt = 0
        sink_cnt = 0

        run_id = @ping_db.register_run( File.readlink(CIP), @run.name, @ping_cnt[@run.count], @src_cnt, @snk_cnt)

        `grep -h PingTimerPlugin #{File.join(CIP, 'workspace', 'log4jlogs', '*.log')} > /tmp/pingstats`
        process_pingstats( run_id, File.open('/tmp/pingstats') )
      end
    end
  end
end
