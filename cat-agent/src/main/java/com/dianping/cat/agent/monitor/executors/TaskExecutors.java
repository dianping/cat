package com.dianping.cat.agent.monitor.executors;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.message.Transaction;

public class TaskExecutors extends ContainerHolder implements Task, Initializable {

	private Collection<Executor> m_executors;

	@Inject
	private DataSender m_sender;

	private static final long DURATION = 5 * 1000;

	@Override
	public String getName() {
		return "data-fetcher";
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Executor> map = lookupMap(Executor.class);
		m_executors = map.values();

		Threads.forGroup("Cat").start(this);
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Transaction t = Cat.newTransaction("Data", "Fetch");

			try {
				long current = System.currentTimeMillis();

				for (Executor executor : m_executors) {
					Transaction t2 = Cat.newTransaction("Executor", executor.getId());
					try {
						List<DataEntity> entities = executor.execute();

						m_sender.put(entities);
						t2.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						t2.setStatus(e);
						Cat.logError(e);
					} finally {
						t2.complete();
					}
				}
				t.setStatus(Transaction.SUCCESS);

				long duration = System.currentTimeMillis() - current;

				try {
					if (duration < DURATION) {
						Thread.sleep(DURATION - duration);
					}
				} catch (InterruptedException e) {
					active = false;
				}

			} catch (Exception e) {
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
	}

	@Override
	public void shutdown() {

	}

}
