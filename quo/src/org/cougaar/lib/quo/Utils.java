/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

package org.cougaar.lib.quo;


import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

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
    private static PrintWriter LogFile = null;
    private static char SEPR;
    private static String TAG;
    private static Rusage lastUsage = new Rusage();
    private static long lastThreadTime = System.currentTimeMillis();;
    private static Timer timer = new Timer();

    static {
	TAG = System.getProperty("org.cougaar.lib.quo.tag", "COUGAAR");
	String sepr = System.getProperty("org.cougaar.lib.quo.separator", "");
	if (sepr.equals("") || sepr.equalsIgnoreCase("space") )
	    SEPR = ' ';
	else if (sepr.equalsIgnoreCase("tab"))
	    SEPR = '\t';
	else 
	    SEPR = sepr.charAt(0);

	String logfilename = System.getProperty("org.cougaar.lib.quo.logfile");
	if (logfilename != null && !logfilename.equals("")) {
	    try {
		FileWriter writer = new FileWriter(logfilename);
		LogFile = new PrintWriter(writer);
	    } catch (IOException io_ex) {
		// nowhere to print...
	    }
	}


    }

    // Nobody calls this anymore.  
    public static void StartProcessStatistics () {
	UnixUtils.ensureLib(); // load jni lib
	lastUsage.update();
	TimerTask task = new TimerTask() {
		public void run() {
		    logProcessorUsage();
		    logThreadCount();
		}
	    };
	timer.schedule(task, 0, 1000);
    }

    static synchronized void logMessageWithLength(long startTime, 
						  Message m,
						  int inLength,
						  int outLength) 
    {
	if (LogFile == null) return;
	beginLogMessage(startTime, "compressed_message", m);
	LogFile.print(SEPR);
	LogFile.print(inLength);
	LogFile.print(SEPR);
	LogFile.print(outLength);
	endLogMessage(m);
    }
	

    static synchronized void logMessage(long startTime, Message m) {
	if (LogFile == null) return;
	beginLogMessage(startTime, "message", m);
	endLogMessage(m);
    }

    static synchronized void logMessage(long startTime, 
					long endTime,
					Message m) 
    {
	if (LogFile == null) return;
	beginLogMessage(startTime, endTime, "message", m);
	endLogMessage(m);
    }

    static void beginLogMessage(long startTime, String type, Message m) {
	long now = System.currentTimeMillis();
	beginLogMessage(startTime, now, type, m);
    }

    static void beginLogMessage(long startTime, 
				long endTime,
				String type, 
				Message m) 
    {
	if (LogFile == null) return;

	long endSecs = endTime/1000;
	long endUsecs = endTime-(endSecs*1000);
	long startSecs = startTime/1000;
	long startUsecs = startTime-(startSecs*1000);
	LogFile.print(startSecs);
	LogFile.print('.');
	LogFile.print(startUsecs);
	LogFile.print(SEPR);
	LogFile.print(endSecs);
	LogFile.print('.');
	LogFile.print(endUsecs);
	LogFile.print(SEPR);
	LogFile.print(TAG);
	LogFile.print(SEPR);
	LogFile.print(type);
	LogFile.print(SEPR);
	LogFile.print(m.getOriginator());
	LogFile.print(SEPR);
	LogFile.print(m.getTarget());
    }

    static void endLogMessage(Message m) {
	if (LogFile == null) return;

	if (m instanceof DirectiveMessage) {
	    LogFile.print(SEPR);
	    LogFile.print("DirectiveMessage");
	    LogFile.print(SEPR);
	    DirectiveMessage dm = (DirectiveMessage) m;
	    Directive[] directives = dm.getDirectives();
	    int count = directives.length;
	    LogFile.print(count);
	    for (int i=0; i<count; i++) {
		LogFile.print(SEPR);
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
	LogFile.print(SEPR);
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);

	LogFile.print(SEPR);
	LogFile.print("EVENT");
	LogFile.print(SEPR);

	LogFile.print(m.getOriginator());
	LogFile.print(SEPR);
	LogFile.print(m.getTarget());

	LogFile.print(SEPR);
	LogFile.print(name.replace(SEPR, '_'));

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
	LogFile.print(SEPR);
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);
	LogFile.print(SEPR);
	LogFile.print("Process");
	LogFile.print(SEPR);
	LogFile.print( Process.getProcessID());
	LogFile.print(SEPR);
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



    static synchronized void logThreadCount() {
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
	LogFile.print(SEPR);
	LogFile.print(nowSecs);
	LogFile.print('.');
	LogFile.print(nowUsecs);
	LogFile.print(SEPR);
	LogFile.print("Threads");
	LogFile.print(SEPR);
	LogFile.print(count);
	LogFile.println("");
	LogFile.flush();
	lastThreadTime = now;
    }



}
