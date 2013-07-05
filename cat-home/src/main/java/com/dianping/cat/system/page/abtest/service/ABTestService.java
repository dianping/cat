package com.dianping.cat.system.page.abtest.service;

import com.dianping.cat.home.dal.abtest.Abtest;

public interface ABTestService {

	public Abtest getABTestNameByRunId(int id);
}
