package com.dianping.cat.system.page.abtest.advisor;

import java.util.ArrayList;
import java.util.List;

public class DefaultABTestAdvisor extends AbstractABTestAdvisor implements ABTestAdvisor {

	@Override
	public List<ABTestAdvice> offer(double actualCTR, double expectedCTR) {
		List<ABTestAdvice> advices = new ArrayList<ABTestAdvice>();

		if (actualCTR >= expectedCTR) {
			return advices;
		}

		double increaseCtr = actualCTR;
		do {
			ABTestAdvice advice = new ABTestAdvice();

			advice.setConfidenceInterval(m_confidenceInterval);
			advice.setDifference(m_difference);
			advice.setCtrOfVariationA(actualCTR);
			increaseCtr += m_difference;
			advice.setCtrOfVariationB(increaseCtr);

			int sizePerGroup = getSampleSize(actualCTR, increaseCtr);

			advice.setSizePerGroup(sizePerGroup);
			advice.setTotalParticipants(sizePerGroup * 2);

			if (m_pv != 0) {
				int days = (sizePerGroup * 2) % m_pv == 0 ? (sizePerGroup * 2) / m_pv : (sizePerGroup * 2) / m_pv + 1;

				advice.setDays(days);
			} 

			advices.add(advice);
		} while (increaseCtr < expectedCTR);

		return advices;
	}

	private int getSampleSize(double crActual, double crExpected) {
		double zscore = 1.65f;
		double diff = crActual - crExpected;

		double result = zscore * zscore * (crActual + crExpected - crActual * crActual - crExpected * crExpected)
		      / (diff * diff);

		return (int) result + 1;
	}

}
