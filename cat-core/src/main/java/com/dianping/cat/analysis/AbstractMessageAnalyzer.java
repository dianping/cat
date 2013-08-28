package com.dianping.cat.analysis;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.Constants;

public abstract class AbstractMessageAnalyzer<R> extends ContainerHolder implements MessageAnalyzer {
	public static final long MINUTE = 60 * 1000L;

	public static final long ONE_HOUR = 60 * 60 * 1000L;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	private long m_extraTime;

	protected long m_startTime;

	protected long m_duration;

	protected Logger m_logger;

	private long m_errors = 0;

	private volatile boolean m_active = true;

	protected static final String ALL = "All";

	protected static Set<String> UNUSED_TYPES = new HashSet<String>();

	protected static Set<String> UNUSED_NAMES = new HashSet<String>();

	static {
		UNUSED_TYPES.add("Service");
		UNUSED_TYPES.add("PigeonService");
		UNUSED_NAMES.add("piegonService:heartTaskService:heartBeat");
		UNUSED_NAMES.add("piegonService:heartTaskService:heartBeat()");
		UNUSED_NAMES.add("pigeon:HeartBeatService:null");
	}

	@Override
	public void analyze(MessageQueue queue) {
		while (!isTimeout() && isActive()) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				try {
					process(tree);
				} catch (Throwable e) {
					m_errors++;

					if (m_errors == 1 || m_errors % 10000 == 0) {
						Cat.logError(e);
					}
				}
			}
		}

		while (true) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				try {
					process(tree);
				} catch (Throwable e) {
					m_errors++;

					if (m_errors == 1 || m_errors % 10000 == 0) {
						Cat.logError(e);
					}
				}
			} else {
				break;
			}
		}
	}

	@Override
	public void destroy() {
		super.release(this);
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		// override it
	}

	protected long getExtraTime() {
		return m_extraTime;
	}

	public abstract R getReport(String domain);

	@Override
	public long getStartTime() {
		return m_startTime;
	}

	@Override
	public void initialize(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	protected boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	protected boolean isLocalMode() {
		ServerConfigManager manager = lookup(ServerConfigManager.class);

		return manager.isLocalMode();
	}

	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	protected void loadReports() {
		// to be overridden
	}

	protected abstract void process(MessageTree tree);
	
	protected boolean shouldDiscard(Transaction t) {
		// pigeon default heartbeat is no use
		String type = t.getType();
		String name = t.getName();

		if (UNUSED_TYPES.contains(type) && UNUSED_NAMES.contains(name)) {
			return true;
		}
		return false;
	}

	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}
	
	public boolean validate(String domain) {
		return !domain.equals("PhoenixAgent") && !domain.equals(Constants.FRONT_END);
	}
	
}
