# first parmeter is col number, which is converted to an index
# col = ARGV.shift.to_i - 1


date=0
success=1
src=2
dst=3
time=4
agentDelay=5
rmiDelay=6
msgSize=7
headerSize=8

begin
  while gets
    $;=','
    $F = $_.split
    puts "#{$F[time]} #{$F[src]} #{$F[dst]} #{$F[agentDelay]} #{$F[rmiDelay]} #{$F[msgSize]} #{$F[headerSize].strip}"
  end
  #Done reading the file

end
