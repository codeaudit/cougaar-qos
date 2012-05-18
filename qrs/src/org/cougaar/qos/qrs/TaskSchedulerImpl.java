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

import java.util.Timer;
import java.util.TimerTask;

import org.cougaar.util.log.Logger;

class TaskSchedulerImpl extends Timer implements TaskScheduler {
    private static class Task extends TimerTask {
        Runnable body;

        Task(Runnable body) {
            this.body = body;
        }

        @Override
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

    @Override
   public void cancel() {
        try {
            throw new RuntimeException();
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(TaskSchedulerImpl.class);
            logger.error("TaskScheduler cancelled", ex);
        }
    }
}
