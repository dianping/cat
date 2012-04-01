package com.dianping.cat.report.page.model.problem;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.site.lookup.annotation.Inject;

public class HistoricalProblemService extends BaseHistoricalModelService<ProblemReport> {
	@Inject
	private ReportDao m_reportDao;

	public HistoricalProblemService() {
		super("problem");
	}

	@Override
	protected ProblemReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(date), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		DefaultMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model = new DefaultXmlParser().parse(xml);

			if (merger == null) {
				merger = new DefaultMerger(model);
			} else {
				model.accept(merger);
			}
		}

		return merger.getProblemReport();
	}
}
