package com.dianping.cat.report.page.heartbeat.service;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalHeartbeatService extends LocalModelService<HeartbeatReport> {

	public static final String ID = HeartbeatAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	private String filterReport(ApiPayload payload, HeartbeatReport report) {
		String ipAddress = payload.getIpAddress();

		if (StringUtils.isEmpty(ipAddress)) {
			Set<String> ips = report.getIps();
			if (ips.size() > 0) {
				ipAddress = SortHelper.sortIpAddress(ips).get(0);
			}
		}
		HeartBeatReportFilter filter = new HeartBeatReportFilter(ipAddress);

		return filter.buildXml(report);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		HeartbeatReport report = super.getReport(period, domain);

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}

		return filterReport(payload, report);
	}

	private HeartbeatReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, HeartbeatAnalyzer.ID);
			String xml = bucket.findById(domain);
			HeartbeatReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new HeartbeatReport(domain);
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

	public static class HeartBeatReportFilter extends
	      com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder {
		private String m_ip;

		public HeartBeatReportFilter(String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_ip = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.heartbeat.model.entity.Machine machine) {
			if (machine.getIp().equals(m_ip) || StringUtils.isEmpty(m_ip) || Constants.ALL.equals(m_ip)) {
				super.visitMachine(machine);
			}
		}
	}
}
