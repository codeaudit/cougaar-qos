/* =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.sysstat;

import org.cougaar.qos.qrs.TimerQueueingDataFeed;

public class SysStatDataFeed extends TimerQueueingDataFeed {
    private final DirectSysStatSupplier supplier;

    public SysStatDataFeed(String[] args) {
        int i = 0;
        int interval = 0;
        String[] kinds = null;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.equals("-interval")) {
                String interval_string = args[i++];
                interval = Integer.parseInt(interval_string);
            } else if (arg.equals("-kinds")) {
                int remaining = args.length - i;
                kinds = new String[remaining];
                for (int j = 0; j < remaining; j++) {
                    kinds[j] = args[i++];
                }
                break;
            }
        }
        supplier = new DirectSysStatSupplier(kinds, this);
        supplier.schedule(interval);
    }

    public DirectSysStatSupplier getSupplier() {
        return supplier;
    }

}
