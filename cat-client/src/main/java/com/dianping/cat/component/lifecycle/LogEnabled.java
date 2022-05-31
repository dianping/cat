package com.dianping.cat.component.lifecycle;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.component.Logger;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface LogEnabled {
	public void enableLogging(Logger logger);
}
