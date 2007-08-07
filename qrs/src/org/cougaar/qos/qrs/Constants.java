/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

/**
 * Holds DataScope constants.
 */
public interface Constants {
    /**
     * The character used to separate fields of a data key.
     */
    public static final String KEY_SEPR = "_";

    public static final String PATH_SEPR = ":";

    // Credibility Spectrum: tries to unify many different notions of
    // credibility into a common metric. The Credibility "Calculus" is
    // still undefined, but here is the general notion for credibility
    // values.
    //
    // The dimensions of credibility include
    // Aggregation: Time period over which the observation were made
    // Staleness: How out-of-date is the data
    // Source: How was the data collected
    // Trust or Collector Authority: Can the collector be trusted
    // Sensitivity: Does a key component of the data have low credibility
    /**
     * No source for data. There was an error or nobody was looking for this
     * data
     */
    public static final double NO_CREDIBILITY = 0.0;
    /**
     * Compile Time Default was the source for data
     */
    public static final double DEFAULT_CREDIBILITY = 0.1;
    /**
     * System Level configuration file was the source for data
     */
    public static final double SYS_DEFAULT_CREDIBILITY = 0.2;
    /**
     * User Level configuration file was the source for data
     */
    public static final double USER_DEFAULT_CREDIBILITY = 0.3;
    /**
     * System Level Base-Line measurements file was the source for data. This
     * data is aggregated over Days
     */
    public static final double SYS_BASE_CREDIBILITY = 0.4;
    /**
     * User Level Base-Line measurements file was the source for data. This data
     * is aggregated over Days
     */
    public static final double USER_BASE_CREDIBILITY = 0.5;
    /**
     * A Single Active measurment was source for data. This data is aggregated
     * over Hours and is not stale
     */
    public static final double HOURLY_MEAS_CREDIBILITY = 0.6;
    /**
     * A Single Active measurment was source for data. This data is aggregated
     * over Minutes and is not stale
     */
    public static final double MINUTE_MEAS_CREDIBILITY = 0.7;
    /**
     * A Single Active measurment was source for data. This data is aggregated
     * over Seconds and is not stale
     */
    public static final double SECOND_MEAS_CREDIBILITY = 0.8;
    /**
     * A Multiple Active measurments were a Consistant source for data. This
     * data is aggregated over Seconds and is not stale
     */
    public static final double CONFIRMED_MEAS_CREDIBILITY = 0.9;
    /**
     * A higher-level system has declared this datarmation to be true. Mainly
     * used by debuggers and gods
     */
    public static final double ORACLE_CREDIBILITY = 1.0;
}
