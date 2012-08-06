package com.dianping.cat.report.page.model.ip;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalIpService extends BaseLocalModelService<IpReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalIpService() {
		super("ip");
	}

	private IpReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "ip");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected IpReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		IpReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long date = Long.parseLong(request.getProperty("date"));

			report = getLocalReport(date, domain);
		}

		return report;
	}
}
