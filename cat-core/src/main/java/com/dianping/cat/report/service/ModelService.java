package com.dianping.cat.report.service;

public interface ModelService<M> {

	public String getName();

	public ModelResponse<M> invoke(ModelRequest request);

	public boolean isEligable(ModelRequest request);
}
