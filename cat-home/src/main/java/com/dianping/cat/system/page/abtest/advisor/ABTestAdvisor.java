package com.dianping.cat.system.page.abtest.advisor;

import java.util.List;

public interface ABTestAdvisor {

	public List<ABTestAdvice> offer(double actualCTR, double expectedCTR);

	public void setConfidenceInterval(double interval);

	public void setCurrentPv(int pv);

	public void setDifference(double difference);
}
