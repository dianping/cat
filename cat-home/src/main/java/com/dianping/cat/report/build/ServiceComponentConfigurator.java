package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.report.page.model.failure.CompositeFailureModelService;
import com.dianping.cat.report.page.model.failure.LocalFailureModelService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.CompositeTransactionModelService;
import com.dianping.cat.report.page.model.transaction.HdfsTransactionModelService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionModelService;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class ServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ModelService.class, "transaction-local", LocalTransactionModelService.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "transaction-hdfs", HdfsTransactionModelService.class) //
				.req(BucketManager.class, MessagePathBuilder.class));
		all.add(C(ModelService.class, "transaction", CompositeTransactionModelService.class) //
		      .req(ModelService.class, new String[] { "transaction-local", "transaction-hdfs" }, "m_services"));

		all.add(C(ModelService.class, "failure-local", LocalFailureModelService.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "failure", CompositeFailureModelService.class) //
		      .req(ModelService.class, new String[] { "failure-local" }, "m_services"));

		return all;
	}
}
