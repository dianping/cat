package com.dianping.cat.system.page.abtest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.ErrorObject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Condition;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.abtest.ScriptFragementTest;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.AbtestRunEntity;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.home.dal.abtest.GroupStrategyDao;
import com.dianping.cat.home.dal.abtest.GroupStrategyEntity;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.abtest.conditions.URLScriptProvider;
import com.dianping.cat.system.page.abtest.ListViewModel.AbtestItem;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvice;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvisor;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Handler implements PageHandler<Context>, LogEnabled, Initializable {

	public static final String CHARSET = "UTF-8";

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private GroupStrategyDao m_groupStrategyDao;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private GroupStrategyParser m_parser;

	@Inject
	private ABTestService m_service;

	@Inject
	private ABTestAdvisor m_advisor;

	@Inject
	private ListViewHandler m_listViewHandler;
	
	@Inject
	private ReportHandler m_reportHandler;
	
	@Inject
	private GsonBuilderManager m_gsonBuilderManager;

	private Logger m_logger;

	private Configuration m_configuration;

	private URLScriptProvider m_urlScriptProvider;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private void handleCreateOrUpdateAction(Context ctx, Payload payload, boolean isUpdate) {
		try {
			int caseID = 0;

			if (!isUpdate) {
				Abtest abtest = new Abtest();

				abtest.setName(payload.getName());
				abtest.setOwner(payload.getOwner());
				abtest.setDescription(payload.getDescription());
				abtest.setGroupStrategy(payload.getStrategyId());
				abtest.setDomains(StringUtils.join(payload.getDomains(), ','));

				m_abtestDao.insert(abtest);
				m_service.setModified();
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
				m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_ALLOWED_MODIFYPART);
				m_service.setModified();
				ctx.setResponseJson(responseJson(0, "successfully modify a abtest!"));
			} else {
				m_abtestRunDao.insert(run);
				m_service.setModified();
				ctx.setResponseJson(responseJson(0, "successfully create a abtest!"));
			}
		} catch (DalException e) {
			Cat.logError(e);
			ctx.setResponseJson(responseJson(1, e.getMessage()));
		}
	}

	private String getJavaFragement(AbtestRun run) {
		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();
		List<Condition> conditions = gson.fromJson(run.getConditions(), new TypeToken<ArrayList<Condition>>() {
		}.getType());
		Run abstractRun = new Run();
		Map<Object, Object> root = new HashMap<Object, Object>();

		abstractRun.getConditions().addAll(conditions);

		root.put("run", abstractRun);
		root.put("urlScriptProvider", m_urlScriptProvider);

		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("scriptFragement.ftl");

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
		}
		return sw.toString();
	}

	private void handleCreateGroupStrategyAction(Context ctx, Payload payload) {
		GroupStrategy gs = new GroupStrategy();

		String name = payload.getGroupStrategyName();
		gs.setClassName(payload.getGroupStrategyClassName());
		gs.setName(name);
		gs.setFullyQualifiedName(payload.getGroupStrategyFullName());
		gs.setDescriptor(payload.getGroupStrategyDescriptor());
		gs.setDescription(payload.getGroupStrategyDescription());
		gs.setStatus(1);

		try {
			List<GroupStrategy> groupStrategies = m_groupStrategyDao.findByName(name, GroupStrategyEntity.READSET_FULL);

			if (groupStrategies == null || groupStrategies.size() == 0) {
				m_groupStrategyDao.insert(gs);
				m_service.setModified();
			} else {
				throw new DalException("Aready to has a groupstrategy which has the same name...");
			}

			ctx.setResponseJson(responseJson(0, "successfully create a groupstrategy!"));
		} catch (DalException e) {
			Cat.logError(e);
			ctx.setResponseJson(responseJson(1, e.getMessage()));
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "abtest")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		if (ctx.getException() != null) {
			ctx.setResponseJson(responseJson(1, ctx.getException().getMessage()));
			return;
		}

		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (action == Action.VIEW) {
			handleStatusChangeAction(ctx);
		}

		if (ctx.getHttpServletRequest().getMethod().equalsIgnoreCase("post")) {
			if (action == Action.AJAX_CREATE) {
				handleCreateOrUpdateAction(ctx, payload, false);
			} else if (action == Action.AJAX_DETAIL) {
				handleCreateOrUpdateAction(ctx, payload, true);
			} else if (action == Action.AJAX_ADDGROUPSTRATEGY) {
				handleCreateGroupStrategyAction(ctx, payload);
			} else if (action == Action.AJAX_PARSEGROUPSTRATEGY) {
				handleParseGroupStrategyAction(ctx, payload);
			} else if (action == Action.ABTEST_CACULATOR) {
				handleCaculatorAction(ctx, payload);
			}
		}
	}

	private void handleCaculatorAction(Context ctx, Payload payload) {
		float actualCtr = payload.getConversionRate() / 100.00f;

		m_advisor.setCurrentPv(payload.getPv());
		List<ABTestAdvice> advices = m_advisor.offer(actualCtr, actualCtr + 0.10f);

		ctx.setAdvice(advices);
	}

	@Override
	@OutboundActionMeta(name = "abtest")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			m_listViewHandler.handle(ctx, model, payload);
			break;
		case CREATE:
			renderCreateModel(model);
			break;
		case DETAIL:
			renderDetailModel(ctx, model, payload);
			break;
		case REPORT:
			m_reportHandler.handle(ctx, model, payload);
			break;
		case MODEL:
			renderModel(model, payload);
			break;
		case SCRIPT_FRAGEMENT:
			renderModelByRunId(payload, model);
			break;
		}

		model.setAction(action);
		model.setPage(SystemPage.ABTEST);
		m_jspViewer.view(ctx, model);
	}

	private void handleParseGroupStrategyAction(Context ctx, Payload payload) {
		InputStream stream;
		try {
			stream = new ByteArrayInputStream(payload.getSrcCode().getBytes("UTF-8"));
			GroupstrategyDescriptor descriptor = m_parser.parse(stream);

			Gson gson = m_gsonBuilderManager.getGsonBuilder().create();
			ctx.setResponseJson(gson.toJson(descriptor, GroupstrategyDescriptor.class));
		} catch (Throwable e) {
			ctx.setResponseJson("{}");
		}
	}

	private void handleStatusChangeAction(Context ctx) {
		Payload payload = ctx.getPayload();
		ErrorObject error = new ErrorObject("disable");
		String[] ids = payload.getIds();

		if (ids != null && ids.length != 0) {
			for (String id : ids) {
				try {
					int runID = Integer.parseInt(id);
					AbtestRun run = m_abtestRunDao.findByPK(runID, AbtestRunEntity.READSET_FULL);

					if (payload.getDisableAbtest() == -1) {
						// suspend abtest
						if (!run.isDisabled()) {
							run.setDisabled(true);
							m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_STATUS);
							m_service.setModified();
						} else {
							error.addArgument(id, String.format("Abtest %d has been already suspended!", id));
						}
					} else if (payload.getDisableAbtest() == 1) {
						// resume abtest
						if (run.isDisabled()) {
							run.setDisabled(false);
							m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_STATUS);
							m_service.setModified();
						} else {
							error.addArgument(id, String.format("Abtest %d has been already active!", id));
						}
					}
				} catch (Throwable e) {
					// do nothing
					Cat.logError(e);
				}
			}

			if (error.getArguments().isEmpty()) {
				ErrorObject success = new ErrorObject("success");
				ctx.addError(success);
			} else {
				ctx.addError(error);
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_service instanceof Task) {
			Threads.forGroup("Cat").start((Task) m_service);
		}

		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(ScriptFragementTest.class, "/freemaker");
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

	private void renderDetailModel(Context ctx, Model model, Payload payload) {
		renderCreateModel(model);
		int runId = payload.getId();

		try {
			AbtestRun run = m_abtestRunDao.findByPK(runId, AbtestRunEntity.READSET_FULL);
			Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);

			AbtestItem item = new AbtestItem(abtest, run);

			model.setAbtest(item);
		} catch (DalException e) {
			Cat.logError(e);
			m_logger.error("Error when fetching abtest", e);
			ctx.setException(e);
		}
	}

	private void renderModel(Model model, Payload payload) {
		long lastUpdateTime = payload.getLastUpdateTime();
		AbtestModel filteredModel = new AbtestModel();

		if (lastUpdateTime < m_service.getModifiedTime()) {
			AbtestModel abtestModel = m_service.getAbtestModelByStatus(AbtestStatus.READY, AbtestStatus.RUNNING);

			for (Case _case : abtestModel.getCases()) {
				Case newCase = new Case();

				for (Run run : _case.getRuns()) {
					if (run.getLastModifiedDate().getTime() > lastUpdateTime) {
						newCase.addRun(run);
					}
				}

				if (newCase.getRuns().size() > 0) {
					newCase.setId(_case.getId());
					newCase.setGroupStrategy(_case.getGroupStrategy());
					newCase.setOwner(_case.getOwner());
					newCase.setDescription(_case.getDescription());
					newCase.getDomains().addAll(_case.getDomains());
					newCase.mergeAttributes(_case);

					filteredModel.addCase(newCase);
				}
			}
		}

		model.setAbtestModel(filteredModel);
	}

	private void renderModelByRunId(Payload payload, Model model) {
		AbtestModel abtestModel = m_service.getAbtestModelByRunID(payload.getId());

		model.setAbtestModel(abtestModel);
	}

	/**
	 * 
	 * @param code
	 *           0 for success, 1 for failure
	 * @param msg
	 * @return
	 */
	public String responseJson(int code, String msg) {
		Gson gson = m_gsonBuilderManager.getGsonBuilder().create();
		return gson.toJson(new ResponseJson(code, msg), ResponseJson.class);
	}

	static class ResponseJson {
		private int m_code;

		private String m_msg;

		public ResponseJson(int code, String msg) {
			m_code = code;
			m_msg = msg;
		}

		public int getCode() {
			return m_code;
		}

		public void setCode(int code) {
			m_code = code;
		}

		public String getMsg() {
			return m_msg;
		}

		public void setMsg(String msg) {
			m_msg = msg;
		}
	}
}
