package com.dianping.cat.system.page.abtest.handler;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.page.abtest.Action;
import com.dianping.cat.system.page.abtest.Context;
import com.dianping.cat.system.page.abtest.Model;
import com.dianping.cat.system.page.abtest.Payload;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvice;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvisor;

public class AdvisorHandler implements SubHandler {
	
	public static final String ID = "advisor_handler";
	
	@Inject
	private ABTestAdvisor m_advisor;

	@Override
	public void handleInbound(Context ctx, Payload payload) {
		Action action = payload.getAction();

		if (action == Action.ABTEST_CACULATOR) {
			handleCaculatorAction(ctx, payload);
		}
	}

	private void handleCaculatorAction(Context ctx, Payload payload) {
		float actualCtr = payload.getConversionRate() / 100.00f;

		m_advisor.setCurrentPv(payload.getPv());
		List<ABTestAdvice> advices = m_advisor.offer(actualCtr, actualCtr + 0.10f);

		ctx.setAdvice(advices);
	}

	@Override
   public void handleOutbound(Context ctx, Model model, Payload payload) {
		throw new UnsupportedOperationException();
   }
}
