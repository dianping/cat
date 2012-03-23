package com.dianping.cat.report.page.model.ip;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;

public class LocalIpService extends BaseLocalModelService<IpReport> {
	public LocalIpService() {
		super("ip");
	}
}
