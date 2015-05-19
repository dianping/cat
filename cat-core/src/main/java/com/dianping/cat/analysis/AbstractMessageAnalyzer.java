package com.dianping.cat.analysis;

import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;

public abstract class AbstractMessageAnalyzer<R> extends ContainerHolder implements MessageAnalyzer {
	public static final long MINUTE = 60 * 1000L;

	public static final long ONE_HOUR = 60 * 60 * 1000L;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	private long m_extraTime;

	protected long m_startTime;

	protected long m_duration;

	protected Logger m_logger;

	private long m_errors = 0;

	private volatile boolean m_active = true;

	protected int m_index;

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
		ReportManager<?> manager = this.getReportManager();

		if (manager != null) {
			manager.destory();
		}
	}

	@Override
	public abstract void doCheckpoint(boolean atEnd);

	@Override
	public int getAnanlyzerCount() {
		return 1;
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
		return m_serverConfigManager.isLocalMode();
	}

	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	protected abstract void loadReports();

	protected abstract void process(MessageTree tree);

	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}

	public void setIndex(int index) {
		m_index = index;
	}

}
