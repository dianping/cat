package com.dianping.cat.report.page.app.processor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.app.Action;
import com.dianping.cat.report.page.app.Model;
import com.dianping.cat.report.page.app.Payload;
import com.dianping.cat.report.page.app.ProblemStatistics;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.site.helper.Splitters;

public class CrashLogProcessor {

	@Inject
	private ReportServiceManager m_reportService;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizer;

	private FieldsInfo buildFeildsInfo(ProblemReport report) {
		FieldsInfo fieldsInfo = new FieldsInfo();
		Set<String> fields = report.getIps();

		for (String field : fields) {
			String[] fs = field.split(":");
			fieldsInfo.addAppVersion(fs[0]);
			fieldsInfo.addPlatVersion(fs[1]);
			fieldsInfo.addModule(fs[2]);
			fieldsInfo.addLevel(fs[3]);
		}
		return fieldsInfo;
	}

	private String queryDomain(Payload payload) {
		String domain = "";

		if (StringUtils.isNotEmpty(payload.getQuery1())) {
			domain = Splitters.by(";").split(payload.getQuery1()).get(0);
		}

		if ("Android".equalsIgnoreCase(domain) || StringUtils.isEmpty(domain)) {
			return "AndroidCrashLog";
		} else if ("iOS".equalsIgnoreCase(domain)) {
			return "iOSCrashLog";
		} else {
			throw new RuntimeException("Unknown crash log domain: " + domain);
		}
	}

	private ProblemReport getHourlyReport(Payload payload, String queryType, String domain) {
		ModelRequest request = new ModelRequest(domain, payload.getDate()).//
		      setProperty("queryType", queryType);

		if (!StringUtils.isEmpty(payload.getType())) {
			request.setProperty("type", "error");
		}
		if (!StringUtils.isEmpty(payload.getStatus())) {
			request.setProperty("name", payload.getStatus());
		}
		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeHelper.ONE_HOUR), ProblemAnalyzer.ID);
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

	public void process(Action action, Payload payload, Model model) {
		m_normalizer.normalize(model, payload);
		ProblemReport report = null;

		switch (action) {
		case HOURLY_CRASH_LOG:
			report = getHourlyReport(payload, "view", queryDomain(payload));
			break;
		case HISTORY_CRASH_LOG:
			report = showSummarizeReport(model, payload, queryDomain(payload));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
		ProblemStatistics problemStatistics = buildProblemStatistics(payload.getQuery1(), report);

		model.setFieldsInfo(buildFeildsInfo(report));
		model.setProblemStatistics(problemStatistics);
		model.setProblemReport(report);
	}

	private ProblemReport showSummarizeReport(Model model, Payload payload, String domain) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		ProblemReport problemReport = m_reportService.queryProblemReport(domain, start, end);

		return problemReport;
	}

	private ProblemStatistics buildProblemStatistics(String query, ProblemReport report) {
		ProblemStatistics problemStatistics = new ProblemStatistics();

		if (StringUtils.isNotEmpty(query)) {
			List<String> querys = Splitters.by(";").split(query);

			problemStatistics.setAppVersions(Splitters.by(":").noEmptyItem().split(querys.get(1)));
			problemStatistics.setPlatformVersions(Splitters.by(":").noEmptyItem().split(querys.get(2)));
			problemStatistics.setModules(Splitters.by(":").noEmptyItem().split(querys.get(3)));
			problemStatistics.setLevels(Splitters.by(":").noEmptyItem().split(querys.get(4)));
		}
		problemStatistics.visitProblemReport(report);

		return problemStatistics;
	}

	public class FieldsInfo {
		private Set<String> m_platVersions = new HashSet<String>();

		private Set<String> m_appVersions = new HashSet<String>();

		private Set<String> m_modules = new HashSet<String>();

		private Set<String> m_levels = new HashSet<String>();

		public void addPlatVersion(String version) {
			m_platVersions.add(version);
		}

		public void addAppVersion(String version) {
			m_appVersions.add(version);
		}

		public void addModule(String module) {
			m_modules.add(module);
		}

		public void addLevel(String level) {
			m_levels.add(level);
		}

		public Set<String> getAppVersions() {
			return m_appVersions;
		}

		public Set<String> getLevels() {
			return m_levels;
		}

		public Set<String> getModules() {
			return m_modules;
		}

		public Set<String> getPlatVersions() {
			return m_platVersions;
		}
	}
}
