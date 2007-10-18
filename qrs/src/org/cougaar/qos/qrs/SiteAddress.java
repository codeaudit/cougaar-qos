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

    private static final long[] Masks = {
        0x00000000l, 0x80000000l, 0xC0000000l, 0xE0000000l,
        0xF0000000l, 0xF8000000l, 0xFC000000l, 0xFE000000l, 
        0xFF000000l, 0xFF800000l, 0xFFC00000l, 0xFFE00000l, 
        0xFFF00000l, 0xFFF80000l, 0xFFFC0000l, 0xFFFE0000l, 
        0xFFFF0000l, 0xFFFF8000l, 0xFFFFC000l, 0xFFFFE000l, 
        0xFFFFF000l, 0xFFFFF800l, 0xFFFFFC00l, 0xFFFFFE00l,
        0xFFFFFF00l, 0xFFFFFF80l, 0xFFFFFFC0l, 0xFFFFFFE0l,
        0xFFFFFFF0l, 0xFFFFFFF8l, 0xFFFFFFFCl, 0xFFFFFFFEl,
        0xFFFFFFFFl
    };

    public long net;
    private final long net_masked;
    public long mask;
    
    public SiteAddress(long address, long mask) {
        this.net = address;
        this.mask = mask;
        net_masked = mask & net;
    }
    
    public SiteAddress(byte[] address, int maskPrefixLength) {
        this.net = bytesToLongAddress(address);
        this.mask = prefixLengthToMask(maskPrefixLength);
        net_masked = mask & net;
    }
    
    public SiteAddress(String maskedAddress) {
        int slash = maskedAddress.indexOf('/');
        String maskbits_string = maskedAddress.substring(1 + slash);
        int maskedbits = Integer.parseInt(maskbits_string);
        String address = maskedAddress.substring(0, slash);
        net = stringToLongAddress(address);
        mask = prefixLengthToMask(maskedbits);
        net_masked = mask & net;
    }
    
    public boolean contains(long addr) {
        return (addr & mask) == net_masked;
    }

    public boolean contains(String addr) {
        return (stringToLongAddress(addr) & mask) == net_masked;
    }

    public String toString() {
        return ((net & 0xff000000) >> 24) + "." + ((net & 0x00ff0000) >> 16) + "."
                + ((net & 0x0000ff00) >> 8) + "." + (net & 0x000000ff) + "/" + maskToPrefixLength(mask);
    }
    
    
    public static long unsignedByteToLong(byte b) {
        if (b < 0) {
            return 256 + b;
        } else {
            return b;
        }
    }

    public static long stringToLongAddress(String address) {
        try {
            return bytesToLongAddress(stringToAddress(address));
        } catch (java.net.UnknownHostException ex) {
            Logger logger = Logging.getLogger(SiteAddress.class);
            logger.error("Bogus net " + address);
            return 0;
        }
    }

    public static long bytesToLongAddress(byte[] net_bytes) {
        return unsignedByteToLong(net_bytes[0]) << 24 | unsignedByteToLong(net_bytes[1]) << 16
        | unsignedByteToLong(net_bytes[2]) << 8 | unsignedByteToLong(net_bytes[3]);
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
    
    public static long prefixLengthToMask(int prefixLength) {
        return Masks[prefixLength];
    }
    

    public static int maskToPrefixLength(long mask) {
        for (int i = 0; i < Masks.length; i++) {
            long l = Masks[i];
            if (mask == l) {
                return i;
            }
        }
        return -1;
    }
    
    public static byte[] stringToAddress(String addressString) throws java.net.UnknownHostException {
        return java.net.InetAddress.getByName(addressString).getAddress();
    }
}
