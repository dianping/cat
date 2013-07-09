package com.dianping.cat.system.page.abtest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
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
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.core.dal.Project;
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
import com.dianping.cat.system.page.abtest.Model.AbtestDaoModel;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.google.gson.Gson;

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

	private Logger m_logger;

	private final int m_pageSize = 10;

	@Inject
	private GroupStrategyParser m_parser;

	@Inject
	private ABTestService m_service;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private void handleCreateAction(Context ctx, Payload payload) {
		Abtest abtest = new Abtest();

		abtest.setName(payload.getName());
		abtest.setOwner(payload.getOwner());
		abtest.setDescription(payload.getDescription());
		abtest.setGroupStrategy(payload.getStrategyId());
		abtest.setDomains(StringUtils.join(payload.getDomains(), ','));

		AbtestRun run = new AbtestRun();

		run.setCreator(payload.getOwner());
		run.setStartDate(payload.getStartDate());
		run.setEndDate(payload.getEndDate());
		run.setDomains(StringUtils.join(payload.getDomains(), ','));
		run.setStrategyConfiguration(payload.getStrategyConfig());
		run.setDisabled(false);
		Date now = new Date();
		run.setCreationDate(now);
		run.setModifiedDate(now);
		try {
			m_abtestDao.insert(abtest);

			run.setCaseId(abtest.getId());
			m_abtestRunDao.insert(run);
			ctx.setResponseJson(responseJson(0, "successfully create a abtest!"));
		} catch (DalException e) {
			Cat.logError(e);
			ctx.setResponseJson(responseJson(1, e.getMessage()));
		}
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
				handleCreateAction(ctx, payload);
			} else if (action == Action.AJAX_DETAIL) {
				handleUpdateAction(ctx, payload);
			} else if (action == Action.AJAX_ADDGROUPSTRATEGY) {
				handleCreateGroupStrategyAction(ctx, payload);
			} else if (action == Action.AJAX_PARSEGROUPSTRATEGY) {
				handleParseGroupStrategyAction(ctx, payload);
			}
		}

	}

	@Override
	@OutboundActionMeta(name = "abtest")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			renderListModel(model, payload);
			break;
		case CREATE:
			renderCreateModel(model);
			break;
		case DETAIL:
			renderDetailModel(ctx, model, payload);
			break;
		case REPORT:
			renderReportModel(ctx, model, payload);
			break;
		case MODEL:
			renderModel(model);
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

			Gson gson = m_parser.getGsonBuilder().create();
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
						} else {
							error.addArgument(id, String.format("Abtest %d has been already suspended!", id));
						}
					} else if (payload.getDisableAbtest() == 1) {
						// resume abtest
						if (run.isDisabled()) {
							run.setDisabled(false);
							m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_STATUS);
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

	private void handleUpdateAction(Context ctx, Payload payload) {
		try {
			AbtestRun run = new AbtestRun();

			run.setId(payload.getId());
			run.setKeyId(payload.getId());
			run.setCreator(payload.getOwner());
			run.setStartDate(payload.getStartDate());
			run.setEndDate(payload.getEndDate());
			run.setDomains(StringUtils.join(payload.getDomains(), ','));
			run.setStrategyConfiguration(payload.getStrategyConfig());
			Date now = new Date();
			run.setModifiedDate(now);

			// only update run info, do not update abtest meta-info
			m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_ALLOWED_MODIFYPART);
			m_service.refresh();
			ctx.setResponseJson(responseJson(0, "successfully modify a abtest!"));
		} catch (DalException e) {
			Cat.logError(e);
			ctx.setResponseJson(responseJson(1, e.getMessage()));
		}
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_service instanceof Task) {
			Threads.forGroup("Cat").start((Task) m_service);
		}
	}

	private void renderCreateModel(Model model) {
		Map<String, List<Project>> projectMap = m_service.getAllProjects();
		List<GroupStrategy> groupStrategyList = m_service.getAllGroupStrategies();

		model.setProjectMap(projectMap);
		model.setGroupStrategyList(groupStrategyList);
	}

	private void renderDetailModel(Context ctx, Model model, Payload payload) {
		renderCreateModel(model);
		renderReportModel(ctx, model, payload);
	}

	private void renderListModel(Model model, Payload payload) {
		List<ABTestReport> reports = new ArrayList<ABTestReport>();
		AbtestStatus status = AbtestStatus.getByName(payload.getStatus(), null);
		Date now = new Date();

		List<ABTestReport> filterReports = new ArrayList<ABTestReport>();
		List<ABTestReport> totalReports = new ArrayList<ABTestReport>();
		int createdCount = 0, readyCount = 0, runningCount = 0, terminatedCount = 0, suspendedCount = 0;

		List<AbtestRun> runs = new ArrayList<AbtestRun>();

		try {
			runs = m_abtestRunDao.findAll(AbtestRunEntity.READSET_FULL);

			for (AbtestRun run : runs) {
				Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);
				ABTestReport report = new ABTestReport(abtest, run, now);

				totalReports.add(report);
				if (status != null && report.getStatus() == status) {
					filterReports.add(report);
				}
				switch (report.getStatus()) {
				case CREATED:
					createdCount++;
					break;
				case READY:
					readyCount++;
					break;
				case RUNNING:
					runningCount++;
					break;
				case TERMINATED:
					terminatedCount++;
					break;
				case SUSPENDED:
					suspendedCount++;
					break;
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}

		model.setCreatedCount(createdCount);
		model.setReadyCount(readyCount);
		model.setRunningCount(runningCount);
		model.setTerminatedCount(terminatedCount);
		model.setSuspendedCount(suspendedCount);

		if (status != null) {
			totalReports = null;
			totalReports = filterReports;
		}

		int totalSize = totalReports.size();
		int totalPages = totalSize % m_pageSize == 0 ? (totalSize / m_pageSize) : (totalSize / m_pageSize + 1);

		// safe guarder for pageNum
		if (payload.getPageNum() >= totalPages) {
			if (totalPages == 0) {
				payload.setPageNum(1);
			} else {
				payload.setPageNum(totalPages);
			}
		} else if (payload.getPageNum() <= 0) {
			payload.setPageNum(1);
		}

		int fromIndex = (payload.getPageNum() - 1) * m_pageSize;
		int toIndex = (fromIndex + m_pageSize) <= totalSize ? (fromIndex + m_pageSize) : totalSize;
		for (int i = fromIndex; i < toIndex; i++) {
			reports.add(totalReports.get(i));
		}

		model.setTotalPages(totalPages);
		model.setDate(now);
		model.setReports(reports);
	}

	private void renderModel(Model model) {
		AbtestModel abtestModel = m_service.getAbtestModelByStatus(AbtestStatus.READY, AbtestStatus.RUNNING);

		model.setAbtestModel(abtestModel.toString());
	}

	private void renderReportModel(Context ctx, Model model, Payload payload) {
		try {
			int runId = payload.getId();
			AbtestRun run = m_abtestRunDao.findByPK(runId, AbtestRunEntity.READSET_FULL);
			Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);
			AbtestDaoModel abtestModel = new AbtestDaoModel(abtest, run);

			model.setAbtest(abtestModel);
		} catch (DalException e) {
			Cat.logError(e);
			m_logger.error("Error when fetching abtest", e);
			ctx.setException(e);
		}
	}

	/**
	 * 
	 * @param code
	 *           0 for success, 1 for failure
	 * @param msg
	 * @return
	 */
	public String responseJson(int code, String msg) {
		Gson gson = m_parser.getGsonBuilder().create();
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
