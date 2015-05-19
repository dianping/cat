package com.dianping.cat.report.service;

public interface ModelService<M> {

	public static final int ANALYZER_COUNT = 2;

	public String getName();

	public ModelResponse<M> invoke(ModelRequest request);

	public boolean isEligable(ModelRequest request);
}
