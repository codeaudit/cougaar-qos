/* =============================================================================
 *
 *                  COPYRIGHT 2007 BBN Technologies Corp.
 *                  10 Moulton St
 *                  Cambridge MA 02138
 *                  (617) 873-8000
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * =============================================================================
 *
 * Created : Aug 14, 2007
 * Workfile: Anova.java
 * $Revision: 1.3 $
 * $Date: 2010/12/20 18:32:31 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

package org.cougaar.core.qos.stats;

public class Anova implements Statistic {
    private String name;
    private int valueCount;
    private double sum;
    private double sumSq;
    private double max; 
    private double min;
    private long startTime ;
    private long endTime ;

    Anova(String name) {
        reset();
        this.name = name;
    }
    
    public void reset() {
        valueCount = 0;
        sum = 0.0;
        sumSq = 0.0;
        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;
        startTime = System.currentTimeMillis();
        endTime = startTime;

    }

    public void newValue(long value) {
    	newValue((double)value);
    }
    
    public void newValue(double value) {
        ++valueCount;
        sum += value;
        sumSq += value * value;
        max = Math.max(max, value);
        min = Math.min(min, value);
        endTime = System.currentTimeMillis();
    }
    
    public Anova delta(Statistic stat) {
        Anova other = (Anova) stat;
        Anova delta = new Anova(null);
        delta.name=name;
        delta.valueCount = valueCount - other.valueCount;
        delta.sum = sum - other.sum;
        delta.sumSq = sumSq - other.sumSq;
        delta.max = Math.max(max, other.max);
        delta.min = Math.min(min, other.min);
        delta.startTime= other.endTime;
        delta.endTime = endTime;
        return delta;
    }
    
    public void accumulate(Statistic stat) {
        Anova other = (Anova) stat;
        valueCount += other.valueCount;
        sum += other.sum;
        sumSq += other.sumSq;
        max = Math.max(max, other.max);
        min = Math.min(min, other.min);
        startTime= Math.min(startTime, other.startTime);
        endTime= Math.max(endTime, other.endTime);
    }
    
    public String getName() {
        return name;
    }
    
    public String getSummaryString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getName());
		buf.append(":");
		buf.append(min());
		buf.append("/");
		buf.append(average());
		buf.append("/");
		buf.append(max());
		return buf.toString();
	}
         
    public String toString() {
    	return "<Anova: " + getSummaryString() + ">";
    }
    
    public Anova clone() throws CloneNotSupportedException {
        return (Anova) super.clone();
    }   
    
    public Anova snapshot() {
    	Anova copy;
		try {
			copy = clone();
		} catch (CloneNotSupportedException e) {
			// This can't happen
			return null;
		}
    	copy.endTime = System.currentTimeMillis();
    	return copy;
    }
    
    public StatisticKind getKind () {
    	return StatisticKind.ANOVA;
    }
    
	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}
    
    /*
     * Anova specific statistical output
     */
    
    public int getValueCount() {
        return valueCount;
    }
    
    public double getSumSq() {
        return sumSq;
    }
    
    public double getSum() {
        return sum;
    }

    
    public double itemPerSec() {
    	long deltaT = endTime - startTime;
        if (deltaT != 0) {
            return (1000.0 * valueCount)/deltaT;
        } else {
            return -1d;
        }
    }
    
    public double average(){
        if (valueCount <= 0) return -1;
        return sum / valueCount;
    }
    
    public double max() {
        return max;
    }
    
    public double min() {
        return min;
    }
    
    public double stdDev() {
        if (valueCount <= 1) return 0;
        double sumSquared = Math.pow(sum, 2);
        double n = valueCount;
        return  Math.sqrt( (n/(n-1)) * (sumSq - sumSquared) );
    }

 }