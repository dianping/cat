package com.dianping.cat.broker.api.log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.broker.api.app.proto.AppDataProto;

public class AppLogManager implements Initializable {

	private BlockingQueue<AppDataProto> m_datas = new LinkedBlockingQueue<AppDataProto>(2000);

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("cat").start(new StoreManager());
	}

	public boolean offer(AppDataProto proto) {
		return m_datas.offer(proto);
	}

	public class StoreManager implements Task {

		@Override
		public String getName() {
			return "store-app-log";
		}

		@Override
		public void run() {
			while (true) {
				try {
					AppDataProto proto = m_datas.poll(5, TimeUnit.MICROSECONDS);

					if (proto != null) {
						// TODO save it
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		@Override
		public void shutdown() {
		}

	}
}
