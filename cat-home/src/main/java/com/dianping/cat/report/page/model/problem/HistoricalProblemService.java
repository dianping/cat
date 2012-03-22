package com.dianping.cat.report.page.model.problem;

import java.util.Date;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalProblemService extends BaseHistoricalModelService<ProblemReport> {
	@Inject
	private BucketManager m_bucketManager;

	public HistoricalProblemService() {
		super("problem");
	}

	@Override
	protected ProblemReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(new Date(date), getName(), "remote");

			String xml = bucket.findById(domain);

			if (xml != null) {
				ProblemReport report = new DefaultXmlParser().parse(xml);

				return report;
			} else {
				return null;
			}
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
