package com.dianping.cat.component.lifecycle;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.component.ComponentContext;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface Initializable {
	public void initialize(ComponentContext ctx);
}
