#!/bin/sh

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
ls -d Mini-Ping* > $TEMP/directories
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

grep -h "PingTimerPlugin" Node*.log | sort > $d-ping_events.txt
echo $d-ping_events.txt created!

# -- compute values and output pingstats.csv file
ruby $CIP/configs/ping-dynamic/computeValues.rb < $d-ping_events.txt
#ruby /mnt/shared/miniping-09052003/configs/ping-dynamic/computeValues.rb < $d-ping_events.txt

done