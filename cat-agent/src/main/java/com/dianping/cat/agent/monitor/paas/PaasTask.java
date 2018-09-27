package com.dianping.cat.agent.monitor.paas;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.message.Transaction;

public class PaasTask implements Task, Initializable {

	@Inject
	private DataSender m_dataSender;

	@Inject
	private DataBuilder m_dataBuilder;

	private static final int DURATION = 5 * 1000;

	@Override
	public String getName() {
		return "paas-task";
	}

	@Override
	public void initialize() throws InitializationException {
		String agent = System.getProperty("agent", "executors");

		if ("paas".equalsIgnoreCase(agent)) {
			Threads.forGroup("cat").start(this);
		}
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("Agent", "Paas");

			try {
				List<String> instances = m_dataBuilder.queryInstances();
				for (String instance : instances) {
					List<DataEntity> entities = m_dataBuilder.buildData(instance);

					m_dataSender.put(entities);
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}

			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {

	}

}
