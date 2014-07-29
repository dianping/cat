package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.List;

public interface Contactor {

	public String getId();

	public List<String> queryEmailContactors();

	public List<String> queryWeiXinContactors();

	public List<String> querySmsContactors();

	public void setModule(String id);
}
