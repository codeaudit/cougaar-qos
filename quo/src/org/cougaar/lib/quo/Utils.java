/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;


import java.io.*;

import org.cougaar.core.util.UID;
import org.cougaar.core.mts.Message;
import org.cougaar.core.blackboard.DirectiveMessage;
import org.cougaar.planning.ldm.plan.Directive;
import org.cougaar.planning.ldm.plan.Notification;

import unix.Rusage;
import unix.Process;
import unix.UnixUtils;

class Utils 
{
    static private PrintWriter LogFile = null;
    static Rusage lastUsage = new Rusage();
    static long lastThreadTime = System.currentTimeMillis();;

    static {
	String logfilename = System.getProperty("org.cougaar.lib.quo.logfile");
	if (logfilename != null && !logfilename.equals("")) {
	    try {
		FileWriter writer = new FileWriter(logfilename);
		LogFile = new PrintWriter(writer);
	    } catch (IOException io_ex) {
		System.err.println("Error opening " + logfilename +
				   ": " + io_ex);
	    }
	}


    }

    static public void StartProcessStatistics (){

	UnixUtils.ensureLib(); // load jni lib
	lastUsage.update();
	Thread updater = new Thread () {
		public void run() {
		    while (true) {
			logProcessorUsage();
			logThreadCount();
			try { sleep(1000); } catch (InterruptedException ex){}
		    }
		}
	    };
	updater.start();
    }

    static synchronized void logMessageWithLength(long startTime, 
						  Message m,
						  int inLength,
						  int outLength) 
    {
      if (LogFile == null) return;
	beginLogMessage(startTime, "COUGAAR compressed_message", m);
	LogFile.print(' ');
	LogFile.print(inLength);
	LogFile.print(' ');
	LogFile.print(outLength);
	endLogMessage(m);
    }
	

    static synchronized void logMessage(long startTime, Message m) {
      if (LogFile == null) return;
	beginLogMessage(startTime, "COUGAAR message", m);
	endLogMessage(m);
    }

    static void beginLogMessage(long startTime, String type, Message m) {
      if (LogFile == null) return;

	long now = System.currentTimeMillis();
	long nowSecs = now/1000;
	long nowUsecs = now-(nowSecs*1000);
	long startSecs = startTime/1000;
	long startUsecs = startTime-(startSecs*1000);
	LogFile.print(startSecs);
	LogFile.print('.');
	LogFile.print(startUsecs);
	LogFile.print(' ');
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);
	LogFile.print(' ');
	LogFile.print(type);
	LogFile.print(' ');
	LogFile.print(m.getOriginator());
	LogFile.print(' ');
	LogFile.print(m.getTarget());
    }

    static void endLogMessage(Message m) {
      if (LogFile == null) return;

	if (m instanceof DirectiveMessage) {
	    LogFile.print(" DirectiveMessage ");
	    DirectiveMessage dm = (DirectiveMessage) m;
	    Directive[] directives = dm.getDirectives();
	    int count = directives.length;
	    LogFile.print(count);
	    for (int i=0; i<count; i++) {
		LogFile.print(' ');
		LogFile.print(directives[i].getClass().getName());
	    }
	}
	LogFile.println("");
	LogFile.flush();
    }


    static synchronized void logEvent(long startTime, Message m, String name) 
    {
      if (LogFile == null) return;

	long now = System.currentTimeMillis();
	long nowSecs = now/1000;
	long nowUsecs = now-(nowSecs*1000);
	long startSecs = startTime/1000;
	long startUsecs = startTime-(startSecs*1000);
	LogFile.print(startSecs);
	LogFile.print('.');
	LogFile.print(startUsecs);
	LogFile.print(' ');
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);

	LogFile.print(" EVENT ");

	LogFile.print(m.getOriginator());
	LogFile.print(' ');
	LogFile.print(m.getTarget());

	LogFile.print(' ');
	LogFile.print(name.replace(' ', '_'));

	LogFile.println("");
	LogFile.flush();

    }
   
    static synchronized void logProcessorUsage() 
    {
      if (LogFile == null) return;

	Rusage currentUsage = new Rusage();
	currentUsage.update();
	long nowSecs = currentUsage.lastUpdated/1000;
	long nowUsecs = currentUsage.lastUpdated-(nowSecs*1000);
	long startSecs = lastUsage.lastUpdated/1000;
	long startUsecs = lastUsage.lastUpdated-(startSecs*1000);

	LogFile.print(startSecs);
	LogFile.print('.');
	LogFile.print(startUsecs);
	LogFile.print(' ');
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);
	LogFile.print(" Process ");
	LogFile.print( Process.getProcessID());
	LogFile.print(' ');
	double deltaUserMsecs = 
	    ((currentUsage.user_secs - lastUsage.user_secs) * 1000
	     + (currentUsage.user_usecs - lastUsage.user_usecs) /  1000);
	double delta_t = (currentUsage.lastUpdated -lastUsage.lastUpdated);
	double userPercent = 100 * (deltaUserMsecs/delta_t);
	
	// Only print the int portion of the percentage
	LogFile.print((int) userPercent);
	LogFile.println("");
	LogFile.flush();
	lastUsage=currentUsage;
    }



    private static int countActiveThreads(ThreadGroup group) {
	if (group == null) return 0;

	int count = group.activeCount();

	int num_groups = group.activeGroupCount();
	ThreadGroup[] groups = new ThreadGroup[num_groups];
	group.enumerate(groups, false);
	for(int i = 0; i < num_groups; i++)
	    count += countActiveThreads(groups[i]);

	return count;
    }
  
    private static int countActiveThreads() {
	ThreadGroup root = Thread.currentThread().getThreadGroup();
	ThreadGroup parent = root.getParent();
	while(parent != null) {
	    root = parent;
	    parent = parent.getParent();
	}
    
	return countActiveThreads(root);

    }



    static synchronized void logThreadCount() 
    {
      if (LogFile == null) return;

	long now = System.currentTimeMillis();
	long nowSecs = now/1000;
	long nowUsecs = now-(nowSecs*1000);
	long lastSecs = lastThreadTime/1000;
	long lastUsecs = lastThreadTime-(lastSecs*1000);
	int count = countActiveThreads();

	LogFile.print(lastSecs);
	LogFile.print('.');
	LogFile.print(lastUsecs);
	LogFile.print(' ');
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);
	LogFile.print(" Threads ");
	LogFile.print(count);
	LogFile.println("");
	LogFile.flush();
	lastThreadTime = now;
    }



}
