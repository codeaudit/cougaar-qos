# first parmeter is col number, which is converted to an index
# col = ARGV.shift.to_i - 1

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

bins =[1,3,
       10,30,
       100,300,
       1000,3000,
       10000,30000,
       100000,30000,
       1000000,300000]

counts = Hash.new
bytes = Hash.new
for i in bins 
  counts[i]=0
  bytes[i]=0
end


begin
  while gets
    f = $_.split
    for i in bins
      if f[msgSize].to_f < i 
	counts[i] += 1
	bytes[i] +=f[msgSize].to_f 
	break
      end
    end
  end
for i in bins
  puts "#{i}, #{counts[i]}, #{bytes[i]} "
end

end
