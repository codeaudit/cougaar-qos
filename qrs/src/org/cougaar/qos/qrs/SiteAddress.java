package org.cougaar.qos.qrs;

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

import java.util.HashMap;
import java.util.Map;

import org.cougaar.util.log.Logger;

// Nice clean code
public class SiteAddress {
    private static Map<String, SiteAddress> sites = new HashMap<String, SiteAddress>();

    private static final long[] Masks =
            {0x00000000, 0x80000000, 0xC0000000, 0xE0000000, 0xF0000000, 0xF8000000, 0xFC000000,
                    0xFE000000, 0xFF000000, 0xFF800000, 0xFFC00000, 0xFFE00000, 0xFFF00000,
                    0xFFF80000, 0xFFFC0000, 0xFFFE0000, 0xFFFF0000, 0xFFFF8000, 0xFFFFC000,
                    0xFFFFE000, 0xFFFFF000, 0xFFFFF800, 0xFFFFFC00, 0xFFFFFE00, 0xFFFFFF00,
                    0xFFFFFF80, 0xFFFFFFC0, 0xFFFFFFE0, 0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC,
                    0xFFFFFFFE, 0xFFFFFFFF};

    public long net;
    private final long net_masked;
    public long mask;
    
    private SiteAddress(String maskedAddress) {
        int slash = maskedAddress.indexOf('/');
        String maskbits_string = maskedAddress.substring(1 + slash);
        int maskedbits = Integer.parseInt(maskbits_string);
        String address = maskedAddress.substring(0, slash);
        net = stringToLongAddress(address);
        mask = countToMask(maskedbits);
        net_masked = mask & net;
    }
    
    private long unsignedByteToLong(byte b) {
        if (b < 0) {
            return 256 + b;
        } else {
            return b;
        }
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

    public String toString() {
        return ((net & 0xff000000) >> 24) + "." + ((net & 0x00ff0000) >> 16) + "."
                + ((net & 0x0000ff00) >> 8) + "." + (net & 0x000000ff) + "/" + maskToCount(mask);
    }
    
    public static Iterable<SiteAddress> elements() {
        return sites.values();
    }
    
    public static SiteAddress getSiteAddress(String maskedAddress) {
        SiteAddress site = sites.get(maskedAddress);
        if (site == null) {
            site = new SiteAddress(maskedAddress);
            sites.put(maskedAddress, site);
        }
        return site;
    }
    
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
    
    public static byte[] stringToAddress(String addressString) throws java.net.UnknownHostException {
        return java.net.InetAddress.getByName(addressString).getAddress();
    }
}
