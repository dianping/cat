package com.dianping.cat.system.alarm;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.template.entity.Duration;
import com.dianping.cat.system.alarm.alert.AlertInfo;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;
import com.dianping.cat.system.notify.ReportRenderImpl;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AlarmContentBuilder implements  Initializable {

	public Configuration m_configuration;
	
	@Override
	public void initialize() throws InitializationException {
		m_configuration = new Configuration();

		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(ReportRenderImpl.class, "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public String buildAlarmTitle(ThresholdAlarmMeta meta) {
		String type = meta.getType();

		if (type.equalsIgnoreCase(AlertInfo.EXCEPTION)) {
			return CatString.EXCEPTION + "[ " + String.valueOf(meta.getDomain()) + " ]";
		} else if (type.equalsIgnoreCase(AlertInfo.SERVICE)) {
			return CatString.SERVICE + "[ " + String.valueOf(meta.getDomain()) + " ]";
		}

		return "Default";
	}
	
	public String buildEmailAlarmContent(ThresholdAlarmMeta meta) {
		Map<Object, Object> root = new HashMap<Object, Object>();
		StringWriter sw = new StringWriter(5000);

		root.put("rule", buildRuleMeta(meta.getDuration()));
		root.put("count", meta.getRealCount());
		root.put("domain", meta.getDomain());
		root.put("date", meta.getDate());
		root.put("url", buildProblemUrl(meta.getBaseShowUrl(), meta.getDomain(), meta.getDate()));

		try {
			String type = meta.getType();

			if (type.equalsIgnoreCase(AlertInfo.EXCEPTION)) {
				Template t = m_configuration.getTemplate("exceptionAlarm.ftl");

				t.process(root, sw);
			} else if (type.equalsIgnoreCase(AlertInfo.SERVICE)) {
				Template t = m_configuration.getTemplate("serviceAlarm.ftl");

				t.process(root, sw);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
	}

	private String buildProblemUrl(String baseUrl, String domain, Date date) {
		long time = date.getTime();

		time = time - time % TimeUtil.ONE_HOUR;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");

		baseUrl = baseUrl.replaceAll("dashboard", "p");
		StringBuilder sb = new StringBuilder(baseUrl);

		sb.append("?").append("domain=").append(domain).append("&date=").append(sdf.format(new Date(time)));
		return sb.toString();
	}

	private Object buildRuleMeta(Duration duration) {
		StringBuilder sb = new StringBuilder(100);

		sb.append("[ Interval:").append(duration.getInterval()).append(";");
		sb.append(" Min:").append(duration.getMin()).append(";");
		sb.append(" Max:").append(duration.getMax()).append("]");
		return sb.toString();
	}

}
