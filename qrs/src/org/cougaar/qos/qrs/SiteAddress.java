package org.cougaar.qos.qrs;

/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;

public class SiteAddress {
    private static Hashtable sites = new Hashtable();
    public long net;
    private final long net_masked;
    public long mask;

    // Nice clean code

    private static final long[] Masks =
            {0x00000000, 0x80000000, 0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000,
                    0xFE000000, 0xFF000000, 0xFF800000, 0xFFC00000, 0xFFE00000, 0xFFF00000,
                    0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000, 0xFFFF8000, 0xFFFFC000,
                    0xFFFFE000, 0xFFFFF000, 0xFFFFF800, 0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00,
                    0xFFFFFF80, 0xFFFFFFC0, 0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC,
                    0xFFFFFFFE, 0xFFFFFFFF};

    public static long countToMask(int count) {
        return Masks[count];
    }

    public static int maskToCount(long mask) {
        for (int i = 0; i < Masks.length; i++) {
            if (mask == Masks[i]) {
                return i;
            }
        }
        return -1;
    }

    private long unsignedByteToLong(byte b) {
        if (b < 0) {
            return 256 + b;
        } else {
            return b;
        }
    }

    public static byte[] stringToAddress(String addressString) throws java.net.UnknownHostException {
        return java.net.InetAddress.getByName(addressString).getAddress();
    }

    private long stringToLongAddress(String address) {
        try {
            byte[] net_bytes = stringToAddress(address);
            return unsignedByteToLong(net_bytes[0]) << 24 | unsignedByteToLong(net_bytes[1]) << 16
                    | unsignedByteToLong(net_bytes[2]) << 8 | unsignedByteToLong(net_bytes[3]);
        } catch (java.net.UnknownHostException ex) {
            Logger logger = Logging.getLogger(SiteAddress.class);
            logger.error("Bogus net " + address);
            return 0;
        }
    }

    public boolean contains(long addr) {
        return (addr & mask) == net_masked;
    }

    public boolean contains(String addr) {
        return (stringToLongAddress(addr) & mask) == net_masked;
    }

    public static SiteAddress getSiteAddress(String maskedAddress) {
        SiteAddress site = (SiteAddress) sites.get(maskedAddress);
        if (site == null) {
            site = new SiteAddress(maskedAddress);
            sites.put(maskedAddress, site);
        }
        return site;
    }

    public static Enumeration elements() {
        return sites.elements();
    }

    private SiteAddress(String maskedAddress) {
        int slash = maskedAddress.indexOf('/');
        String maskbits_string = maskedAddress.substring(1 + slash);
        int maskedbits = Integer.parseInt(maskbits_string);
        String address = maskedAddress.substring(0, slash);
        net = stringToLongAddress(address);
        mask = countToMask(maskedbits);
        net_masked = mask & net;
    }

    public String toString() {
        return ((net & 0xff000000) >> 24) + "." + ((net & 0x00ff0000) >> 16) + "."
                + ((net & 0x0000ff00) >> 8) + "." + (net & 0x000000ff) + "/" + maskToCount(mask);
    }
}
