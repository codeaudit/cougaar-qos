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

/**
 * Used only for internal testing of DataFeeds.
 */
public class FeedTester implements DataFeedListener {
    private static DataValue value;

    public static void main(String[] args) {
        String propertiesFile = null;
        String feedname = null;
        String key = null;
        FeedTester fer = new FeedTester();

        for (int i = 0; i < args.length; i++) {
            if (args.length < 6) {
                System.exit(0);
            }
            String arg = args[i];
            if (arg.equals("-conf")) {
                propertiesFile = args[++i];
            } else if (arg.equals("-feed")) {
                feedname = args[++i];
            } else if (arg.equals("-key")) {
                key = args[++i];
            } else {
                System.err.println("Skipping unknown option " + arg);
            }
        }

        try {
            RSS rss = RSS.makeInstance(propertiesFile);
            DataFeed feed = rss.getFeed(feedname);

            feed.addListenerForKey(fer, key);
            while (true) {
                value = feed.lookup(key);
                System.out.println("lookup value" + value);
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException ex) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void newData(DataFeed store, String key, DataValue data) {
        System.out.println("Subscribing to " + store + " value obtained" + data);
        if (!value.equals(data)) {
            System.out.println("subscription and look up values are different");
        }

    }

}
