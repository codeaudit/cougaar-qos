#!/usr/bin/ruby

require 'mysql'
require 'tempfile'

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

module Cougaar; module Actions

class PostMiniPingResults < ::Cougaar::Action
  DOCUMENTATION = Cougaar.document {
    @description = "Scan log files and post MiniPing data to database."
    @parameters = [
      {:hostname => "Hostname of the MySQL database."},
      {:database => "Database name of the MiniPing database."},
      {:username => "Username for the MiniPing database."},
      {:password => "Password for the MiniPing database."},
      {:ping_cnt => "Number of Ping Pairs."}]
  }

  def initialize( run, hostname, database, username, password, ping_cnt )
    super( run )
    @run = run
    @ping_db = PingDatabase.new( hostname, database, username, password )
    @ping_cnt = ping_cnt
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

        puts "#{ps.uid}, #{ps.visit}"
        @ping_db.post_log( runId, ps ) unless ps.visit.to_i < 2
      end
    }
  end

  def perform
    src_cnt = 0
    sink_cnt = 0
    @run.society.each_agent { |agent|
       src_cnt += 1 if agent.name =~ /src/
       sink_cnt += 1 if agent.name =~ /sink/
    }

    run_id = @ping_db.register_run( File.readlink(CIP), @run.name, @ping_cnt, src_cnt, sink_cnt)

    `grep -h PingTimerPlugin #{File.join(CIP, 'workspace', 'log4jlogs', '*.log')} > /tmp/pingstats`
    process_pingstats( run_id, File.open('/tmp/pingstats') )
  end
end
end; end

# pd = PingDatabase.new( 'u111', 'miniping', 'ping', 'p0ng' )
