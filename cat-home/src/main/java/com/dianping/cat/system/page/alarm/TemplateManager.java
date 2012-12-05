package com.dianping.cat.system.page.alarm;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.alarm.AlarmTemplate;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateEntity;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

public class TemplateManager {

	@Inject
	private AlarmTemplateDao m_alarmTemplateDao;

	public void queryTemplateByName(Payload payload, Model model) {
		String name = payload.getTemplateName();

		if (name == null || name.length() == 0) {
			name = "exception";
		}
		//for page show
		if (name.equals("exception")) {
			model.setTemplateIndex(9);
		} else if (name.equals("service")) {
			model.setTemplateIndex(10);
		}
		try {
			AlarmTemplate entity = m_alarmTemplateDao.findAlarmTemplateByName(name, AlarmTemplateEntity.READSET_FULL);

			model.setAlarmTemplate(entity);
		} catch (DalNotFoundException nfe) {
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public void templateAddSubmit(Payload payload, Model model) {
		String name = payload.getTemplateName();
		String content = payload.getContent();

		AlarmTemplate entity = m_alarmTemplateDao.createLocal();
		entity.setContent(content);
		entity.setName(name);

		try {
			m_alarmTemplateDao.insert(entity);
			model.setOpState(Handler.SUCCESS);
		} catch (Exception e) {
			model.setOpState(Handler.FAIL);
		}
	}

	public void templateUpdate(Payload payload, Model model) {
		int id = payload.getAlarmTemplateId();

		try {
			AlarmTemplate alarmTemplate = m_alarmTemplateDao.findByPK(id, AlarmTemplateEntity.READSET_FULL);
			model.setAlarmTemplate(alarmTemplate);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public void templateUpdateSubmit(Payload payload, Model model) {
		int id = payload.getAlarmTemplateId();
		String content = payload.getContent();
		String name = payload.getTemplateName();
		AlarmTemplate entity = m_alarmTemplateDao.createLocal();

		entity.setContent(content);
		entity.setName(name);
		entity.setId(id);
		entity.setKeyId(id);
		try {
			m_alarmTemplateDao.updateByPK(entity, AlarmTemplateEntity.UPDATESET_FULL);
			model.setOpState(Handler.SUCCESS);
		} catch (Exception e) {
			model.setOpState(Handler.FAIL);
		}
	}

}
