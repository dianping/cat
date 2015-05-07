package com.dianping.cat.report.page.app.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.page.app.Action;
import com.dianping.cat.report.page.app.Model;
import com.dianping.cat.report.page.app.Payload;
import com.dianping.cat.report.page.app.ProblemStatistics;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class CrashLogProcessor {

	@Inject
	private ProblemReportService m_reportService;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizer;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private String APP_VERSIONS = "appVersions";

	private String LEVELS = "levels";

	private String MODULES = "modules";

	private String PLATFORM_VERSIONS = "platformVersions";

	private FieldsInfo buildFeildsInfo(ProblemReport report) {
		FieldsInfo fieldsInfo = new FieldsInfo();
		Set<String> fields = report.getIps();
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();

		for (String field : fields) {
			String[] fs = field.split(":");
			findOrCreate(APP_VERSIONS, fieldsMap).add(fs[0]);
			findOrCreate(PLATFORM_VERSIONS, fieldsMap).add(fs[1]);
			findOrCreate(MODULES, fieldsMap).add(fs[2]);
			findOrCreate(LEVELS, fieldsMap).add(fs[3]);

		}
		if (!fieldsMap.isEmpty()) {
			sortFields(fieldsMap, fieldsInfo);
		}
		return fieldsInfo;
	}

	private ProblemStatistics buildProblemStatistics(String query, ProblemReport report) {
		ProblemStatistics problemStatistics = new ProblemStatistics();

		if (StringUtils.isNotEmpty(query)) {
			List<String> querys = Splitters.by(";").split(query);

			if (querys.size() == 5) {
				problemStatistics.setAppVersions(Splitters.by(":").noEmptyItem().split(querys.get(1)));
				problemStatistics.setPlatformVersions(Splitters.by(":").noEmptyItem().split(querys.get(2)));
				problemStatistics.setModules(Splitters.by(":").noEmptyItem().split(querys.get(3)));
				problemStatistics.setLevels(Splitters.by(":").noEmptyItem().split(querys.get(4)));
			}
		}
		problemStatistics.visitProblemReport(report);

		return problemStatistics;
	}

	private Set<String> findOrCreate(String key, Map<String, Set<String>> map) {
		Set<String> value = map.get(key);

		if (value == null) {
			value = new HashSet<String>();
			map.put(key, value);
		}
		return value;
	}

	private ProblemReport getHourlyReport(Payload payload, String domain) {
		ModelRequest request = new ModelRequest(domain, payload.getDate()).//
		      setProperty("queryType", "view");

		if (!StringUtils.isEmpty(payload.getType())) {
			request.setProperty("type", "error");
		}
		if (!StringUtils.isEmpty(payload.getStatus())) {
			request.setProperty("name", payload.getStatus());
		}
		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

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
			report = getHourlyReport(payload, queryDomain(payload));
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
		model.setCrashLogDomains(m_serverFilterConfigManager.getCrashLogDomains().values());
	}

	private String queryDomain(Payload payload) {
		Set<String> crashLogDomains = m_serverFilterConfigManager.getCrashLogDomains().keySet();
		String domain = "";

		if (StringUtils.isNotEmpty(payload.getQuery1())) {
			domain = Splitters.by(";").split(payload.getQuery1()).get(0);
		}

		if (StringUtils.isEmpty(domain)) {
			return crashLogDomains.iterator().next();
		} else if (crashLogDomains.contains(domain)) {
			return domain;
		} else {
			throw new RuntimeException("Unknown crash log domain: " + domain);
		}
	}

	private ProblemReport showSummarizeReport(Model model, Payload payload, String domain) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		ProblemReport problemReport = m_reportService.queryReport(domain, start, end);

		return problemReport;
	}

	private void sortFields(Map<String, Set<String>> fieldsMap, FieldsInfo fieldsInfo) {

		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s2.compareTo(s1);
			}
		};
		List<String> v = new ArrayList<String>(fieldsMap.get(APP_VERSIONS));
		List<String> l = new ArrayList<String>(fieldsMap.get(LEVELS));
		List<String> m = new ArrayList<String>(fieldsMap.get(MODULES));
		List<String> p = new ArrayList<String>(fieldsMap.get(PLATFORM_VERSIONS));

		Collections.sort(v, comparator);
		Collections.sort(p, comparator);
		Collections.sort(m);
		Collections.sort(l);
		fieldsInfo.setAppVersions(v).setPlatVersions(p).setModules(m).setLevels(l);
	}

	public class FieldsInfo {

		private List<String> m_platVersions;

		private List<String> m_appVersions;

		private List<String> m_modules;

		private List<String> m_levels;

		public List<String> getAppVersions() {
			return m_appVersions;
		}

		public List<String> getLevels() {
			return m_levels;
		}

		public List<String> getModules() {
			return m_modules;
		}

		public List<String> getPlatVersions() {
			return m_platVersions;
		}

		public FieldsInfo setAppVersions(List<String> appVersions) {
			m_appVersions = appVersions;
			return this;
		}

		public FieldsInfo setLevels(List<String> levels) {
			m_levels = levels;
			return this;
		}

		public FieldsInfo setModules(List<String> modules) {
			m_modules = modules;
			return this;
		}

		public FieldsInfo setPlatVersions(List<String> platVersions) {
			m_platVersions = platVersions;
			return this;
		}
	}
}
