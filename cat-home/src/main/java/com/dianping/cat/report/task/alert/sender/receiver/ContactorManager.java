package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.task.alert.AlertConstants;

public class ContactorManager implements Initializable {

	@Inject(type = Contactor.class, value = BusinessContactor.ID)
	private Contactor m_businessContactor;

	@Inject(type = Contactor.class, value = NetworkContactor.ID)
	private Contactor m_networkContactor;

	@Inject(type = Contactor.class, value = ExceptionContactor.ID)
	private Contactor m_exceptionContactor;

	@Inject(type = Contactor.class, value = SystemContactor.ID)
	private Contactor m_systemContactor;

	@Inject(type = Contactor.class, value = ThirdpartyContactor.ID)
	private Contactor m_thirdpartyContactor;

	@Inject(type = Contactor.class, value = FrontEndExceptionContactor.ID)
	private Contactor m_frontEndExceptionContactor;

	private Map<String, Contactor> m_contactors = new HashMap<String, Contactor>();

	public List<String> queryReceivers(String group, String channel, String type) {
		Contactor contactor = m_contactors.get(type);

		if (AlertConstants.MAIL.equals(channel)) {
			return contactor.queryEmailContactors(group);
		} else if (AlertConstants.SMS.equals(channel)) {
			return contactor.querySmsContactors(group);
		} else if (AlertConstants.WEIXIN.equals(channel)) {
			return contactor.queryWeiXinContactors(group);
		} else {
			return new ArrayList<String>();
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_contactors.put(m_businessContactor.getId(), m_businessContactor);
		m_contactors.put(m_networkContactor.getId(), m_networkContactor);
		m_contactors.put(m_exceptionContactor.getId(), m_exceptionContactor);
		m_contactors.put(m_systemContactor.getId(), m_systemContactor);
		m_contactors.put(m_thirdpartyContactor.getId(), m_thirdpartyContactor);
		m_contactors.put(m_frontEndExceptionContactor.getId(), m_frontEndExceptionContactor);
	}

	public void setBusinessContactor(Contactor contactor) {
		m_businessContactor = contactor;
	}

	public void setNetworkContactor(Contactor networkContactor) {
		m_networkContactor = networkContactor;
	}

	public void setExceptionContactor(Contactor exceptionContactor) {
		m_exceptionContactor = exceptionContactor;
	}

	public void setSystemContactor(Contactor systemContactor) {
		m_systemContactor = systemContactor;
	}

	public void setThirdpartyContactor(Contactor thirdpartyContactor) {
		m_thirdpartyContactor = thirdpartyContactor;
	}

	public void setFrontEndExceptionContactor(Contactor frontEndExceptionContactor) {
		m_frontEndExceptionContactor = frontEndExceptionContactor;
	}

}
