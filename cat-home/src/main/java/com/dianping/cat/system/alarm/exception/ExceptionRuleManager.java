package com.dianping.cat.system.alarm.exception;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.AlarmRule;
import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleEntity;
import com.dianping.cat.home.dal.alarm.AlarmTemplate;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateEntity;
import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.home.template.transform.DefaultSaxParser;
import com.dianping.cat.system.alarm.template.TemplateMerger;
import com.dianping.cat.system.alarm.template.ThresholdRule;
import com.site.lookup.annotation.Inject;

public class ExceptionRuleManager implements Initializable {

	public List<ThresholdRule> m_rules;

	@Inject
	private AlarmTemplateDao m_alarmTemplateDao;

	@Inject
	private AlarmRuleDao m_alarmRuleDao;

	public List<ThresholdRule> getAllExceptionRules() {
		return m_rules;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			AlarmTemplate alarmTemplate = m_alarmTemplateDao.findAlarmTemplateByName("exception",
			      AlarmTemplateEntity.READSET_FULL);
			int templateId = alarmTemplate.getId();
			String content = alarmTemplate.getContent();
			ThresholdTemplate baseTemplate = DefaultSaxParser.parse(content);

			List<AlarmRule> exceptionRules = m_alarmRuleDao.findAllAlarmRuleByTemplateId(templateId,
			      AlarmRuleEntity.READSET_FULL);

			for (AlarmRule rule : exceptionRules) {
				try {
					String newContent = rule.getContent();
					ThresholdTemplate newTemplate = DefaultSaxParser.parse(newContent);
					TemplateMerger merger = new TemplateMerger(new ThresholdTemplate());

					baseTemplate.accept(merger);
					newTemplate.accept(merger);

					@SuppressWarnings("unused")
               ThresholdTemplate template = merger.getThresholdTemplate();

				} catch (Exception e) {
					Cat.logError(e);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
