# first parmeter is agent to get rate
agent = ARGV.shift.to_s

# fields in trace record
$;=','
date=0
success=1
src=2
dst=3
time=4
agentDelay=5
rmiDelay=6
msgSize=7
headerSize=8

$avgPeriod = 10000 # 10 seconds in msec 

#the current record is beyond the time to output.
# dump bins and then continue to dump bins (with 0 value), 
# until now is within the next bin
def outputBins(lastOut, period, now, bin)
  begin
    lastOut += period
    dumpBins(lastOut, period, bin) 
  end    until lastOut+period > now  # now is within next period
  return lastOut 
 end

def dumpBins(timeStamp, period, bin)
  seconds = period/1000.0
  for agent in bin.keys 
    puts "#{timeStamp} #{agent} #{bin[agent]/seconds}"
    bin[agent] = 0;
  end
end


# Assume sorted in submit time order
begin

  inMsgs=Hash.new(0) #undefined value is 0
  outMsgs=Hash.new(0) 
  lastOutputTime=0
  submitTime=0

  while gets
    f = $_.split
    srcAgent=f[src]
    dstAgent=f[dst]
    submitTime=f[time].to_i
    #puts "#{submitTime} #{srcAgent} #{dstAgent}"
    
    if lastOutputTime == 0
      lastOutputTime = submitTime
    end

    if submitTime > lastOutputTime + $avgPeriod
      lastOutputTime = outputBins(lastOutputTime, $avgPeriod, 
				  submitTime, inMsgs)
    end

    outMsgs[srcAgent] += 1 
    inMsgs[dstAgent] +=1

  end
  dumpBins(submitTime, submitTime-lastOutputTime, inMsgs)
end
