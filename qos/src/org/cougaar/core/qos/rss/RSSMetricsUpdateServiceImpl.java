/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

// Later this will move elsewhere...
package org.cougaar.core.qos.rss;

import java.util.StringTokenizer;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.qos.metrics.Metric;
import org.cougaar.core.qos.metrics.MetricsUpdateService;
import org.cougaar.core.qos.metrics.QosComponent;
import org.cougaar.qos.qrs.sysstat.DirectSysStatSupplier;

/**
 * This Component is an implementation of MetricsUpdateService that uses the
 * RSS. Instantiated by and as child component of the {@link
 * RSSMetricsServiceProvider}.
 * 
 * @property org.cougaar.metrics.probes If provided, this should be a
 *           comma-separated list of host probes, as understood by the QuO
 *           StatusTEC; or 'all'; or 'none'. The list of possible probes is as
 *           follows: Bogomips, Cache, Clock, FreeMemory, TotalMemory,
 *           LoadAverage, TCPInUse, UDPInUse, Jips, and CPUCount. If the
 *           property is not provided, or is provided with the value 'all', all
 *           probes are run. If the property is provided with the value 'none',
 *           the probe task will not be started. Note that some probes are only
 *           available in Linux.
 * 
 * 
 */
public class RSSMetricsUpdateServiceImpl extends QosComponent implements MetricsUpdateService {
    private static final String SYSSTAT_KINDS_PROPERTY = "org.cougaar.metrics.probes";
    private static final int SYSTAT_PERIOD = 15000;

    private TrivialDataFeed dataFeed;
    private MetricInterpreter interpreter;

    public RSSMetricsUpdateServiceImpl() {
    }

    public void load() {
        super.load();

        ServiceBroker sb = getServiceBroker();

        String kinds_string = System.getProperty(SYSSTAT_KINDS_PROPERTY);
        // kinds_string absent or 'all' for all probes
        // kinds_string empty or 'none' for no probes
        // Otherwise it should be a comma-separated list

        String[] kinds = {"Jips", "Memory", "CPU", "LoadAverage", "Sockets", "CPUCount"};;
        // kinds == null for all probes
        // kinds == zero-length array for no probes
        // kinds == true array of strings for specified probes

        if (kinds_string != null) {
            if (kinds_string.equalsIgnoreCase("none")) {
                kinds = new String[0];
            } else if (!kinds_string.equalsIgnoreCase("all")) {
                StringTokenizer tokenizer = new StringTokenizer(kinds_string, ",");
                kinds = new String[tokenizer.countTokens()];
                int i = 0;
                while (tokenizer.hasMoreElements()) {
                    kinds[i] = tokenizer.nextToken();
                }
            }
        }

        dataFeed = new TrivialDataFeed(sb);
        interpreter = new MetricInterpreter();
        if (kinds.length > 0) {
            DirectSysStatSupplier supplier = new DirectSysStatSupplier(kinds, dataFeed);
            supplier.schedule(SYSTAT_PERIOD);
        }

    }

    TrivialDataFeed getMetricsFeed() {
        return dataFeed;
    }

    public void updateValue(String key, Metric value) {
        dataFeed.newData(key, value, interpreter);
    }

}
