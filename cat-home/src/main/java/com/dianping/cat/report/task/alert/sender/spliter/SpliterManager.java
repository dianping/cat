package com.dianping.cat.report.task.alert.sender.spliter;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

public class SpliterManager implements Initializable {

	@Inject(type = Spliter.class, value = MailSpliter.ID)
	private Spliter m_mailSpliter;

	@Inject(type = Spliter.class, value = SmsSpliter.ID)
	private Spliter m_smsSpliter;

	@Inject(type = Spliter.class, value = WeixinSpliter.ID)
	private Spliter m_weixinSpliter;

	private Map<String, Spliter> m_spliters = new HashMap<String, Spliter>();

	@Override
	public void initialize() throws InitializationException {
		m_spliters.put(m_mailSpliter.getID(), m_mailSpliter);
		m_spliters.put(m_smsSpliter.getID(), m_smsSpliter);
		m_spliters.put(m_weixinSpliter.getID(), m_weixinSpliter);
	}

	public String process(String content, String channelName) {
		Spliter splitter = m_spliters.get(channelName);
		return splitter.process(content);
	}

	public void setMailSpliter(Spliter mailSpliter) {
		m_mailSpliter = mailSpliter;
	}

	public void setSmsSpliter(Spliter smsSpliter) {
		m_smsSpliter = smsSpliter;
	}

	public void setWeixinSpliter(Spliter weixinSpliter) {
		m_weixinSpliter = weixinSpliter;
	}

}
