The files in this directory are property and configuration
files needed to run with the MessageTransport Adapt tools 
for the UltraLog 2001 Assessment.

All files should remain in the /opt/cougaar/quo/data
directory except server.props.  Note that properties which specify
paths assume that the $COUGAAR_INSTALL_PATH is /opt/cougaar and they 
are written for use on a linux machine.  If you are running
on a Windows machine or in a directory other than /opt/cougaar
please change the paths accordingly.

- server.props contains extra properties needed for the
csmart server - you MUST move this file to 
$COUGAAR_INSTALL_PATH/server/bin or add the section of 
properties at the end to your own server.props file.  Note
that a keystore is required to run with SSL - please edit 
the keystore properties to reflect where your keystore is 
located and what the password is.

- adaptdemo.props contains more properties for mts adapt code
and is referenced by the org.cougaar.properties.url property
in the server.props file.

- kernel.conf contains properties needed for the RSS module of
the mts adaptability code and is referenced by the
org.cougaar.rss.properties property in the adaptdemo.props file.

Sites.conf, Hosts.conf and TestIP.conf are all configuration files
used by RSS.  These files are referenced by properties in the
kernel.conf file.  Note that these files can be served up via a web
server - just change the kernel.conf properties.
