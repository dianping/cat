package com.dianping.cat.report.page.model.spi;

import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;

public interface ModelService<M> {
	public String getName();

	public ModelResponse<M> invoke(ModelRequest request);

	public boolean isEligable(ModelRequest request);
}
