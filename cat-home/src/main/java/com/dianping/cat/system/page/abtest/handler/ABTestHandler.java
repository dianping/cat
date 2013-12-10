package com.dianping.cat.system.page.abtest.handler;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.Condition;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.system.page.abtest.Action;
import com.dianping.cat.system.page.abtest.Context;
import com.dianping.cat.system.page.abtest.Handler;
import com.dianping.cat.system.page.abtest.Model;
import com.dianping.cat.system.page.abtest.Payload;
import com.dianping.cat.system.page.abtest.ResponseJson;
import com.dianping.cat.system.page.abtest.conditions.ScriptProvider;
import com.dianping.cat.system.page.abtest.conditions.URLScriptProvider;
import com.dianping.cat.system.page.abtest.handler.ListViewModel.AbtestItem;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.util.GsonManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ABTestHandler implements SubHandler, Initializable {

	public static final String ID = "abtest_handler";

	@Inject
	private ABTestService m_service;

	@Inject
	private GsonManager m_gsonBuilderManager;

	private URLScriptProvider m_urlScriptProvider;

	private Configuration m_configuration;

	protected String getJavaFragement(AbtestRun run) {
		Gson gson = m_gsonBuilderManager.getGson();
		List<Condition> conditions = gson.fromJson(run.getConditions(), new TypeToken<ArrayList<Condition>>() {
		}.getType());
		Run newRun = new Run();
		Map<Object, Object> root = new HashMap<Object, Object>();

		newRun.getConditions().addAll(conditions);

		root.put("run", newRun);
		root.put("urlScriptProvider", m_urlScriptProvider);

		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate(ScriptProvider.m_fileName);

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}

		return sw.toString();
	}

	private void handleCreateOrUpdateAction(Context ctx, Payload payload, boolean isUpdate) {
		try {
			int caseID = -1;

			if (!isUpdate) {
				Abtest abtest = new Abtest();

				abtest.setName(payload.getName());
				abtest.setOwner(payload.getOwner());
				abtest.setDescription(payload.getDescription());
				abtest.setGroupStrategy(payload.getStrategyId());
				abtest.setDomains(StringUtils.join(payload.getDomains(), ','));

				m_service.insertAbtest(abtest);
				caseID = abtest.getId();
			}

			AbtestRun run = new AbtestRun();
			Date now = new Date();

			if (isUpdate) {
				run.setId(payload.getId());
				run.setKeyId(payload.getId());
			} else {
				run.setCaseId(caseID);
				run.setCreationDate(now);
			}
			run.setCreator(payload.getOwner());
			run.setStartDate(payload.getStartDate());
			run.setEndDate(payload.getEndDate());
			run.setDomains(StringUtils.join(payload.getDomains(), ','));
			run.setStrategyConfiguration(payload.getStrategyConfig());
			run.setConditions(payload.getConditions());
			run.setConversionGoals(payload.getConversionGoals());
			run.setJavaFragement(getJavaFragement(run));
			run.setDisabled(false);
			run.setModifiedDate(now);

			if (isUpdate) {
				// only update run info, do not update abtest meta-info
				m_service.updateAbtestRun(run);
				ctx.setResponseJson(responseJson(0, "successfully modify a abtest!"));
			} else {
				m_service.insertAbtestRun(run);
				ctx.setResponseJson(responseJson(0, "successfully create a abtest!"));
			}
		} catch (DalException e) {
			Cat.logError(e);
			ctx.setResponseJson(responseJson(1, e.getMessage()));
		}
	}

	@Override
	public void handleInbound(Context ctx, Payload payload) {
		Action action = payload.getAction();

		switch (action) {
		case AJAX_CREATE:
			handleCreateOrUpdateAction(ctx, payload, false);
			break;
		case AJAX_DETAIL:
			handleCreateOrUpdateAction(ctx, payload, true);
			break;
		}
	}

	@Override
	public void handleOutbound(Context ctx, Model model, Payload payload) {
		Action action = payload.getAction();

		switch (action) {
		case CREATE:
			renderCreateModel(model);
			break;
		case DETAIL:
			renderDetailModel(model, payload.getId());
			break;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(Handler.class, "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}

		m_urlScriptProvider = new URLScriptProvider();
	}

	private void renderCreateModel(Model model) {
		Map<String, List<Project>> projectMap = m_service.getAllProjects();
		List<GroupStrategy> groupStrategyList = m_service.getAllGroupStrategies();

		model.setProjectMap(projectMap);
		model.setGroupStrategyList(groupStrategyList);
	}

	private void renderDetailModel(Model model, int runId) {
		renderCreateModel(model);

		AbtestRun run = m_service.getAbTestRunById(runId);
		Abtest abtest = m_service.getABTestByCaseId(run.getCaseId());

		AbtestItem item = new AbtestItem(abtest, run);

		model.setAbtest(item);
	}

	/**
	 * 
	 * @param code
	 *           0 for success, 1 for failure
	 * @param msg
	 * @return
	 */
	public String responseJson(int code, String msg) {
		Gson gson = m_gsonBuilderManager.getGson();
		return gson.toJson(new ResponseJson(code, msg), ResponseJson.class);
	}
}
