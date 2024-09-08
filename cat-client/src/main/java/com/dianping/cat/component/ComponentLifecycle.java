package com.dianping.cat.component;

import com.dianping.cat.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface ComponentLifecycle {
	public void onStart(Object component);

	public void onStop(Object component);
}
