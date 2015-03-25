package com.dianping.cat.report.page.transaction.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalTransactionService extends LocalModelService<TransactionReport> {

	public static final String ID = TransactionAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	private String filterReport(ApiPayload payload, TransactionReport report) {
		String type = payload.getType();
		String name = payload.getName();
		String ip = payload.getIpAddress();
		String xml = null;

		try {
			TransactionReportFilter filter = new TransactionReportFilter(type, name, ip);

			xml = filter.buildXml(report);
		} catch (Exception e) {
			TransactionReportFilter filter = new TransactionReportFilter(type, name, ip);

			xml = filter.buildXml(report);
		}
		return xml;
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		TransactionReport report = super.getReport(period, domain);

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return filterReport(payload, report);
	}

	private TransactionReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, TransactionAnalyzer.ID);
			String xml = bucket.findById(domain);
			TransactionReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new TransactionReport(domain);
				report.setStartTime(new Date(timestamp));
				report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));
				report.getDomainNames().addAll(bucket.getIds());
			}
			return report;

		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class TransactionReportFilter extends
	      com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public TransactionReportFilter(String type, String name, String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitAllDuration(AllDuration duration) {
		}

		@Override
		public void visitDuration(Duration duration) {
			if (m_type != null && m_name != null) {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.transaction.model.entity.Machine machine) {
			synchronized (machine) {
				if (m_ipAddress == null || m_ipAddress.equals(Constants.ALL)) {
					super.visitMachine(machine);
				} else if (machine.getIp().equals(m_ipAddress)) {
					super.visitMachine(machine);
				}
			}
		}

		@Override
		public void visitName(TransactionName name) {
			if (m_type != null) {
				visitTransactionName(name);
			}
		}

		@Override
		public void visitRange(Range range) {
			if (m_type != null && m_name != null) {
				super.visitRange(range);
			}
		}

		private void visitTransactionName(TransactionName name) {
			super.visitName(name);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			synchronized (transactionReport) {
				super.visitTransactionReport(transactionReport);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (type.getId().equals(m_type)) {
				super.visitType(type);
			}
		}
	}

}
