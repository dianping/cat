package com.dianping.cat.system.page.abtest.handler;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.system.page.abtest.Action;
import com.dianping.cat.system.page.abtest.Context;
import com.dianping.cat.system.page.abtest.Model;
import com.dianping.cat.system.page.abtest.Payload;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.util.AbtestStatus;

public class ModelHandler implements SubHandler {
	
	public static final String ID = "model_handler";

	@Inject
	private ABTestService m_service;

	@Override
	public void handleOutbound(Context ctx, Model model, Payload payload) {

		Action action = payload.getAction();

		if (action == Action.MODEL) {
			renderModel(model, payload);
		}
	}

	private void renderModel(Model model, Payload payload) {
		long lastUpdateTime = payload.getLastUpdateTime();
		AbtestModel filteredModel = new AbtestModel();

		if (lastUpdateTime < m_service.getModifiedTime()) {
			AbtestModel abtestModel = m_service.getABTestModelByStatus(AbtestStatus.READY, AbtestStatus.RUNNING);

			for (Case abtestCase : abtestModel.getCases()) {
				Case newCase = new Case();

				for (Run run : abtestCase.getRuns()) {
					if (run.getLastModifiedDate().getTime() > lastUpdateTime) {
						newCase.addRun(run);
					}
				}

				if (newCase.getRuns().size() > 0) {
					newCase.setId(abtestCase.getId());
					newCase.setGroupStrategy(abtestCase.getGroupStrategy());
					newCase.setOwner(abtestCase.getOwner());
					newCase.setDescription(abtestCase.getDescription());
					newCase.getDomains().addAll(abtestCase.getDomains());
					newCase.mergeAttributes(abtestCase);

					filteredModel.addCase(newCase);
				}
			}
		}

		model.setAbtestModel(filteredModel);
	}

	@Override
   public void handleInbound(Context ctx, Payload payload) {
		throw new UnsupportedOperationException();
   }
}
