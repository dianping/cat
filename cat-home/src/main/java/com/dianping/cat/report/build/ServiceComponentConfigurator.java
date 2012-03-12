package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.report.page.model.logview.CompositeLogViewService;
import com.dianping.cat.report.page.model.logview.LocalLogViewService;
import com.dianping.cat.report.page.model.problem.CompositeProblemService;
import com.dianping.cat.report.page.model.problem.LocalProblemService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.CompositeTransactionService;
import com.dianping.cat.report.page.model.transaction.HdfsTransactionService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

class ServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ModelService.class, "transaction-local", LocalTransactionService.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "transaction-hdfs", HdfsTransactionService.class) //
		      .req(BucketManager.class, MessagePathBuilder.class));
		all.add(C(ModelService.class, "transaction", CompositeTransactionService.class) //
		      .req(ModelService.class, new String[] { "transaction-local", "transaction-hdfs" }, "m_services"));

		all.add(C(ModelService.class, "problem-local", LocalProblemService.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "problem", CompositeProblemService.class) //
		      .req(ModelService.class, new String[] { "problem-local" }, "m_services"));

		all.add(C(ModelService.class, "logview-local", LocalLogViewService.class) //
		      .req(MessagePathBuilder.class));
		all.add(C(ModelService.class, "logview", CompositeLogViewService.class) //
		      .req(ModelService.class, new String[] { "logview-local" }, "m_services"));

		return all;
	}
}
