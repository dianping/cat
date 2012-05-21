package com.dianping.cat.job.spi.joblet;

public abstract class AbstractJobletContext implements JobletContext {
	@Override
	public boolean isInCombiner() {
		return false;
	}
}
