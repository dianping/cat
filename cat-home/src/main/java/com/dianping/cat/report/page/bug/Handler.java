package com.dianping.cat.report.page.bug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.Domain;
import com.dianping.cat.home.bug.entity.ExceptionItem;
import com.dianping.cat.home.bug.transform.BaseVisitor;
import com.dianping.cat.home.heavy.entity.HeavyCache;
import com.dianping.cat.home.heavy.entity.HeavyCall;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.HeavySql;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.heavy.HeavyReportMerger.ServiceComparator;
import com.dianping.cat.report.task.heavy.HeavyReportMerger.UrlComparator;
import com.dianping.cat.system.config.BugConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private BugConfigManager m_bugConfigManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	private void buildSortedHeavyInfo(Model model, HeavyReport heavyReport) {
		HeavyCall heavyCall = heavyReport.getHeavyCall();
		if (heavyCall != null) {

			List<Url> callUrls = new ArrayList<Url>(heavyCall.getUrls().values());
			List<Service> callServices = new ArrayList<Service>(heavyCall.getServices().values());
			Collections.sort(callUrls, new UrlComparator());
			Collections.sort(callServices, new ServiceComparator());
			model.setCallUrls(callUrls);
			model.setCallServices(callServices);
		}

		HeavySql heavySql = heavyReport.getHeavySql();

		if (heavySql != null) {
			List<Url> sqlUrls = new ArrayList<Url>(heavySql.getUrls().values());
			List<Service> sqlServices = new ArrayList<Service>(heavySql.getServices().values());
			Collections.sort(sqlUrls, new UrlComparator());
			Collections.sort(sqlServices, new ServiceComparator());
			model.setSqlUrls(sqlUrls);
			model.setSqlServices(sqlServices);
		}

		HeavyCache heavyCache = heavyReport.getHeavyCache();
		if (heavyCache != null) {
			List<Url> cacheUrls = new ArrayList<Url>(heavyCache.getUrls().values());
			List<Service> cacheServices = new ArrayList<Service>(heavyCache.getServices().values());
			Collections.sort(cacheUrls, new UrlComparator());
			Collections.sort(cacheServices, new ServiceComparator());
			model.setCacheUrls(cacheUrls);
			model.setCacheServices(cacheServices);
		}
	}

	public Project findByDomain(String domain) {
		try {
			return m_projectDao.findByDomain(domain, ProjectEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "bug")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "bug")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		m_normalizePayload.normalize(model, payload);
		Action action = payload.getAction();

		switch (action) {
		case SERVICE_REPORT:
		case SERVICE_HISTORY_REPORT:
			ServiceReport serviceReport = queryServiceReport(payload);
			List<com.dianping.cat.home.service.entity.Domain> dHisList = sort(serviceReport, payload.getSortBy());
			model.setServiceList(dHisList);
			model.setServiceReport(serviceReport);
			break;
		case BUG_HISTORY_REPORT:
		case BUG_REPORT:
		case BUG_HTTP_JSON:
			BugReport bugReport = queryBugReport(payload);
			BugReportVisitor visitor = new BugReportVisitor();
			visitor.visitBugReport(bugReport);

			model.setBugReport(bugReport);
			bugReport = queryBugReport(payload);
			visitor = new BugReportVisitor();
			visitor.visitBugReport(bugReport);

			Map<String, ErrorStatis> errors = visitor.getErrors();
			errors = sortErrorStatis(errors);
			model.setErrorStatis(errors);

			if (action == Action.BUG_HTTP_JSON) {
				new ClearBugReport().visitBugReport(bugReport);
			}
			model.setBugReport(bugReport);
			break;
		case HEAVY_HISTORY_REPORT:
		case HEAVY_REPORT:
			HeavyReport heavyReport = queryHeavyReport(payload);

			model.setHeavyReport(heavyReport);
			buildSortedHeavyInfo(model, heavyReport);
			break;
		case UTILIZATION_REPORT:
		case UTILIZATION_HISTORY_REPORT:
			UtilizationReport utilizationReport = queryUtilizationReport(payload);
			List<com.dianping.cat.home.utilization.entity.Domain> dUList = sort(utilizationReport, payload.getSortBy());
			model.setUtilizationReport(utilizationReport);
			model.setUtilizationList(dUList);
			break;
		}
		model.setPage(ReportPage.BUG);
		m_jspViewer.view(ctx, model);
	}

	private boolean isBug(String domain, String exception) {
		Set<String> bugConfig = m_bugConfigManager.queryBugConfigsByDomain(domain);

		return !bugConfig.contains(exception);
	}

	private BugReport queryBugReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);

		return m_reportService.queryBugReport(CatString.CAT, pair.getKey(), pair.getValue());
	}

	private HeavyReport queryHeavyReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);

		return m_reportService.queryHeavyReport(CatString.CAT, pair.getKey(), pair.getValue());
	}

	private ServiceReport queryServiceReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);

		return m_reportService.queryServiceReport(CatString.CAT, pair.getKey(), pair.getValue());
	}

	private Pair<Date, Date> queryStartEndTime(Payload payload) {
		Date start = null;
		Date end = null;
		if (!payload.getAction().getName().startsWith("history")) {
			if (payload.getPeriod().isCurrent()) {
				start = new Date(payload.getDate() - TimeUtil.ONE_HOUR);
				end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
			} else {
				start = new Date(payload.getDate());
				end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
			}
		} else {
			start = payload.getHistoryStartDate();
			end = payload.getHistoryEndDate();
		}
		return new Pair<Date, Date>(start, end);
	}

	private UtilizationReport queryUtilizationReport(Payload payload) {
		Pair<Date, Date> pair = queryStartEndTime(payload);
		UtilizationReport report = m_reportService.queryUtilizationReport(CatString.CAT, pair.getKey(), pair.getValue());
		System.out.println(report);
		new UtilizationReportScore().visitUtilizationReport(report);
		return report;
	}

	private List<com.dianping.cat.home.service.entity.Domain> sort(ServiceReport serviceReport, final String sortBy) {
		List<com.dianping.cat.home.service.entity.Domain> result = new ArrayList<com.dianping.cat.home.service.entity.Domain>(
		      serviceReport.getDomains().values());
		Collections.sort(result, new Comparator<com.dianping.cat.home.service.entity.Domain>() {
			public int compare(com.dianping.cat.home.service.entity.Domain d1,
			      com.dianping.cat.home.service.entity.Domain d2) {
				if (sortBy.equals("failure")) {
					return (int) (d2.getFailureCount() - d1.getFailureCount());
				} else if (sortBy.equals("total")) {
					return (int) (d2.getTotalCount() - d1.getTotalCount());
				} else if (sortBy.equals("failurePercent")) {
					return (int) (100000 * d2.getFailurePercent() - 100000 * d1.getFailurePercent());
				} else {
					return (int) (d2.getAvg() - d1.getAvg());
				}
			}
		});
		return result;
	}

	private List<com.dianping.cat.home.utilization.entity.Domain> sort(UtilizationReport utilizationReport,
	      final String sortBy) {
		List<com.dianping.cat.home.utilization.entity.Domain> result = new ArrayList<com.dianping.cat.home.utilization.entity.Domain>(
		      utilizationReport.getDomains().values());
		Collections.sort(result, new Comparator<com.dianping.cat.home.utilization.entity.Domain>() {
			public int compare(com.dianping.cat.home.utilization.entity.Domain d1,
			      com.dianping.cat.home.utilization.entity.Domain d2) {
				if (sortBy.equals("urlCount")) {
					return (int) (d2.getUrlCount() - d1.getUrlCount());
				} else if (sortBy.equals("urlResponse")) {
					return (int) (100 * d2.getUrlResponseTime() - 100 * d1.getUrlResponseTime());
				} else if (sortBy.equals("serviceCount")) {
					return (int) (d2.getServiceCount() - d1.getServiceCount());
				} else if (sortBy.equals("serviceResponse")) {
					return (int) (100 * d2.getServiceResponseTime() - 100 * d1.getServiceResponseTime());
				} else if (sortBy.equals("sqlCount")) {
					return (int) (d2.getSqlCount() - d1.getSqlCount());
				} else if (sortBy.equals("pigeonCallCount")) {
					return (int) (d2.getPigeonCallCount() - d1.getPigeonCallCount());
				} else if (sortBy.equals("swallowCallCount")) {
					return (int) (d2.getSwallowCallCount() - d1.getSwallowCallCount());
				} else if (sortBy.equals("memcacheCount")) {
					return (int) (d2.getMemcacheCount() - d1.getMemcacheCount());
				} else if (sortBy.equals("webScore")) {
					return (int) (d2.getWebScore() - d1.getWebScore());
				} else if (sortBy.equals("serviceScore")) {
					return (int) (d2.getServiceScore() - d1.getServiceScore());
				} else {
					return (int) (d2.getWebScore() - d1.getWebScore());
				}
			}
		});
		return result;
	}

	private Map<String, ErrorStatis> sortErrorStatis(Map<String, ErrorStatis> errors) {
		Comparator<java.util.Map.Entry<String, ErrorStatis>> errorCompator = new Comparator<java.util.Map.Entry<String, ErrorStatis>>() {

			@Override
			public int compare(java.util.Map.Entry<String, ErrorStatis> o1, java.util.Map.Entry<String, ErrorStatis> o2) {
				String department1 = o1.getValue().getDepartment();
				String department2 = o2.getValue().getDepartment();
				String productLine1 = o1.getValue().getProductLine();
				String productLine2 = o2.getValue().getProductLine();

				if (department1.equals(department2)) {
					return productLine1.compareTo(productLine2);
				} else {
					return department1.compareTo(department2);
				}
			}
		};
		errors = MapUtils.sortMap(errors, errorCompator);

		for (ErrorStatis temp : errors.values()) {
			Comparator<java.util.Map.Entry<String, ExceptionItem>> compator = new Comparator<java.util.Map.Entry<String, ExceptionItem>>() {

				@Override
				public int compare(java.util.Map.Entry<String, ExceptionItem> o1,
				      java.util.Map.Entry<String, ExceptionItem> o2) {
					return o2.getValue().getCount() - o1.getValue().getCount();
				}
			};
			Map<String, ExceptionItem> bugs = temp.getBugs();
			Map<String, ExceptionItem> exceptions = temp.getExceptions();

			temp.setBugs(MapUtils.sortMap(bugs, compator));
			temp.setExceptions(MapUtils.sortMap(exceptions, compator));
		}

		return errors;
	}

	public class BugReportVisitor extends BaseVisitor {
		private String m_domain;

		private Map<String, ErrorStatis> m_errors = new HashMap<String, ErrorStatis>();

		public ErrorStatis findOrCreateErrorStatis(String productLine) {
			ErrorStatis statis = m_errors.get(productLine);

			if (statis == null) {
				statis = new ErrorStatis();
				m_errors.put(productLine, statis);
			}
			return statis;
		}

		public Map<String, ErrorStatis> getErrors() {
			return m_errors;
		}

		@Override
		public void visitDomain(Domain domain) {
			m_domain = domain.getId();
			super.visitDomain(domain);
		}

		@Override
		public void visitExceptionItem(ExceptionItem exceptionItem) {
			String exception = exceptionItem.getId();
			int count = exceptionItem.getCount();
			Project project = findByDomain(m_domain);

			if (project != null) {
				String productLine = project.getProjectLine();
				String department = project.getDepartment();
				ErrorStatis statis = findOrCreateErrorStatis(productLine);

				statis.setDepartment(department);
				statis.setProductLine(productLine);

				Map<String, ExceptionItem> items = null;

				if (isBug(m_domain, exception)) {
					items = statis.getBugs();
				} else {
					items = statis.getExceptions();
				}

				ExceptionItem item = items.get(exception);

				if (item == null) {
					item = new ExceptionItem(exception);
					item.setCount(count);
					item.getMessages().addAll(exceptionItem.getMessages());
					items.put(exception, item);
				} else {
					List<String> messages = item.getMessages();
					item.setCount(item.getCount() + count);
					messages.addAll(exceptionItem.getMessages());

					if (messages.size() > 10) {
						messages = messages.subList(0, 10);
					}
				}
			}
		}
	}

	public class ClearBugReport extends BaseVisitor {

		@Override
		public void visitDomain(Domain domain) {
			String domainName = domain.getId();
			Set<String> removes = new HashSet<String>();
			Map<String, ExceptionItem> items = domain.getExceptionItems();

			for (ExceptionItem item : items.values()) {
				if (!isBug(domainName, item.getId())) {
					removes.add(item.getId());
				}
			}
			for (String remove : removes) {
				items.remove(remove);
			}
		}
	}
}
