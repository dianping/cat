package com.dianping.cat.report.page.model.problem;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;

public class LocalProblemService extends BaseLocalModelService<ProblemReport> {
	public LocalProblemService() {
		super("problem");
	}
}
