#!/bin/sh
CIP=`pwd`
export CIP
COUGAAR_INSTALL_PATH=`pwd`
export COUGAAR_INSTALL_PATH
ping/bin/jips.sh
cd ping/configs/ping
../../bin/Node-min MiniPing-minimum.xml NodeA
