package org.cougaar.core.qos.stats;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class Trace implements Statistic {
	
	/**
    * 
    */
   private static final long serialVersionUID = 1L;
   private transient final Logger log = Logging.getLogger(getClass().getName());
	private final String name;
	private long startTime;
	private long endTime;
	
	Trace(String name) {
		reset();
		this.name = name;
	}
	
	public void reset() {
		startTime = System.currentTimeMillis();
		endTime= startTime;
	}

	public void newValue(long value) {
		log.shout("new value" +name + " " + value);
	}
	
	public void newValue(double value) {
        log.shout("new value" +name + " " + value);
    }

	public Trace delta(Statistic s) {
		// TODO Auto-generated method stub
		return null;
	}

	public void accumulate(Statistic additionalStatistic) {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getSummaryString() {
		return getName();
	}
	
	@Override
   public String toString(){
		return "<Trace: " + getSummaryString() + ">";
		
	}
	
	public StatisticKind getKind () {
		return StatisticKind.TRACE;
	}
	
	@Override
   public Trace clone() throws CloneNotSupportedException {
        return (Trace) super.clone();
    }

	public Trace snapshot() {
    	Trace copy;
		try {
			copy = clone();
		} catch (CloneNotSupportedException e) {
			// This can't happen
			return null;
		}
    	copy.endTime = System.currentTimeMillis();
    	return copy;
    }

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}
}
