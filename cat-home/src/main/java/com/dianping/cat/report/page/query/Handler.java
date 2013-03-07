package com.dianping.cat.report.page.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.ReportService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	private List<TransactionQueryItem> buildTransactionItems(Date start, Date end, String domain, String type,
	      String name, String reportLevel) {
		List<TransactionQueryItem> items = new ArrayList<TransactionQueryItem>();

		if ("hour".equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_HOUR) {
				TransactionReport report = m_reportService.queryTransactionReport(domain, new Date(i), new Date(i
				      + TimeUtil.ONE_HOUR));

				items.add(convert(report, type, name));
			}
		} else if ("day".equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_DAY) {
				TransactionReport report = m_reportService.queryTransactionReport(domain, new Date(i), new Date(i
				      + TimeUtil.ONE_DAY));

				items.add(convert(report, type, name));
			}
		} else {
			throw new RuntimeException("Invalid query type");
		}
		return items;
	}

	private TransactionQueryItem convert(TransactionReport report, String type, String name) {
		TransactionReportVisitor vistitor = new TransactionReportVisitor(type, name);
		vistitor.visitTransactionReport(report);
		
		return vistitor.getItem();
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "query")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "query")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.QUERY);

		Date start = payload.getStart();
		Date end = payload.getEnd();
		String domain = payload.getQueryDomain();
		String type = payload.getType();
		String name = payload.getName();
		String reportLevel = payload.getReportLevel();
		String queryType = payload.getQueryType();

		if ("transaction".equals(queryType)) {
			List<TransactionQueryItem> items = buildTransactionItems(start, end, domain, type, name, reportLevel);

			model.setTransactionItems(items);
		} else if ("event".equals(queryType)) {
		} else if ("problem".equals(queryType)) {
		}
		m_jspViewer.view(ctx, model);
	}

	public static class TransactionQueryItem {
		private Date m_date;

		private String m_type;

		private String m_name;

		private long m_totalCount;

		private long m_failCount;

		private double m_failPercent;

		private double m_min = 86400000d;

		private double m_max = -1d;

		private double m_avg;

		private double m_tps;

		private double m_line95Value;

		public double getAvg() {
			return m_avg;
		}

		public Date getDate() {
			return m_date;
		}

		public long getFailCount() {
			return m_failCount;
		}

		public double getFailPercent() {
			return m_failPercent;
		}

		public double getLine95Value() {
			return m_line95Value;
		}

		public double getMax() {
			return m_max;
		}

		public double getMin() {
			return m_min;
		}

		public String getName() {
			return m_name;
		}

		public long getTotalCount() {
			return m_totalCount;
		}

		public double getTps() {
			return m_tps;
		}

		public String getType() {
			return m_type;
		}

		public void setAvg(double avg) {
			m_avg = avg;
		}

		public TransactionQueryItem setDate(Date date) {
			m_date = date;
			return this;
		}

		public TransactionQueryItem setFailCount(long failCount) {
			m_failCount = failCount;
			return this;
		}

		public TransactionQueryItem setFailPercent(double failPercent) {
			m_failPercent = failPercent;
			return this;
		}

		public TransactionQueryItem setLine95Value(double line95Value) {
			m_line95Value = line95Value;
			return this;
		}

		public TransactionQueryItem setMax(double max) {
			m_max = max;
			return this;
		}

		public TransactionQueryItem setMin(double min) {
			m_min = min;
			return this;
		}

		public TransactionQueryItem setName(String name) {
			m_name = name;
			return this;
		}

		public TransactionQueryItem setTotalCount(long totalCount) {
			m_totalCount = totalCount;
			return this;
		}

		public TransactionQueryItem setTps(double tps) {
			m_tps = tps;
			return this;
		}

		public TransactionQueryItem setType(String type) {
			m_type = type;
			return this;
		}
	}

	public static class TransactionReportVisitor extends BaseVisitor {
		private String m_type;

		private String m_name;

		private String m_currentType;

		private String m_currentName;

		public TransactionQueryItem m_item = new TransactionQueryItem();

		public TransactionReportVisitor(String type, String name) {
			m_type = type;
			m_name = name;
			m_item.setType(type);
			m_item.setName(name);
		}

		@Override
		public void visitName(TransactionName name) {
			m_currentName = name.getId();
			if (m_type.equalsIgnoreCase(m_currentType) && m_name.equalsIgnoreCase(m_currentName)) {
				m_item.setTotalCount(name.getTotalCount());
				m_item.setFailCount(name.getFailCount());
				m_item.setFailPercent(name.getFailPercent());
				m_item.setMin(name.getMin());
				m_item.setMax(name.getMax());
				m_item.setAvg(name.getAvg());
				m_item.setLine95Value(name.getLine95Value());
			}
		}

		@Override
		public void visitType(TransactionType type) {
			m_currentType = type.getId();
			if (m_name == null || m_name.trim().length() == 0) {
				if (m_type.equalsIgnoreCase(m_currentType)) {
					m_item.setTotalCount(type.getTotalCount());
					m_item.setFailCount(type.getFailCount());
					m_item.setFailPercent(type.getFailPercent());
					m_item.setMin(type.getMin());
					m_item.setMax(type.getMax());
					m_item.setAvg(type.getAvg());
					m_item.setLine95Value(type.getLine95Value());
				}
			} else {
				super.visitType(type);
			}
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			super.visitTransactionReport(transactionReport);
			m_item.setDate(transactionReport.getStartTime());
		}

		public TransactionQueryItem getItem() {
			return m_item;
		}

		public void setItem(TransactionQueryItem item) {
			m_item = item;
		}

	}
}
