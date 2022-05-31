package com.dianping.cat.component;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.component.lifecycle.Initializable;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface ComponentLifecycle extends Initializable {
	public void onStart(Object component);

	public void onStop(Object component);
}
