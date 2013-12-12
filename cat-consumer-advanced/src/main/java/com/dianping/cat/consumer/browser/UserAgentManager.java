package com.dianping.cat.consumer.browser;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import com.dianping.cat.consumer.advanced.dal.UserAgent;

public interface UserAgentManager extends Initializable {

	public UserAgent parse(String userAgentString);

	public void storeResult();

}
