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

package org.cougaar.qos.qrs.sysstat;

import java.util.Arrays;
import java.util.Map;

import org.cougaar.qos.qrs.DataValue;

/**
 * 
 * This is a JVM+Host microbenchmark facility designed to determine the CAPACITY
 * of a specific Java Virtual machine running on a specific host. We have found
 * that the version of JVM makes a big difference in the application
 * performance, orders of magnitudes, as well as the MIPS of the host. This
 * benchmark tries to unify the hosts BogoMIPS measures and JVM version.
 * Obviously to be valid the benchmark must be run under the same JAVA VM as the
 * application.
 * 
 * The goal is to measure JVM+host Capacity. We need to remove all the possible
 * sources of latency variation, such as clock granularity, OS scheduling,
 * memory swapping, and garbage collection. Given Java's clock granularity of 1
 * msec, we should run the test for at least 100ms to get 1% accuracy.
 * Unfortunately 100ms is longer than the OS scheduling increment, such as a
 * Linux Jiffy which is 10ms. NOTE RUNNING FOR A LONGER TIME DOES NOT HELP,
 * because OS scheduling other tasks, and garbage collection will then be
 * "averaged" into the measurements.
 * 
 * The scheme is:
 * 
 * 1) Estimate the number of benchmark iterations that will run a test for
 * around 100ms.
 * 
 * 2) Run COUNT tests with a SLEEP of msec between the tests. The sleep is to
 * help the benchmark process from being marked as greedy by the OS. Hence
 * sleeping reduces the probability of being preempted during the test by some
 * other job.
 * 
 * 3) Pick the fastest time as best estimator for the maximum Capacity. Used to
 * pick the second fastest because the fastest might be a bad measurement (the
 * best seems fine)
 * 
 * 4) Return JIPS (Java Instructions Per Second) Jips = (benchmarkIterations /
 * BestTime) * INST_PER_ITER
 * 
 * We have run benchmark on hosts with a load average of 10 and have got the
 * same Jips as when run with a unloaded host. This work despite an under
 * estimate of the number of iterations for 100ms.
 * 
 * Jips comes with with 3 built in benchmarks, You are encouraged to experiment
 * with others: 0 measures the overhead of the loop. 1 measures time to add two
 * integers (Default) 2 measures time to add two floats
 * 
 * Problems:
 * 
 * What if the application excapes to C code, such as array copy, sockets, or
 * compression?
 * 
 * 
 */

public class Jips extends SysStatHandler implements Runnable

{
    public static final double PERIOD = 0.1;
    public static final int COUNT = 5;
    public static final int TYPE = 3;
    public static final int SLEEP = 100;
    public static final int MEM_SIZE = 1000000;
    public static final int MEM_SIZE_MINUS_1 = MEM_SIZE - 1;

    // estimate the number of "instructions per iteration"
    public static final int[] INST_PER_ITER = {19, 20, 20, 91, 148};
    private double fieldD = 0.0;
    private int fieldI = 0;
    private int[] fieldM = null;
    private final int type;
    private String key;
    private DataValue theValue;

    private double iterationsPerSecond;

    public Jips(int type) {
        this.type = type;
    }

    public Jips() {
        type = TYPE;
    }

    @Override
   public void initialize(String host, int pid) {
        if (type >= 3) {
            fieldM = new int[MEM_SIZE];
        }
        // cache value here
        iterationsPerSecond = runTests(PERIOD, COUNT, SLEEP);
        fieldM = null; // let memory be garbage collected
        key = "Host" + KEY_SEPR + host + KEY_SEPR + "CPU" + KEY_SEPR + "Jips";
        theValue = new DataValue(iterationsPerSecond, HOURLY_MEAS_CREDIBILITY, "", PROVENANCE);
    }

    // backward compatibility
    DataValue getData() {
        return theValue;
    }

    @Override
   public void getData(Map<String, DataValue> map) {
        map.put(key, theValue);
    }

    public void printResults(double[] results) {
        for (int i = 0; i < results.length; i++) {
            System.out.println(i + " " + results[i]);
        }
    }

    public void run() {
        switch (type) {
            case 0:
                break;
            case 1:
                incI(1);
                break;
            case 2:
                incD(1.0);
                break;
            case 3:
                incM(1);
                break;
            // Need better Cougaar benchmark, memory good enough for now.
            case 4:
                incM(1);
                incI(1);
                incI(1);
                incI(1);
                break;
        }
    }

    // Base Benchmarks
    // Interger overflow will happen when processors are about 10Ghz
    // (i.e. in the year 2006)
    // Floating Point Add
    public void incD(double value) {
        fieldD = fieldD + value;
    }

    // Interger Add
    public void incI(int value) {
        fieldI = fieldI + value;
    }

    // Memory Add
    public void incM(int value) {
        // sweep memory from both sides
        fieldI = fieldI + value;
        int dst = fieldI % MEM_SIZE;
        int src = MEM_SIZE_MINUS_1 - dst;
        fieldM[dst] = fieldM[src] + fieldI;
    }

    // time Benchmark over n iterations
    public double time(int n) {
        fieldI = 0;
        fieldD = 0.0;
        // fieldM just keeps old values
        long start = System.currentTimeMillis();
        while (n-- > 0) {
            run();
        }
        // return time in Seconds
        return (System.currentTimeMillis() - start) / 1000.0;
    }

    // estimate the number of iterations to get the target period
    public int estimateN(int n, double target) {
        // exponentially increase n until time is above target
        double time;
        while ((time = Math.min(time(n), time(n))) < target) {
            n = n * 2;
            // sleep between tests
            snooze(SLEEP + SLEEP);
        }
        return (int) (target / (time / n));
    }

    // Sequence a number of tests and pick the second best time
    public double runTests(double target, int count, int sleepMsec) {
        // How many iterations to get about targetPeriod
        int n = estimateN(1, target);
        // Test results array
        double[] results = new double[count];
        for (int i = 0; i < count; i++) {
            // run test
            results[i] = time(n);
            // sleep between tests
            snooze(sleepMsec);
        }
        // sort results
        Arrays.sort(results);
        // Debug Dump array
        // printResults(results);
        // return the second fastest time
        // Seconds per iteration
        return n / results[0] * INST_PER_ITER[type];

    }

    private void snooze(int msec) {
        // sleep between tests
        try {
            if (msec > 0) {
                Thread.sleep(msec);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Jips benchmark = null;

        benchmark = new Jips(0);
        benchmark.initialize("localhost", 0);
        System.out.println("Loop MJIPS = " + benchmark.getData().getDoubleValue() / 1000000.0);

        benchmark = new Jips(1);
        benchmark.initialize("localhost", 0);
        System.out.println("Int MJIPS = " + benchmark.getData().getDoubleValue() / 1000000.0);

        benchmark = new Jips(2);
        benchmark.initialize("localhost", 0);
        System.out.println("Double MJIPS = " + benchmark.getData().getDoubleValue() / 1000000.0);

        benchmark = new Jips(3);
        benchmark.initialize("localhost", 0);
        System.out.println("Memory MJIPS = " + benchmark.getData().getDoubleValue() / 1000000.0);

        benchmark = new Jips(4);
        benchmark.initialize("localhost", 0);
        System.out.println("Cougaar MJIPS = " + benchmark.getData().getDoubleValue() / 1000000.0);

    }

}
