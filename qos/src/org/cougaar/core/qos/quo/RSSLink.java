package org.cougaar.core.qos.quo;

import com.bbn.quo.data.BoundDataFormula;
import com.bbn.quo.data.DataFormula;
import com.bbn.quo.data.DataScopeSpec;
import com.bbn.quo.data.DataScope;
import com.bbn.quo.data.DataValue;
import com.bbn.quo.data.HostDS;
import com.bbn.quo.data.RSS;

import org.cougaar.core.mts.RMILinkProtocol;
import org.cougaar.core.society.MessageAddress;
import org.cougaar.core.mts.NameSupport;
import org.cougaar.core.qos.monitor.ResourceMonitorService;

import java.rmi.Remote;
import java.util.Observable;

public class RSSLink implements ResourceMonitorService
{
    private static final String RSS_PROPFILE = "org.cougaar.rss.propfile";
    private RSS rss;
    private NameSupport nameSupport;
    
    public RSSLink(NameSupport nameSupport) {
	String propfile = System.getProperty(RSS_PROPFILE);
	rss = RSS.makeInstance(propfile);
	this.nameSupport = nameSupport;
    }

    private String extractHost (String string) {
	// form is
	// classname[RemoteStub [ref: [endpoint:[host:port](local),objID:[0]]]]
	int type_end = string.indexOf('[');
	if (type_end < 0) return null;
	String type = string.substring(0, type_end);
	
	// Local stubs of remote objects will have _Stub appended to
	// the type, but we don't want that here.
	if (type.endsWith("_Stub")) {
	    type  = type.substring(0, type.length()-5);
	    // System.out.println("//// Removed _stub: " + type);
	}


	int host_start = string.indexOf("[endpoint:[");
	if (host_start < 0) return null;
	host_start += 11;
	int host_end = string.indexOf(':', host_start);
	if (host_end < 0) return null;
	return string.substring(host_start, host_end);
    }    



    private String addressToHost(MessageAddress agentAddress) {
	String ttype = RMILinkProtocol.PROTOCOL_TYPE;
	Object stub = nameSupport.lookupAddressInNameServer(agentAddress, ttype);
	if (stub == null || !(stub instanceof Remote)) return null;
	return extractHost(stub.toString());
    }

    private DataFormula jips(MessageAddress agentAddress) {
	String host = addressToHost(agentAddress);
	if (host == null) return null;
	String[] args = { host };
	DataScopeSpec[] path = new DataScopeSpec[1];
	path[0] = new DataScopeSpec(HostDS.class, args);
	DataScope scope = rss.getDataScope(path);
	return scope.getFormula("EffectiveMJips");
    }

    public double getJipsForAgent(MessageAddress agentAddress) {
	DataFormula jips = jips(agentAddress);
	if (jips != null) {
	    DataValue value = jips.query();
	    System.out.println("===== jps DataValue=" + value);
	    return value.getDoubleValue();
	} else {
	    return 0.0;
	}
    }

    public Observable getJipsForAgentObservable(MessageAddress agentAddress) {
	DataFormula formula = jips(agentAddress);
	if (formula != null) {
	    return new BoundDataFormula(formula);
	} else {
	    return null;
	}
    }
  

}
