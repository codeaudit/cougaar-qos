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

agent = Hash.new
rmi = Hash.new
for i in bins 
  agent[i]=0
  rmi[i]=0
end


begin
  while gets
    f = $_.split
    for i in bins
      if f[agentDelay].to_f < i 
	agent[i] += 1
	break
      end
    end

    for i in bins
      if f[rmiDelay].to_f < i 
	rmi[i] += 1
	break
      end
    end


  end
for i in bins
  puts "#{i}, #{agent[i]}, #{rmi[i]} "
end

end
