#
#Compute values from all $d-ping_events.txt and output single file
# parameters: filename of events
# output: .csv file containing computed values


$pingCount=0, $deltaCountSum=0, $deltaTimeSum=0, $minRTTVal=99999, $maxRTTVal=0, $avgRTTSum=0, $sumSqrRTTSum=0, $recordsPerEpic=0

# hashes for uids/epics
$h = Hash.new
$checkList = Hash.new
# steady state flag
$state=false
$f = File.open("pingstats.csv", File::CREAT|File::RDWR|File::APPEND)   # pingstats file

def resetEpics()
  # reset statistics
  # make new  checkList
  $pingCount=0, $deltaCountSum=0, $deltaTimeSum=0, $minRTTVal=99999, $maxRTTVal=0, $avgRTTSum=0, $sumSqrRTTSum=0, $recordsPerEpic=0
  $h.each_key {|key| $checkList[key]=false}
end


begin
  while line=readline
    puts line
    
    match =/^([^\s]*)\s.*Ping uid=([^,]*), from=([^,]*), to=([^,]*), count=([^,]*), limit=([^,]*), minRTT=([^,]*), maxRTT=([^,]*), meanRTT=([^,]*), stddevRTT=([^,]*), deltaTime=([^,]*), deltaCount=([^,]*), sumSumSqrRTT=([^,]*),/.match(line)
    
    if match
      timestamp, uid, from, to, count, limit, minRTT, maxRTT, meanRTT, stddevRTT, deltaTime, deltaCount, sumSqrRTT = match[1,13]     
      puts "======raw  sumSqrRTT= #{ sumSqrRTT}"
      # check for steady state - 3 sets worth of uids
      if $state==false 
        if $h.has_key?(uid) && $h.fetch(uid) == 3 
          puts "Got 3 uids: #{uid}, setting state to true"
          $state=true
	  resetEpics()
	elsif $h.has_key?(uid) && $h.fetch(uid) < 3 
	  $h[uid] = $h.fetch(uid)+1
	  puts "adding counter to uid: #{uid}, counter is: #{$h[uid]}"
	elsif !$h.has_key?(uid)
	  $h[uid] = 1
	  puts "first time seeing uid: #{uid}, adding to hash, counter is: #{$h[uid]}"
	end
	puts "Processed send uid: #{uid}, state is: #{$state}" 
      end 
      
      # Now reached steady state - compute values, epic style
      if $state==true
	puts "State is REALLY: #{$state}, computing values" 
	
	# chunk throuh epics, collect data
	# set all uids to false - haven't seen
	
	if $checkList.fetch(uid) == false	  
	  $checkList[uid]=true
	  $recordsPerEpic+=1
	  
	  # we want to record here
	  if minRTT.to_f < $minRTTVal
	    $minRTTVal = minRTT.to_f
	  end
	  if maxRTT.to_f > $maxRTTVal 
	    $maxRTTVal = maxRTT.to_f
	  end
	  $avgRTTSum+=meanRTT.to_f
	  $sumSqrRTTSum+=sumSqrRTT.to_f
	  $deltaCountSum+=deltaCount.to_f
	  $deltaTimeSum+=deltaTime.to_f
	else
	  # wierd order ignore this record
	  puts "dropped record for #{line}"
	end
	
	# check if end of epic, all checklist is true
	if  ! $checkList.has_value?(false)
	  # print stats - 1 ln per epic to file
	  # Process values, output to file
	  puts "!!!!!!----deltaCountSum is: #{$deltaCountSum}"
	  throughPut = ($deltaCountSum/($deltaTimeSum/$recordsPerEpic))*1000
	  avgRTTVal = $avgRTTSum/$recordsPerEpic
	  
	  if $deltaCountSum <= 0 
	    stddevRTTVal = 0
	  else
	    avgX2=  $sumSqrRTTSum 
	    avgXsquared= (avgRTTVal*avgRTTVal) * $deltaCountSum
	    stddevRTTVal = Math.sqrt((avgX2-avgXsquared) /$deltaCountSum)
          end
	  
	  puts "!-----computing stddevRTTVal, sumSqrRTTSum is: #{$sumSqrRTTSum}, avgRTTSum is: #{$avgRTTSum}"
          
	  #append to file	  
	  $f.print("#{timestamp}, #{$recordsPerEpic}, #{throughPut}, #{$minRTTVal}, #{$maxRTTVal}, #{avgRTTVal}, #{stddevRTTVal} \n")
	  $f.flush
	  puts "------Wrote time-stamped pingstats.csv - format is: timestamp, throughPut, minRTT, maxRTT, avgRTT, stddevRTT"
	  
	  # reset epic
	  resetEpics()
	end
      end
    end
  end    
  
rescue EOFError
end

