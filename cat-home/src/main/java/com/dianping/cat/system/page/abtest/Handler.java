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

public class Handler implements PageHandler<Context>, LogEnabled {

	public static final String CHARSET = "UTF-8";

	private final int m_pageSize = 5;

	private Logger m_logger;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private GroupStrategyDao m_groupStrategyDao;

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
				Abtest abtest = new Abtest();

				// TODO need more parameters for abtest
				abtest.setName(payload.getName());
				abtest.setDescription(payload.getDescription());
				abtest.setGroupStrategy(payload.getStrategyId());
				abtest.setDomains(StringUtils.join(payload.getDomains(), ','));

				AbtestRun run = new AbtestRun();

				run.setStartDate(payload.getStartDate());
				run.setEndDate(payload.getEndDate());
				run.setDomains(StringUtils.join(payload.getDomains(), ','));
				run.setStrategyConfiguration(payload.getStrategyConfig());
				try {
					m_abtestDao.insert(abtest);
				} catch (DalException e) {
					m_logger.error("Error when saving abtest", e);
					ctx.setException(e);
				}
			} else if (action == Action.DETAIL) {
				Abtest abtest = new Abtest();
				
				abtest.setKeyId(payload.getId());
				abtest.setName(payload.getName());
				abtest.setDescription(payload.getDescription());
				abtest.setDomains(StringUtils.join(payload.getDomains(), ','));
				
				AbtestRun run = new AbtestRun();

				run.setStartDate(payload.getStartDate());
				run.setEndDate(payload.getEndDate());
				run.setDomains(StringUtils.join(payload.getDomains(), ','));
				run.setStrategyConfiguration(payload.getStrategyConfig());
				
				try {
					m_abtestDao.updateByPK(abtest, AbtestEntity.UPDATESET_FULL);
				} catch (DalException e) {
					m_logger.error("Error when saving abtest", e);
					ctx.setException(e);
				}
			}
		}

		if (action == Action.VIEW) {
			handleStatusChangeActions(ctx);
		}
	}

	private void handleStatusChangeActions(Context ctx) {
		Payload payload = ctx.getPayload();
		ErrorObject error = new ErrorObject("disable");
		String[] ids = payload.getIds();

		if (ids != null && ids.length != 0) {
			for (String id : ids) {
				System.out.println("change status for " + id);
				try {
					int runID = Integer.parseInt(id);
					AbtestRun run = m_abtestRunDao.findByPK(runID, AbtestRunEntity.READSET_FULL);

					if (payload.getDisableAbtest() == -1) { // suspend
						if (!run.isDisabled()) {
							run.setDisabled(true);
							m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_STATUS);
						} else {
							error.addArgument(id, "Abtest " + id + " has been already suspended!");
						}
					} else if (payload.getDisableAbtest() == 1) { // resume
						if (run.isDisabled()) {
							run.setDisabled(false);
							m_abtestRunDao.updateByPK(run, AbtestRunEntity.UPDATESET_STATUS);
						} else {
							error.addArgument(id, "Abtest " + id + " has been already active!");
						}
					}
				} catch (NumberFormatException e) {
					// do nothing
				} catch (DalException e) {
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
	@OutboundActionMeta(name = "abtest")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (action == Action.VIEW) {
			renderListView(model, payload);
		} else if (action == Action.CREATE) {
			Map<String, List<Project>> projectMap = getAllProjects();
			List<GroupStrategy> groupStrategyList = getAllGroupStrategys();
			model.setProjectMap(projectMap);
			model.setGroupStrategyList(groupStrategyList);
		} else if (action == Action.DETAIL) {
			Map<String, List<Project>> projectMap = getAllProjects();
			List<GroupStrategy> groupStrategyList = getAllGroupStrategys();
			model.setProjectMap(projectMap);
			model.setGroupStrategyList(groupStrategyList);
			int abtestId = payload.getId();
			try {
				Abtest abtest = m_abtestDao.findByPK(abtestId, AbtestEntity.READSET_FULL);
				model.setAbtest(abtest);
			} catch (DalException e) {
				m_logger.error("Error when fetching abtest", e);
				ctx.setException(e);
			}
		} else if (action == Action.REPORT) {

		}

		model.setAction(action);
		model.setPage(SystemPage.ABTEST);
		m_jspViewer.view(ctx, model);
	}

	private void renderListView(Model model, Payload payload) {
		List<ABTestReport> reports = new ArrayList<ABTestReport>();
		List<Abtest> entities = new ArrayList<Abtest>();
		AbtestStatus status = AbtestStatus.getByName(payload.getStatus(), null);
		Date now = new Date();

		try {
			entities = m_abtestDao.findAll(AbtestEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}

		List<ABTestReport> filterReports = new ArrayList<ABTestReport>();
		List<ABTestReport> totalReports = new ArrayList<ABTestReport>();
		int createdCount = 0, readyCount = 0, runningCount = 0, terminatedCount = 0, suspendedCount = 0;

		for (Abtest abtest : entities) {
			List<AbtestRun> runs = new ArrayList<AbtestRun>();
			try {
				runs = m_abtestRunDao.findByCaseId(abtest.getId(), AbtestRunEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			
			for(AbtestRun run : runs){
				ABTestReport report = new ABTestReport(abtest,run, now);
				
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
		}

		model.setCreatedCount(createdCount);
		model.setReadyCount(readyCount);
		model.setRunningCount(runningCount);
		model.setTerminatedCount(terminatedCount);
		model.setSuspendedCount(suspendedCount);
		
		if (status != null) {
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

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
