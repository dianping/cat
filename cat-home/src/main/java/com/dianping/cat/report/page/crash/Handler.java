package com.dianping.cat.report.page.crash;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.crash.entity.App;
import com.dianping.cat.home.crash.entity.CrashReport;
import com.dianping.cat.home.crash.entity.Module;
import com.dianping.cat.home.crash.entity.Version;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.crash.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.crash.display.CrashLogDisplayInfo;
import com.dianping.cat.report.page.crash.display.CrashReportSorter;
import com.dianping.cat.report.page.crash.display.DisplayVersion;
import com.dianping.cat.report.page.crash.service.CrashLogQueryEntity;
import com.dianping.cat.report.page.crash.service.CrashLogService;
import com.dianping.cat.report.page.crash.service.CrashStatisticReportService;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private CrashLogService m_crashLogService;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Inject
	private CrashStatisticReportService m_statisticsService;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	private void buildAppCrashGraph(Payload payload, Model model) {
		CrashLogQueryEntity entity = payload.getCrashLogQuery();
		CrashLogDisplayInfo info = m_crashLogService.buildCrashGraph(entity);

		model.setCrashLogDisplayInfo(info);
	}

	private CrashLogDisplayInfo buildAppCrashLog(Payload payload) {
		CrashLogQueryEntity entity = payload.getCrashLogQuery();
		CrashLogDisplayInfo info = m_crashLogService.buildCrashLogDisplayInfo(entity);

		return info;
	}

	private void buildAppCrashLogDetail(Payload payload, Model model) {
		CrashLogDetailInfo info = m_crashLogService.queryCrashLogDetailInfo(payload.getId());

		model.setCrashLogDetailInfo(info);
	}

	private void buildAppCrashTrend(Payload payload, Model model) {
		CrashLogDisplayInfo info = m_crashLogService.buildCrashTrend(payload.getCrashLogTrendQuery1(),
		      payload.getCrashLogTrendQuery2());
		model.setCrashLogDisplayInfo(info);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "crash")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "crash")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.CRASH);

		switch (action) {
		case APP_CRASH_LOG:
			CrashLogDisplayInfo displayInfo = buildAppCrashLog(payload);

			model.setCrashLogDisplayInfo(displayInfo);
			break;
		case APP_CRASH_LOG_JSON:
			displayInfo = buildAppCrashLog(payload);

			model.setFetchData(m_jsonBuilder.toJson(displayInfo));
			break;
		case APP_CRASH_LOG_DETAIL:
			buildAppCrashLogDetail(payload, model);
			break;
		case APP_CRASH_GRAPH:
			buildAppCrashGraph(payload, model);
			break;
		case APP_CRASH_TREND:
			buildAppCrashTrend(payload, model);
			break;
		case CRASH_STATISTICS:
			buildCrashStatistics(payload, model);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void buildCrashStatistics(Payload payload, Model model) {
		model.setAppNames(m_mobileConfigManager.queryApps());
		int appId = payload.getAppId();
		Date start = payload.getDayDate();
		int platform = payload.getPlatform();

		CrashReport report = m_statisticsService.queryDailyReport(appId, start, null);
		CrashReport yesterdayReport = m_statisticsService.queryDailyReport(appId, TimeHelper.addDays(start, -1), null);
		CrashReport lastWeekReport = m_statisticsService.queryDailyReport(appId, TimeHelper.addDays(start, -7), null);

		App app = report.getApps().get(platform);
		App yesterdayApp = yesterdayReport.getApps().get(platform);
		App lastWeekApp = lastWeekReport.getApps().get(platform);

		List<DisplayVersion> versions = new LinkedList<DisplayVersion>();

		if (app != null) {
			for (Version version : app.getVersions().values()) {
				DisplayVersion displayVersion = new DisplayVersion();
				String id = version.getId();
				int crashCount = version.getCrashCount();
				double percent = version.getPercent();

				displayVersion.setId(id);
				displayVersion.setCrashCount(crashCount);
				displayVersion.setDau(version.getDau());
				displayVersion.setPercent(percent);

				List<Module> modules = new LinkedList<Module>(version.getModules().values());

				Collections.sort(modules, new Comparator<Module>() {

					@Override
					public int compare(Module o1, Module o2) {
						return o2.getCrashCount() - o1.getCrashCount();
					}

				});

				displayVersion.setModules(modules);

				if (yesterdayApp != null) {
					Version yesterday = yesterdayApp.findVersion(id);

					if (yesterday != null) {
						int yesterdayCount = yesterday.getCrashCount();
						double yesterdayPercent = yesterday.getPercent();

						displayVersion.setCrashCountMoM(yesterdayCount == 0 ? 0 : crashCount * 100.0 / yesterdayCount);
						displayVersion.setPercentMoM(yesterdayPercent == 0 ? 0 : percent * 100.0 / yesterdayPercent);
					}
				}

				if (lastWeekApp != null) {
					Version lastweek = lastWeekApp.findVersion(id);

					if (lastweek != null) {
						int lastweekCount = lastweek.getCrashCount();
						double lastweekPercent = lastweek.getPercent();

						displayVersion.setCrashCountYoY(lastweekCount == 0 ? 0 : crashCount * 100.0 / lastweekCount);
						displayVersion.setPercentYoY(lastweekPercent == 0 ? 0 : percent * 100.0 / lastweekPercent);
					}
				}

				versions.add(displayVersion);
			}
		}

		Collections.sort(versions, new CrashReportSorter(payload.getSort()));
		model.setVersions(versions);
	}
}
