package com.dianping.cat.report.page.model.ip;

import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultMerger;

public class IpReportMerger extends DefaultMerger {
	public IpReportMerger(IpReport ipReport) {
		super(ipReport);
	}

	@Override
	protected void mergeIp(Ip old, Ip ip) {
		old.setCount(old.getCount() + ip.getCount());
	}
}
