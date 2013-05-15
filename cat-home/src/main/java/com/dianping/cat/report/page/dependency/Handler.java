package com.dianping.cat.report.page.dependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.report.page.model.dependency.DependencyReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private EventCollectManager m_manager;

	@Inject(type = ModelService.class, value = "dependency")
	private ModelService<DependencyReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	private DependencyReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<DependencyReport> response = m_service.invoke(request);
			DependencyReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable dependency service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "dependency")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "dependency")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);

		DependencyReport report = getReport(payload);
		String min = payload.getMinute();
		Set<Integer> keys = report.getSegments().keySet();
		int minute = 0;
		
		List<Integer> minutes = new ArrayList<Integer>(keys);
		int maxMinute=0;
		if (minutes.size() > 0) {
			maxMinute =minutes.get(minutes.size() - 1);
		}
		Collections.sort(minutes);
		if (StringUtils.isEmpty(min)) {
			if (minutes.size() > 0) {
				min = String.valueOf(minutes.get(minutes.size() - 1));
				minute = Integer.parseInt(min);
				maxMinute = minute;
			}
		} else {
			minute = Integer.parseInt(min);
		}

		Date time = new Date(payload.getDate() + TimeUtil.ONE_MINUTE * minute);
		Segment segment = report.findSegment(minute);
		
		minutes = new ArrayList<Integer>();
		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}

		model.setMinute(minute);
		model.setMaxMinute(maxMinute);
		model.setMinutes(minutes);
		model.setReport(report);
		model.setSegment(segment);

		if (payload.isAll()) {
			model.setSegment(buildAllSegment(report));
		}
		model.setEvents(queryDependencyEvent(segment, payload.getDomain(), time));

		m_jspViewer.view(ctx, model);
	}

	private Segment buildAllSegment(DependencyReport report) {
		Segment result = new Segment();
		Map<Integer, Segment> segments = report.getSegments();
		DependencyReportMerger merger = new DependencyReportMerger(null);

		for (Segment segment : segments.values()) {
			Map<String, Dependency> dependencies = segment.getDependencies();
			Map<String, Index> indexs = segment.getIndexs();

			for (Index index : indexs.values()) {
				Index temp = result.findOrCreateIndex(index.getName());
				merger.mergeIndex(temp, index);
			}
			for (Dependency dependency : dependencies.values()) {
				Dependency temp = result.findOrCreateDependency(dependency.getKey());

				merger.mergeDependency(temp, dependency);
			}
		}
		return result;
	}

	private Map<String, List<Event>> queryDependencyEvent(Segment segment, String domain, Date date) {
		Map<String, List<Event>> result = new LinkedHashMap<String, List<Event>>();
		Map<String, List<String>> dependencies = parseDependencies(segment);
		List<Event> domainEvents = m_manager.queryEvents(domain, date);

		if (domainEvents != null && domainEvents.size() > 0) {
			result.put(domain, domainEvents);
		}
		for (Entry<String, List<String>> entry : dependencies.entrySet()) {
			String key = entry.getKey();
			List<String> targets = entry.getValue();

			for (String temp : targets) {
				List<Event> queryEvents = m_manager.queryEvents(temp, date);

				if (queryEvents != null && queryEvents.size() > 0) {
					List<Event> events = result.get(key);

					if (events == null) {
						events = new ArrayList<Event>();
						result.put(key, events);
					}
					events.addAll(queryEvents);
				}
			}
		}
		return result;
	}

	private Map<String, List<String>> parseDependencies(Segment segment) {
		Map<String, List<String>> results = new TreeMap<String, List<String>>();

		if (segment != null) {
			Map<String, Dependency> dependencies = segment.getDependencies();

			for (Dependency temp : dependencies.values()) {
				String type = temp.getType();
				String target = temp.getTarget();
				List<String> targets = results.get(type);

				if (targets == null) {
					targets = new ArrayList<String>();
					results.put(type, targets);
				}
				targets.add(target);
			}
		}

		return results;
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.DEPENDENCY);
		model.setAction(Action.VIEW);

		m_normalizePayload.normalize(model, payload);
	}

}
