/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import org.omg.CORBA.ORB;


/**
 * Used only for internal testing of DataFeeds. */
public class FeedTester implements DataFeedListener
{
  private static DataValue value;
    public static void main(String[] args) {
	String propertiesFile = null;
	String feedname = null;
	String key = null;
	FeedTester fer = new FeedTester();
	

	for (int i=0; i<args.length; i++) {
	    if (args.length < 6){
		System.exit(0);
	    }
	    String arg = args[i];
	    if (arg.equals("-conf"))
		propertiesFile = args[++i];
	    else if (arg.equals("-feed"))
		feedname = args[++i];
	    else if (arg.equals("-key"))
	      key = args[++i];
	    else
		System.err.println("Skipping unknown option " + arg);
	}

	
	try {
	  RSS rss= RSS.makeInstance(propertiesFile);
	  DataFeed  feed =  rss.getFeed(feedname);
	
	  feed.addListenerForKey(fer, key);
	  while (true) { 
	    value = feed.lookup(key);
	    System.out.println("lookup value" + value);
	    try { Thread.sleep(1200); } catch (InterruptedException ex) {}
	  }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

  public  void newData(DataFeed store, 
		       String key,
		       DataValue data) {
    System.out.println("Subscribing to " + store + " value obtained" + data);
    if (!value.equals(data))
      System.out.println("subscription and look up values are different");
    
  }

}








