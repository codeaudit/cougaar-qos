/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

class TaskSchedulerImpl extends Timer implements TaskScheduler {
    private static class Task extends TimerTask {
        Runnable body;

        Task(Runnable body) {
            this.body = body;
        }

        public void run() {
            try {
                body.run();
            } catch (Throwable t) {
                Logger logger = Logging.getLogger(TaskSchedulerImpl.class);
                logger.error("Task threw an Exception", t);
            }
        }
    }

    private TimerTask makeTask(Runnable body) {
        return new Task(body);
    }

    public Object schedule(Runnable body, long delay, long period) {
        TimerTask task = makeTask(body);
        try {
            super.schedule(task, delay, period);
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(TaskSchedulerImpl.class);
            logger.error("Unable to schedule task", ex);
            return null;
        }
        return task;
    }

    public Object schedule(Runnable body, long delay) {
        TimerTask task = makeTask(body);
        try {
            super.schedule(task, delay);
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(TaskSchedulerImpl.class);
            logger.error("Unable to schedule task", ex);
            return null;

        }
        return task;
    }

    public void unschedule(Object task) {
        if (task != null) {
            ((TimerTask) task).cancel();
        }
    }

    public void cancel() {
        try {
            throw new RuntimeException();
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(TaskSchedulerImpl.class);
            logger.error("TaskScheduler cancelled", ex);
        }
    }
}
