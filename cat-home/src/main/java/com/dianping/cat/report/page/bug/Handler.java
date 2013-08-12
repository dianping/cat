package com.dianping.cat.report.page.bug;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
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
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.service.ReportService;
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

		BugReport bugReport = queryBugReport(payload);
		BugReportVisitor visitor = new BugReportVisitor();
		visitor.visitBugReport(bugReport);

		Map<String, ErrorStatis> errors = visitor.getErrors();
		errors = sortErrorStatis(errors);

		model.setBugReport(bugReport);
		model.setErrorStatis(errors);
		model.setAction(payload.getAction());
		model.setPage(ReportPage.BUG);
		m_jspViewer.view(ctx, model);
	}

	private boolean isBug(String domain, String exception) {
		Set<String> bugConfig = m_bugConfigManager.queryBugConfigsByDomain(domain);

		return !bugConfig.contains(exception);
	}

	private BugReport queryBugReport(Payload payload) {
		Date start = null;
		Date end = null;
		if (payload.getAction() == Action.HOURLY_REPORT) {
			start = new Date(payload.getDate());
			end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		} else {
			start = payload.getHistoryStartDate();
			end = payload.getHistoryEndDate();
		}

		return m_reportService.queryBugReport(CatString.CAT, start, end);
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

	public static class ErrorStatis {
		private String m_productLine;

		private String m_department;

		private Map<String, ExceptionItem> m_bugs = new HashMap<String, ExceptionItem>();

		private Map<String, ExceptionItem> m_exceptions = new HashMap<String, ExceptionItem>();

		public Map<String, ExceptionItem> getBugs() {
			return m_bugs;
		}

		public String getDepartment() {
			return m_department;
		}

		public Map<String, ExceptionItem> getExceptions() {
			return m_exceptions;
		}

		public String getProductLine() {
			return m_productLine;
		}

		public void setBugs(Map<String, ExceptionItem> bugs) {
			m_bugs = bugs;
		}

		public void setDepartment(String department) {
			m_department = department;
		}

		public void setExceptions(Map<String, ExceptionItem> exceptions) {
			m_exceptions = exceptions;
		}

		public void setProductLine(String productLine) {
			m_productLine = productLine;
		}
	}
}
