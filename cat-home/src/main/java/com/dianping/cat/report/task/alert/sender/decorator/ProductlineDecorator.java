package com.dianping.cat.report.task.alert.sender.decorator;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.site.lookup.util.StringUtils;

public abstract class ProductlineDecorator extends DefaultDecorator {

	@Inject
	protected ProductLineConfigManager m_manager;

	public String buildContactInfo(String domainName) {
		try {
			ProductLine product = m_manager.queryProductLine(domainName);
			String owners = product.getOwner();
			String phones = product.getPhone();
			StringBuilder builder = new StringBuilder();

			if (!StringUtils.isEmpty(owners)) {
				builder.append("[业务负责人: ").append(owners).append(" ]");
			}
			if (!StringUtils.isEmpty(phones)) {
				builder.append("[负责人手机号码: ").append(phones).append(" ]");
			}

			return builder.toString();
		} catch (Exception ex) {
			Cat.logError("build productline contact info error for doamin: " + domainName, ex);
		}

		return "";
	}
}
