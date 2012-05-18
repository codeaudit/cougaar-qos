/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

 */

package org.cougaar.qos.qrs;

import java.net.InetAddress;

import org.cougaar.util.log.Logger;

/**
 * A sample ResourceContext which looks for Host capacity data on any feed, by
 * using a Remos-style key with an IntegraterDS. The available formulas are
 * 'CapacityMax' and 'CapacityUnused'.
 */
public class HostDS extends ResourceContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new HostDS(parameters, parent);
            }

            @Override
            public Object identifyParameters(String[] parameters) {
                if (parameters == null || parameters.length != 1) {
                    return null;
                }
                String x = parameters[0];
                String result;
                try {
                    result = InetAddress.getByName(x).getHostAddress();
                } catch (java.net.UnknownHostException ex) {
                    result = x;
                }
                return result.intern();
            }

        };
        registerContextInstantiater("Host", cinst);
    }

    private static final String IPADDRESS = "ipAddress";

    // Host ResourceContexts can be the first element in a path. They have
    // no parent or context other than the root.
    @Override
   protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    @Override
   protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("PrimaryIpAddress")) {
            return new PrimaryIpAddress();
        } else if (kind.equals("PollingLoadAverage")) {
            return new PollingLoadAverage();
        } else if (kind.equals("LoadAverage")) {
            return new LoadAverage();
        } else if (kind.equals("BogoMips")) {
            return new BogoMips();
        } else if (kind.equals("Jips")) {
            return new Jips();
        } else if (kind.equals("Cache")) {
            return new Cache();
        } else if (kind.equals("Count")) {
            return new Count();
        } else if (kind.equals("FreeMemory")) {
            return new FreeMemory();
        } else if (kind.equals("TotalMemory")) {
            return new TotalMemory();
        } else if (kind.equals("MemoryUtilization")) {
            return new MemoryUtilization();
        } else if (kind.equals("TcpInUse")) {
            return new TcpInUse();
        } else if (kind.equals("UdpInUse")) {
            return new UdpInUse();
        } else if (kind.equals("MeanTimeBetweenFailure")) {
            return new MeanTimeBetweenFailure();
        } else if (kind.equals("EffectiveMJips")) {
            return new EffectiveMJips();
        } else if (kind.equals("IsReachable")) {
            return new IsReachable();
        } else if (kind.equals("RawGPS")) {
            return new RawGPS();
        } else if (kind.equals("Position")) {
            return new Position();
        } else if (kind.equals("ClockSpeed")) {
            return new ClockSpeed();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain one string, the address of the host being
     * monitored.
     */
    @Override
   protected void verifyParameters(String[] parameters) throws ParameterError {
        if (parameters == null || parameters.length != 1) {
            throw new ParameterError("HostDS: wrong number of parameters");
        } else {
            // could canonicalize here
            String addr = parameters[0];
            try {
                String ipAddress = InetAddress.getByName(addr).getHostAddress();
                bindSymbolValue(IPADDRESS, ipAddress);
            } catch (java.net.UnknownHostException unknown_host) {
                Logger logger = Logging.getLogger(HostDS.class);
                logger.error("Couldn't resolve hostname " + addr);
                bindSymbolValue(IPADDRESS, addr);
            }

        }

    }

    private DataValue ipAddrValue;

    private HostDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    @Override
   public String toString() {
        String ipAddr = (String) getValue(IPADDRESS);
        return "<HostDS " + ipAddr + ">";
    }

    private synchronized DataValue getIpAddressValue() {
        if (ipAddrValue == null) {
            String addr = (String) getValue(IPADDRESS);
            ipAddrValue = new DataValue(addr, SECOND_MEAS_CREDIBILITY);
        }
        return ipAddrValue;
    }

    public class PrimaryIpAddress extends DataFormula {
        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            // no dependencies, just ask our scope
            return getIpAddressValue();
        }

    }

    abstract static class Formula extends DataFormula {

        abstract String getKey();

        abstract DataValue defaultValue();

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            String ipAddr = (String) context.getValue(IPADDRESS);
            // Canonicalize ipAddr here
            String key = "Host" + KEY_SEPR + ipAddr + KEY_SEPR + getKey();
            String[] parameters = {key};
            ResourceContext dependency = RSS.instance().resolveSpec("Integrater", parameters);
            registerDependency(dependency, "Formula");
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            DataValue defaultValue = defaultValue();
            DataValue computedValue = values.get("Formula");
            return DataValue.mostCredible(computedValue, defaultValue);
        }

    }

    public static class PollingLoadAverage extends SingleKeyPollingIntegral {

        @Override
      protected String getKey() {
            String ipAddr = (String) getContext().getValue(IPADDRESS);
            return "Host" + KEY_SEPR + ipAddr + KEY_SEPR + "CPU" + KEY_SEPR + "loadavg";
            // return "CPU" + KEY_SEPR + "loadavg" ;
        }

        private static final String[] DefaultArgs = {"60000"}; // 1 min

        @Override
      protected boolean hasArgs(String[] args) {
            if (args == null || args.length == 0) {
                return super.hasArgs(DefaultArgs);
            } else {
                return super.hasArgs(args);
            }
        }

        @Override
      protected void setArgs(String[] args) {
            if (args == null || args.length == 0) {
                super.setArgs(DefaultArgs);
            } else {
                super.setArgs(args);
            }
        }

        // Not used by SingleKeyPollingIntegral
        DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class LoadAverage extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "loadavg";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(0.01);
        }
    }

    public static class BogoMips extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "bogomips";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(400);
        }
    }

    public static class Jips extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "Jips";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(10.0E6);
        }
    }

    public static class Cache extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "cache";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class Count extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "count";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(1);
        }
    }

    public static class FreeMemory extends Formula {
        @Override
      String getKey() {
            return "Memory" + KEY_SEPR + "Physical" + KEY_SEPR + "Free";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(64000);
        }
    }

    public static class TotalMemory extends Formula {
        @Override
      String getKey() {
            return "Memory" + KEY_SEPR + "Physical" + KEY_SEPR + "Total";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(128000);
        }
    }

    public static class MemoryUtilization extends Formula {
        @Override
      String getKey() {
            return "Memory" + KEY_SEPR + "Physical" + KEY_SEPR + "Utilization";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(0.0);
        }
    }

    public static class TcpInUse extends Formula {
        @Override
      String getKey() {
            return "Network" + KEY_SEPR + "TCP" + KEY_SEPR + "sockets" + KEY_SEPR + "inuse";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class UdpInUse extends Formula {
        @Override
      String getKey() {
            return "Network" + KEY_SEPR + "UDP" + KEY_SEPR + "sockets" + KEY_SEPR + "inuse";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(0);
        }
    }

    public static class MeanTimeBetweenFailure extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "MeanTimeBetweenFailure";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(8760.0, DEFAULT_CREDIBILITY, "hours", "LinuxLiterature");
        }
    }

    public static class ClockSpeed extends Formula {
        @Override
      String getKey() {
            return "CPU" + KEY_SEPR + "clockspeed";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue(100.0, NO_CREDIBILITY, "MHz", "Random");
        }
    }

    public static class EffectiveMJips extends DataFormula {

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            registerDependency(context, "LoadAverage");
            registerDependency(context, "Jips");
            registerDependency(context, "Count");
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            double lavg = values.doubleValue("LoadAverage");
            double mjips = values.doubleValue("Jips") / 1.0E6;
            double cpus = values.doubleValue("Count");

            double credibility = values.minCredibility();
            if (mjips <= 0) {
                mjips = 400;
                credibility = DEFAULT_CREDIBILITY;
            }
            if (cpus <= 0) {
                cpus = 1;
                credibility = DEFAULT_CREDIBILITY;
            }

            double effectiveMJips =
            // JAZ LoadAverage for Linux is a hard metric to model
                    // If the Object is not greedy, it sneeks in and get
                    // normal latency regardless of the loadaverage.
                    // If the Object is greedy, its latancy is multiplied by
                    // the loadaverage which includes itself.
                    // The number of cpu reduces the load average
                    mjips / // Million Java Inst Per Sec
                            Math.max(1, (lavg / cpus));

            return new DataValue(effectiveMJips, credibility);
        }

    }

    public static class IsReachable extends DataFormula {

        private static final DataValue defaultValue = new DataValue(false, DEFAULT_CREDIBILITY);

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            // The IP address of this Host
            String remoteIpAddr = (String) context.getValue(IPADDRESS);
            // The IP address of the host that RSS is running on
            String localIpAddr = CorbaUtils.hostaddr();
            // Canonicalize ipAddr here
            String key =
                    "Ip" + KEY_SEPR + "Flow" + KEY_SEPR + localIpAddr + KEY_SEPR + remoteIpAddr
                            + KEY_SEPR +
                            // JAZ TOS bits hard wired to 1 for now
                            "1" + KEY_SEPR + "PathCost-Hops";
            String[] parameters = {key};
            ResourceContext hops = RSS.instance().resolveSpec("Integrater", parameters);
            registerDependency(hops, "Formula");
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            DataValue hops = values.get("Formula");
            if (hops.getCredibility() >= DEFAULT_CREDIBILITY) {
                if (hops.getDoubleValue() >= 0.0) {
                    return new DataValue(true,
                                         hops.getCredibility(),
                                         "up/down",
                                         hops.getProvenance());
                } else {
                    return new DataValue(false,
                                         hops.getCredibility(),
                                         "up/down",
                                         hops.getProvenance());
                }
            } else {
                return defaultValue;
            }
        }

    }

    public static class RawGPS extends Formula {
        @Override
      String getKey() {
            return "GPS";
        }

        @Override
      DataValue defaultValue() {
            return new DataValue("none", NO_CREDIBILITY);
        }
    }

    public static class Position extends DataFormula {

        @Override
      protected void initialize(ResourceContext context) {
            super.initialize(context);
            registerDependency(context, "RawGPS");
            registerDependency(context, "IsReachable");
        }

        @Override
      protected DataValue doCalculation(DataFormula.Values values) {
            DataValue rawGPS = values.get("RawGPS");
            DataValue isReachable = values.get("IsReachable");

            if (rawGPS.getCredibility() > NO_CREDIBILITY) {
                // GPS should have a real position
                if (isReachable.getBooleanValue()) {
                    // The position is uptodate
                    return new DataValue(rawGPS);
                } else {
                    // the position is stale
                    return new DataValue(rawGPS.getStringValue(),
                                         HOURLY_MEAS_CREDIBILITY,
                                         rawGPS.getUnits(),
                                         rawGPS.getProvenance());
                }
            } else {
                // no real real position
                return new DataValue("none", NO_CREDIBILITY);
            }

        }
    }
}
