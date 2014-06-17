package com.dianping.cat.agent.monitor.puppet;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.EnvironmentConfig;
import com.dianping.cat.message.Transaction;

public class PuppetTask implements Task, Initializable {

	@Inject
	private EnvironmentConfig m_environmentConfig;

	private DataSender m_dataSender;

	private AlterationParser m_alterationParser;

	private static final String LOG_FILE = "/var/log/messages";

	private static final int DURATION = 60 * 1000;

	private ReaderManager m_readerManager = new ReaderManager();

	@Override
	public void run() {
		boolean active = true;
		Long end_position = 0L;
		Transaction t = Cat.newTransaction("Puppet", "Task");

		while (active) {
			long current = System.currentTimeMillis();
			Alteration alertation = null;
			Long position = m_readerManager.queryPointer();
			RandomAccessFile reader = null;

			try {
				reader = new RandomAccessFile(LOG_FILE, "r");

				reader.seek(position);
				// 判断日志是否切割了,一定要放在while((line=reader.readLine())!=null)之前，否则回导致反复读取
				if (position >= 2) {
					reader.seek(position - 2);
					try {
						reader.readChar();
						reader.seek(position);
					} catch (IOException e) {
						m_readerManager.updatePointer(0L);
						reader.seek(0L);
						Cat.logError(e);
					}
				}

				String line = null;
				while ((line = reader.readLine()) != null) {
					alertation = m_alterationParser.parse(line);

					if (alertation != null) {
						m_dataSender.send(alertation);
					}
				}
				end_position = reader.getFilePointer();

				if (end_position > position) {
					m_readerManager.updatePointer(end_position);
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (IOException e) {
				Cat.logError("读文件异常:" + LOG_FILE, e);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					Cat.logError(e);
				}

				long duration = System.currentTimeMillis() - current;

				try {
					if (duration < DURATION) {
						Thread.sleep(DURATION - duration);
					}
				} catch (InterruptedException e) {
					active = false;
				}

				t.complete();
			}
		}
	}

	@Override
	public String getName() {
		return "puppet";
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void initialize() throws InitializationException {
		m_dataSender = new DataSender(m_environmentConfig);
		m_alterationParser = new AlterationParser(m_environmentConfig);
		Threads.forGroup("Cat").start(this);
	}
}
