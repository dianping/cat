package com.dianping.cat.system.page.abtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.ErrorObject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dainping.cat.consumer.dal.report.Project;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ProjectEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
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

public class Handler implements PageHandler<Context>, LogEnabled {

	public static final String CHARSET = "UTF-8";

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private GroupStrategyDao m_groupStrategyDao;

	@Inject
	private ProjectDao m_projectDao;
	
	@Inject
	private JspViewer m_jspViewer;
	
	private Logger m_logger;

	private final int m_pageSize = 10;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private AbtestModel fetchAbtestModel() {
		try {
			AbtestModel abtestModel = new AbtestModel();

			List<AbtestRun> abtestRuns = m_abtestRunDao.findAll(AbtestRunEntity.READSET_FULL);

			if (abtestRuns != null) {
				Date now = new Date();
				for (AbtestRun abtestRun : abtestRuns) {
					AbtestStatus status = AbtestStatus.calculateStatus(abtestRun, now);
					if (status == AbtestStatus.READY || status == AbtestStatus.RUNNING) {
						// fetch Case and GroupStrategy
						int caseId = abtestRun.getCaseId();
						Abtest entity = m_abtestDao.findByPK(caseId, AbtestEntity.READSET_FULL);
						int gid = entity.getGroupStrategy();
						GroupStrategy groupStrategy = m_groupStrategyDao.findByPK(gid, GroupStrategyEntity.READSET_FULL);

						Case _case = transform(abtestRun, entity, groupStrategy);
						abtestModel.addCase(_case);
					}
				}
			}
			System.out.println(abtestModel);
			return abtestModel;
		} catch (DalException e) {
			m_logger.error("Error when find all AbtestRun", e);
			Cat.logError(e);
		}
		return null;
	}

	private List<GroupStrategy> getAllGroupStrategys() {
		try {
			return m_groupStrategyDao.findAllByStatus(1, GroupStrategyEntity.READSET_FULL);
		} catch (DalException e) {
			m_logger.error(e.getMessage(), e);
			Cat.logError(e);
		}
		return null;
	}

	private Map<String, List<Project>> getAllProjects() {
		List<Project> projects = new ArrayList<Project>();

		try {
			projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			Cat.logError(e);
		}
		// Collections.sort(projects, new ProjectCompartor());
		return transform(projects);
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
		} catch (DalException e) {
			m_logger.error("Error when saving abtest", e);
			Cat.logError(e);
			ctx.setException(e);
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "abtest")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		if (ctx.getException() != null) {
			return;
		}

		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (ctx.getHttpServletRequest().getMethod().equalsIgnoreCase("post")) {
			if (action == Action.CREATE) {
				handleCreateAction(ctx, payload);
			} else if (action == Action.DETAIL) {
				handleUpdateAction(ctx, payload);
			}
		}

		if (action == Action.VIEW) {
			handleStatusChangeAction(ctx);
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
		} catch (DalException e) {
			m_logger.error("Error when updating abtest", e);
			Cat.logError(e);
			ctx.setException(e);
		}
	}

	private void renderCreateModel(Model model) {
		Map<String, List<Project>> projectMap = getAllProjects();
		List<GroupStrategy> groupStrategyList = getAllGroupStrategys();

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
		model.setAbtestModel(fetchAbtestModel().toString());
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

	private Case transform(AbtestRun abtestRun, Abtest entity, GroupStrategy groupStrategy) throws DalException {
		Case _case = new Case(entity.getId());
		_case.setCreatedDate(entity.getCreationDate());
		_case.setDescription(entity.getDescription());
		_case.setGroupStrategy(groupStrategy.getName());
		_case.setName(entity.getName());
		_case.setOwner(entity.getOwner());
		_case.setLastModifiedDate(entity.getModifiedDate());
		for (String domain : StringUtils.split(entity.getDomains(), ',')) {
			_case.addDomain(domain);
		}

		Run run = new Run(abtestRun.getId());
		for (String domain : StringUtils.split(abtestRun.getDomains(), ',')) {
			run.addDomain(domain);
		}
		run.setCreator(abtestRun.getCreator());
		run.setDisabled(false);
		run.setEndDate(abtestRun.getEndDate());
		run.setGroupStrategyConfiguration(abtestRun.getStrategyConfiguration());
		run.setStartDate(abtestRun.getStartDate());

		_case.addRun(run);

		return _case;
	}

	private Map<String, List<Project>> transform(List<Project> projects) {
		Map<String, List<Project>> re = new TreeMap<String, List<Project>>();
		if (projects != null) {
			for (Project project : projects) {
				String key = project.getDepartment() + "-" + project.getProjectLine();
				List<Project> list = re.get(key);
				if (list == null) {
					list = new ArrayList<Project>();
					re.put(key, list);
				}
				list.add(project);
			}
		}
		return re;
	}
}
