package org.cougaar.core.qos.stats;

public enum StatisticKind {
	ANOVA {
		@Override
      public Anova makeStatistic(String name) {
			return new Anova(name);
		}
	},
	
	TRACE {
		@Override
      public Trace makeStatistic(String name) {
			return new Trace(name);
		}
	};
	
	public abstract Statistic makeStatistic(String name);

}
