package com.dianping.cat.report.page.model.problem;

import java.util.Date;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlParser;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HdfsProblemService implements ModelService<ProblemReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Override
	public ModelResponse<ProblemReport> invoke(ModelRequest request) {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		String path = m_pathBuilder.getReportPath(new Date(date));
		ModelResponse<ProblemReport> response = new ModelResponse<ProblemReport>();
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(path);

			String xml = bucket.findById("problem-" + domain);

			if (xml == null) {
				ProblemReport report = new DefaultXmlParser().parse(xml);

				response.setModel(report);
			}
		} catch (Exception e) {
			response.setException(e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
	}
}
