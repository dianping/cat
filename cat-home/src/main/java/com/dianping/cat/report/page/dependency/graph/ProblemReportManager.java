package com.dianping.cat.report.page.dependency.graph;

import java.util.HashMap;
import java.util.Map;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.model.spi.ModelService;

public class ProblemReportManager {

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_service;

	private Map<String, String> m_exceptionInfos = new HashMap<String, String>();

	public class Reload implements Task {

		@Override
		public void run() {

		}

		@Override
		public String getName() {
			return "ExceptionInfoReload";
		}

		@Override
		public void shutdown() {
		}

	}

}
