package com.dianping.cat.consumer.build;

import static com.dianping.cat.consumer.problem.ProblemType.FAILURE;
import static com.dianping.cat.consumer.problem.ProblemType.LONG_URL;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.DefaultAnalyzerFactory;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.handler.FailureHandler;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.handler.LongUrlHandler;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(AnalyzerFactory.class, DefaultAnalyzerFactory.class));

		all.add(C(MessageConsumer.class, "realtime", RealtimeConsumer.class) //
		      .req(AnalyzerFactory.class) //
		      .config(E("extraTime").value(property("extraTime", "300000"))//
		            , E("analyzers").value("problem,transaction,event,ip")));

		String failureTypes = "Error,RuntimeException,Exception";

		all.add(C(Handler.class, FAILURE.getName(), FailureHandler.class)//
		      .config(E("failureType").value(failureTypes)));

		all.add(C(Handler.class, LONG_URL.getName(), LongUrlHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(Handler.class, new String[] { FAILURE.getName(), LONG_URL.getName() }, "m_handlers") //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(EventAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, ReportDao.class));

		all.add(C(IpAnalyzer.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
