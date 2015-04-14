package com.dianping.cat.report.page.storage.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalStorageService extends LocalModelService<StorageReport> {

	public static final String ID = StorageAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String id, ApiPayload payload) throws Exception {
		StorageReport report = super.getReport(period, id);

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, id);
		}

		String ipAddress = payload.getIpAddress();

		return new StorageReportFilter(ipAddress).buildXml(report);
	}

	private StorageReport getReportFromLocalDisk(long timestamp, String id) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, StorageAnalyzer.ID);
			String xml = bucket.findById(id);
			StorageReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new StorageReport(id);
				report.setStartTime(new Date(timestamp));
				report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));
				report.getIds().addAll(bucket.getIds());
			}
			return report;

		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class StorageReportFilter extends com.dianping.cat.consumer.storage.model.transform.DefaultXmlBuilder {

		private String m_ipAddress;

		public StorageReportFilter(String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_ipAddress = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.storage.model.entity.Machine machine) {
			if (StringUtils.isEmpty(m_ipAddress) || m_ipAddress.equals(Constants.ALL)) {
				super.visitMachine(machine);
			} else if (machine.getId().equals(m_ipAddress)) {
				super.visitMachine(machine);
			}
		}
	}
}
