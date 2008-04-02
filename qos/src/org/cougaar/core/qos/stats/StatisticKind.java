package org.cougaar.core.qos.stats;

public enum StatisticKind {
	ANOVA {
		public Anova makeStatistic(String name) {
			return new Anova(name);
		}
	},
	
	TRACE {
		public Trace makeStatistic(String name) {
			return new Trace(name);
		}
	};
	
	public abstract Statistic makeStatistic(String name);

}
