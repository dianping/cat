package com.dianping.cat.report.page.ip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.Period;
import com.dianping.cat.consumer.ip.model.transform.BaseVisitor;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.ip.Model.DisplayModel;
import com.dianping.cat.report.page.ip.location.IPSeekerManager;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject(type = ModelService.class, value = "ip")
	private ModelService<IpReport> m_service;

	private List<DisplayModel> getDisplayModels(IpReport report) {
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		Map<String, DisplayModel> models = new HashMap<String, DisplayModel>();
		DisplayModelBuilder builder = new DisplayModelBuilder(models, minute);

		if (report != null) {
			report.accept(builder); // prepare display model
		}

		List<DisplayModel> displayModels = new ArrayList<DisplayModel>(models.values());

		Collections.sort(displayModels, new Comparator<DisplayModel>() {
			@Override
			public int compare(DisplayModel m1, DisplayModel m2) {
				return m2.getLastFifteen() - m1.getLastFifteen(); // desc order
			}
		});

		if (displayModels.size() > 100) {
			return displayModels.subList(0, 100);
		} else {
			return displayModels;
		}
	}

	private int getHour(long date) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	private IpReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<IpReport> response = m_service.invoke(request);
			IpReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "ip")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "ip")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		model.setAction(payload.getAction());
		model.setPage(ReportPage.IP);

		switch (payload.getAction()) {
		case VIEW:
			showReport(model, payload);
			break;
		case MOBILE:
			showReport(model, payload);
			Gson gson = new Gson();
			MobileModel mobileModel = new MobileModel().setDisplayModels(model.getDisplayModels());
			model.setMobileResponse(gson.toJson(mobileModel));
			break;
		case MOBILE_IP:
			String ip = payload.getIp();
			String location = IPSeekerManager.getLocation(ip);
			model.setMobileResponse(location);
			break;
		}
		if (payload.getPeriod().isCurrent()) {
			model.setCreatTime(new Date());
		} else {
			model.setCreatTime(new Date(payload.getDate() + 60 * 60 * 1000 - 1000));
		}
		m_jspViewer.view(ctx, model);
	}

	private void showReport(Model model, Payload payload) {
		try {
			ModelPeriod period = payload.getPeriod();
			IpReport report = getReport(payload);

			if (period.isFuture()) {
				model.setLongDate(payload.getCurrentDate());
			} else {
				model.setLongDate(payload.getDate());
			}

			List<DisplayModel> displayModels = getDisplayModels(report);

			model.setHour(getHour(model.getLongDate()));
			model.setDisplayModels(displayModels);
			model.setDisplayDomain(payload.getDomain());
			model.setReport(report);
		} catch (Throwable e) {
			Cat.logError(e);
			model.setException(e);
		}
	}

	static class DisplayModelBuilder extends BaseVisitor {
		private int m_minute;

		private Map<String, DisplayModel> m_models;

		private Period m_period;

		public DisplayModelBuilder(Map<String, DisplayModel> models, int minute) {
			m_models = models;
			m_minute = minute;
		}

		@Override
		public void visitIp(Ip ip) {
			String address = ip.getAddress();
			DisplayModel model = m_models.get(address);

			if (model == null) {
				model = new DisplayModel(address);
				m_models.put(address, model);
			}

			model.process(m_minute, m_period.getMinute(), ip.getCount());
		}

		@Override
		public void visitPeriod(Period period) {
			m_period = period;
			super.visitPeriod(period);
		}
	}
}
