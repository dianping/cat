package com.dianping.cat.report.page.alert;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.site.helper.Splitters;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private SenderManager m_senderManager;

	@Inject
	private AlertDao m_alertDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alert")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "alert")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case ALERT:
			List<String> receivers = Splitters.by(",").noEmptyItem().split(payload.getReceivers());
			if (receivers == null || receivers.size() == 0) {
				setAlertResult(model, 0);
			} else {
				AlertMessageEntity message = new AlertMessageEntity(payload.getGroup(), payload.getTitle(),
				      payload.getType(), payload.getContent(), receivers);

				try {
					boolean result = m_senderManager.sendAlert(AlertChannel.findByName(payload.getChannel()), message);
					if (result) {
						setAlertResult(model, 1);
					} else {
						setAlertResult(model, 2);
					}
				} catch (NullPointerException ex) {
					setAlertResult(model, 3);
				}
			}
			break;
		case VIEW:
			Date startTime = payload.getStartTime();
			Date endTime = payload.getEndTime();
			String domain = payload.getDomain();
			String metric = payload.getMetric();
			String level = payload.getLevel();
			List<Alert> alerts;
			try {
				alerts = m_alertDao.queryAlertsByTimeDomainMetricType(startTime, endTime, domain, metric, level,
				      AlertEntity.READSET_FULL);
			} catch (DalException e) {
				alerts = new ArrayList<Alert>();
				Cat.logError(e);
			}
			model.setAlerts(generateAlertMap(alerts));
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALERT);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private Map<String, List<Alert>> generateAlertMap(List<Alert> alerts) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Map<String, List<Alert>> map = new LinkedHashMap<String, List<Alert>>();

		for (Alert alert : alerts) {
			String time = format.format(alert.getAlertTime());
			List<Alert> alertsInMinute = map.get(time);
			if (alertsInMinute == null) {
				alertsInMinute = new ArrayList<Alert>();
				map.put(time, alertsInMinute);
			}

			alertsInMinute.add(alert);
		}

		return map;
	}

	private void setAlertResult(Model model, int status) {
		switch (status) {
		case 0:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"lack receivers\"}");
			break;
		case 1:
			model.setAlertResult("{\"status\":200}");
			break;
		case 2:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"send failed, please retry again\"}");
			break;
		case 3:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"send failed, please check your channel argument\"}");
			break;
		}
	}

}
