#!/usr/bin/ruby
#
# parse network.xml into QuO RSS config file
#
# see method:
#  read_xml_metrics(networkDef)

CIP = ENV['CIP']

$:.unshift File.join(CIP, 'csmart', 'acme_service', 'src')

require 'plugins/acme_net_shape/vlan.rb'

# Setup Ultralog load path
require 'cougaar/scripting'
require 'ultralog/scripting'
# require code to read network.xml file

def toSite(vlan)
  a,b,c,d=vlan.router.chomp.split(/\s*\.\s*/)
  if vlan.netmask == "255.255.255.0"
    network="#{a}.#{b}.#{c}.0"
    return "#{network}/24"
  elsif vlan.netmask == "255.255.0.0"
    network="#{a}.#{b}.0.0"
    return "#{network}/16"
  elsif vlan.netmask == "255.255.255.240"
    lower = d.to_i - 1
    network="#{a}.#{b}.#{c}.#{lower}"
    return "#{network}/28"

  end
end

# where is min?
def min (a, b)
  if a.to_f <= b.to_f
    return a
  else
    return b
  end
end

#
# hacked to shape bandwidth newBandwidth is the measure for all vlan links 
#
def read_xml_metrics(networkDef, newBandwidth)
  # Read in network.xml file
  network=VlanSupport::Network.new(networkDef)

  # Bandwidth shape flag
  if newBandwidth != 0.0
    bandwidth_shape = true
  else 
    bandwidth_shape = false
  end
	
  # Expand implicit links where the router does not traffic shape
  # These links are limited by the intra-vlan bandwidth
  # so the link should be the min of the src and dst
  network.vlans.each { |srcVlan|
    # add intra-site bandwidth
      srcVlan.add_link(srcVlan,srcVlan.bandwidth)
    # add links for ones that are not already there 
    network.vlans.each {|dstVlan|
      found = false
      srcVlan.links.each { |link|
        if link.to == dstVlan 
	  found = true
	  #set bandwidth      
          if srcVlan != dstVlan  && bandwidth_shape
            link.bandwidth = newBandwidth
          end        
        end
      }
      if ! found 
        if bandwidth_shape
          srcVlan.add_link(dstVlan,min(srcVlan.bandwidth,newBandwidth))
        else
          srcVlan.add_link(dstVlan,min(srcVlan.bandwidth,dstVlan.bandwidth))
        end        
      end
    }
  }
  
  # create array of metrics to send to the node
  metrics = [];
  network.vlans.each { |vlan|
    vlan.links.each { |link|
	
    # convert vlan to <network>/<mask> format
    srcSite = toSite(vlan)
    dstSite = toSite(link.to)
    metric = "?key=Site_Flow_#{srcSite}_#{dstSite}_Capacity_Max&value=#{link.bandwidth.to_f*1000}";
    metrics.push(metric);
    }
  }

  return metrics;
end
