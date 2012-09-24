package com.dianping.dog.alarm.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.xml.sax.SAXException;

import com.dianping.cat.dog.home.template.model.entity.Duration;
import com.dianping.cat.dog.home.template.model.entity.Param;
import com.dianping.cat.dog.home.template.model.entity.ThresholdTemplate;
import com.dianping.cat.dog.home.template.model.transform.DefaultDomParser;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.dal.Ruleinstance;
import com.dianping.dog.dal.Ruletemplate;

public class ProblemRuleLoader implements RuleLoader, LogEnabled {
	private Logger m_logger;

	private volatile ThresholdTemplate cashTemplateObj;

	private volatile Ruletemplate cashTemplate;
 
	@Override
	public RuleEntity loadRuleEntity(Ruleinstance instance, Ruletemplate template) {
		try {
			ThresholdTemplate thresholdTemplate = getTemplateObjFromCash(template);
			if (thresholdTemplate == null) {
				m_logger.error(String.format("fail to deserialize template content[%s]", template.getContent()));
				return null;
			}
			ThresholdTemplate instanceTemplate = deSerizlizeFromXml(instance.getContent());
			if (instanceTemplate == null) {
				m_logger.error(String.format("fail to deserialize ruleInstance content[%s]", instance.getContent()));
				return null;
			}
			ThresholdTemplate mergedTemplate = mergeTemplate(thresholdTemplate, instanceTemplate);
			RuleEntity entity = convertToRuleEntity(mergedTemplate,instance);
			return entity;
		} catch (Exception e) {
			m_logger.error(String.format("fail to convert template to RuleEntity  [%s]", e.getMessage()));
			return null;
		}
	}

	private ThresholdTemplate getTemplateObjFromCash(Ruletemplate template) {
		if (cashTemplate == null || cashTemplate.getLastModifiedDate().getTime() != template.getLastModifiedDate().getTime()) {
			cashTemplate = template;
			cashTemplateObj = deSerizlizeFromXml(template.getContent());
		}
		return cashTemplateObj;
	}

	private ThresholdTemplate deSerizlizeFromXml(String content) {
		DefaultDomParser parser = new DefaultDomParser();
		ThresholdTemplate template = null;
		try {
			template = parser.parse(content);
			return template;
		} catch (Exception e) {
			m_logger.error(e.getMessage());
			return null;
		}
	}

	private RuleEntity convertToRuleEntity(ThresholdTemplate template,Ruleinstance instance) {
		RuleEntity entity = new RuleEntity();
		Map<String, Param> paramMap = template.getConnection().getParams();

		entity.setBaseUrl(template.getConnection().getBaseUrl());
		entity.setDomain(paramMap.get("domain").getValue());
		//entity.setIp(paramMap.get("ip").getValue());
		entity.setName(paramMap.get("name").getValue());
		entity.setPeriod(template.getThreshold().getPeriod());
		entity.setReport(paramMap.get("report").getValue());
		entity.setRuleType(RuleType.Exception);
		entity.setType(paramMap.get("type").getValue());
		entity.setGmtModified(instance.getLastModifiedDate());
		entity.setId(instance.getId());
		Map<String, Duration> durationMap = template.getThreshold().getDurations();
		List<Duration> durationEntityList = new ArrayList<Duration>(durationMap.values());
		for (Duration durationEntity : durationEntityList) {
			com.dianping.dog.alarm.entity.Duration duration = new com.dianping.dog.alarm.entity.Duration();
			String alarmStr = durationEntity.getAlarm();
			String[] alarmArray = alarmStr.split(",");
			for (String alarm : alarmArray) {
				if (alarm.equals("EMAIL")) {
					duration.addAlarmType(AlarmType.EMAIL);
				} else if (alarm.equals("SMS")) {
					duration.addAlarmType(AlarmType.SMS);
				}
			}
			duration.setId(durationEntity.getId());
			duration.setInterval(durationEntity.getInterval());
			duration.setMax(durationEntity.getMax());
			duration.setMin(durationEntity.getMin());
			entity.addDuration(duration);
		}
		return entity;
	}

	private ThresholdTemplate mergeTemplate(final ThresholdTemplate template, final ThresholdTemplate instance)
	      throws SAXException, IOException {
		TemplateMerger merger = new TemplateMerger(new ThresholdTemplate());
		template.accept(merger);
		instance.accept(merger);
		return merger.getThresholdTemplate();

	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

}
