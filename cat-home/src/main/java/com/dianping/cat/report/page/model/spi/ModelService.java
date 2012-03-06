package com.dianping.cat.report.page.model.spi;

public interface ModelService<M> {
	public boolean isEligable(ModelRequest request);

	public ModelResponse<M> invoke(ModelRequest request);
}
