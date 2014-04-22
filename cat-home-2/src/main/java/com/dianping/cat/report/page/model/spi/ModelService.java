package com.dianping.cat.report.page.model.spi;

import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public interface ModelService<M> {
	public String getName();

	public ModelResponse<M> invoke(ModelRequest request);

	public boolean isEligable(ModelRequest request);
}
