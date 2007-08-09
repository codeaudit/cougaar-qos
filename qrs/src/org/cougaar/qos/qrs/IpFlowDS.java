/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;
import java.net.InetAddress;

/**
 * A sample ResourceContext which looks for ip-flow capacity data on any feed,
 * by using a Remos-style key with an IntegraterDS. The available formulas are
 * 'CapacityMax' and 'CapacityUnused'.
 */
public class IpFlowDS extends ResourceContext {
    static void register() {
        ContextInstantiater cinst = new AbstractContextInstantiater() {
            public ResourceContext instantiateContext(String[] parameters, ResourceContext parent)
                    throws ResourceContext.ParameterError {
                return new IpFlowDS(parameters, parent);
            }

            public Object identifyParameters(String[] parameters) {
                if (parameters == null || parameters.length != 2) {
                    return null;
                }
                String x = parameters[0];
                String y = parameters[1];
                String result;
                try {
                    String a1 = InetAddress.getByName(x).getHostAddress();
                    String a2 = InetAddress.getByName(y).getHostAddress();
                    result = a1 + "=>" + a2;
                } catch (java.net.UnknownHostException ex) {
                    result = x + "=>" + y;
                }
                return result.intern();
            }

        };
        registerContextInstantiater("IpFlow", cinst);
    }

    private static final String SOURCE = "source";
    private static final String DESTINATION = "destination";

    // IpFlow ResourceContexts can be the first element in a path
    protected ResourceContext preferredParent(RSS root) {
        return root;
    }

    protected DataFormula instantiateFormula(String kind) {
        if (kind.equals("CapacityMax")) {
            return new CapacityMax();
        } else if (kind.equals("CapacityUnused")) {
            return new CapacityUnused();
        } else {
            return null;
        }
    }

    /**
     * The parameters should contain two strings, the source and destination ip
     * of the flow.
     */
    protected void verifyParameters(String[] parameters) throws ParameterError {
        // should be two strings (ip addresses)
        if (parameters == null || parameters.length != 2) {
            throw new ParameterError("IpFlowDS ...");
        }

        String src = parameters[0];
        String dst = parameters[1];

        try {
            src = InetAddress.getByName(src).getHostAddress();
            dst = InetAddress.getByName(dst).getHostAddress();
        } catch (java.net.UnknownHostException ex) {
            Logger logger = Logging.getLogger(IpFlowDS.class);
            logger.error(null, ex);
        }

        bindSymbolValue(SOURCE, src);
        bindSymbolValue(DESTINATION, dst);

        // Clobbering the arglist is not right.
        // parameters = new Object[2];
        // parameters[0] = src;
        // parameters[1] = dst;
    }

    private IpFlowDS(String[] parameters, ResourceContext parent) throws ParameterError {
        super(parameters, parent);
    }

    public String toString() {
        String src = (String) getValue(SOURCE);
        String dst = (String) getValue(DESTINATION);
        return "<IpFlowDS " + src + ", " + dst + ">";
    }

    abstract static class Formula extends DataFormula implements Constants {
        private DataValue[] values;
        private Logger logger;

        abstract String getKey();

        abstract String getSiteFormula();

        protected void initialize(ResourceContext context) {
            super.initialize(context);
            logger = Logging.getLogger(IpFlowDS.class);
            String source = (String) context.getValue(SOURCE);
            String destination = (String) context.getValue(DESTINATION);

            values = new DataValue[3];

            // Make a default IP flow with compile-time dependancy
            if (source.equals(destination)) {
                // 10 Gigbit bus
                values[0] = new DataValue(10000000, HOURLY_MEAS_CREDIBILITY);
            } else {
                // ethernet
                values[0] = new DataValue(10000, DEFAULT_CREDIBILITY);
            }

            // Get IP FLOW from the DataFeeds
            String key =
                    "Ip" + KEY_SEPR + "Flow" + KEY_SEPR + source + KEY_SEPR + destination
                            + KEY_SEPR + getKey();
            String[] parameters = {key};
            ResourceContext dependency = RSS.instance().resolveSpec("Integrater", parameters);
            registerDependency(dependency, "Formula");

            SitesDB sites = RSS.instance().getSitesDB();

            // As Backup Case get the Site Flow for this IP Flow
            SiteAddress from_site = sites.lookup(source);
            SiteAddress to_site = sites.lookup(destination);

            if (from_site == null) {
                logger.error("IP FLOW No from site matching " + source);
                return;
            }

            if (to_site == null) {
                logger.error("IPFLOW No to site matching " + destination);
                return;
            }

            String[] parameters2 = {from_site.toString(), to_site.toString()};
            ResourceContext dependency2 = RSS.instance().resolveSpec("SiteFlow", parameters2);
            registerDependency(dependency2, getSiteFormula());

        }

        protected DataValue doCalculation(DataFormula.Values values) {
            // ipflow should be dominate when credibility is the same

            this.values[2] = values.get("Formula");
            this.values[1] = values.get(getSiteFormula());
            DataValue result = DataValue.maxCredibility(this.values);
            if (logger.isDebugEnabled()) {
                logger.debug("Recalculating " + getKey() + " values=" + this.values[2]
                        + " (ipflow) " + ", " + this.values[1] + " (siteflow) " + ", "
                        + this.values[0] + " (default)");
                logger.debug("Result=" + result);
            }

            return result;
        }

    }

    public static class CapacityMax extends Formula {

        String getSiteFormula() {
            return "CapacityMax";
        }

        String getKey() {
            return "Capacity" + KEY_SEPR + "Max";
        }

    }

    public static class CapacityUnused extends Formula {

        String getSiteFormula() {
            return "CapacityUnused";
        }

        String getKey() {
            return "Capacity" + KEY_SEPR + "Unused";
        }

    }

}