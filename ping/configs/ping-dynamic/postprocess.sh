#!/bin/sh

# <copyright>
#  
#  Copyright 2004 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects
#  Agency (DARPA).
# 
#  You can redistribute this software and/or modify it under the
#  terms of the Cougaar Open Source License as published on the
#  Cougaar Open Source Website (www.cougaar.org).
# 
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#  
# </copyright>


##
# Loops through archive and greps for pings, 
# then merges them into sorted file, computes, and writes a single csv file
# Input: archive directory name
# Output: file of ping events

#BASE=/mnt/shared/miniping-09052003/Logs

TEMP=/tmp/$USER-pingstats
rm -rf $TEMP
mkdir $TEMP
cd $TEMP

touch directories
cd $CIP/Logs
ls -d miniALL* > $TEMP/directories
cd $TEMP

dirs=`cat directories`

for d in $dirs; do
 mkdir  $TEMP/$d
 cd  $TEMP/$d
 echo Attempting un-archive of $d/Logs.tgz

# -- unzip and untar logs -- only interested in node logs
tar xvzf $CIP/Logs/$d/Logs.tgz

# -- grep all Node*.log files for Ping events and sort into new file
touch $d-ping_events.txt

grep -h "PingTimerPlugin" *Node*.log | sort > $d-ping_events.txt
echo $d-ping_events.txt created!

# -- compute values and output pingstats.csv file
ruby $CIP/configs/ping-dynamic/computeValues.rb < $d-ping_events.txt
#ruby /mnt/shared/miniping-09052003/configs/ping-dynamic/computeValues.rb < $d-ping_events.txt

done