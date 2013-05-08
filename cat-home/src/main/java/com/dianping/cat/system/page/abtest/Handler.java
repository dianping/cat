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

		if (action == Action.DOCREATE) {
			Abtest abtest = new Abtest();
			abtest.setName(payload.getName());
			abtest.setDescription(payload.getDescription());
			abtest.setStartDate(payload.getStartDate());
			abtest.setEndDate(payload.getEndDate());
			abtest.setDomains(StringUtils.join(payload.getDomains(), ','));
			abtest.setStrategyId(payload.getStrategyId());
			abtest.setStrategyConfig(payload.getStrategyConfig().getBytes(CHARSET));
			try {
				m_abtestDao.insert(abtest);
			} catch (DalException e) {
				m_logger.error("Error when saving abtest", e);
				ctx.setException(e);
			}
		} else if (action == Action.DETAIL && ctx.getHttpServletRequest().getMethod().equalsIgnoreCase("post")) {
			Abtest abtest = new Abtest();
			abtest.setKeyId(payload.getAbtestId());
			abtest.setName(payload.getName());
			abtest.setDescription(payload.getDescription());
			abtest.setStartDate(payload.getStartDate());
			abtest.setEndDate(payload.getEndDate());
			abtest.setDomains(StringUtils.join(payload.getDomains(), ','));
			abtest.setStrategyId(payload.getStrategyId());
			abtest.setStrategyConfig(payload.getStrategyConfig().getBytes(CHARSET));
			try {
				System.out.println(m_abtestDao.updateByPK(abtest, AbtestEntity.UPDATESET_FULL));
				System.out.println(abtest);
			} catch (DalException e) {
				m_logger.error("Error when saving abtest", e);
				ctx.setException(e);
			}
		}
	}

	@Override
	@OutboundActionMeta(name = "abtest")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (action == Action.LIST) {
			renderListView(model, payload);
		} else if (action == Action.VIEW) {
			Map<String, List<Project>> projectMap = getAllProjects();
			List<GroupStrategy> groupStrategyList = getAllGroupStrategys();
			model.setProjectMap(projectMap);
			model.setGroupStrategyList(groupStrategyList);
		} else if (action == Action.DOCREATE) {
			Map<String, List<Project>> projectMap = getAllProjects();
			List<GroupStrategy> groupStrategyList = getAllGroupStrategys();
			model.setProjectMap(projectMap);
			model.setGroupStrategyList(groupStrategyList);
		} else if (action == Action.DETAIL) {
			Map<String, List<Project>> projectMap = getAllProjects();
			List<GroupStrategy> groupStrategyList = getAllGroupStrategys();
			model.setProjectMap(projectMap);
			model.setGroupStrategyList(groupStrategyList);
			int abtestId = payload.getAbtestId();
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
			entities = m_abtestDao.findAllAbtest(AbtestEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}

		List<Abtest> filterTests = new ArrayList<Abtest>();
		int createdCount = 0, readyCount = 0, runningCount = 0, terminatedCount = 0, suspendedCount = 0;

		for (Abtest abtest : entities) {
			ABTestReport report = new ABTestReport(abtest, now);

			if (status != null && report.getStatus() == status) {
				filterTests.add(abtest);
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

		model.setCreatedCount(createdCount);
		model.setReadyCount(readyCount);
		model.setRunningCount(runningCount);
		model.setTerminatedCount(terminatedCount);
		model.setSuspendedCount(suspendedCount);
		if (status != null) {
			entities = filterTests;
		}

		int totalSize = entities.size();
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
			reports.add(new ABTestReport(entities.get(i), now));
		}

		model.setTotalPages(totalPages);
		model.setDate(now);
		model.setReports(reports);
	}

	private List<GroupStrategy> getAllGroupStrategys() {
		try {
			return m_groupStrategyDao.findAllGroupStrategy(GroupStrategyEntity.READSET_FULL);
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
