package com.dianping.cat.broker.api.log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class ErrorLogManager {

	public volatile static ErrorLogManager s_manager;

	private BlockingQueue<Entity> m_datas = new LinkedBlockingQueue<Entity>(2000);

	private int m_error = -1;

	private final static String LOG_BASE_PATH = "/data/appdatas/cat/app-error-log";

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	public static ErrorLogManager getInstance() {
		if (s_manager == null) {
			synchronized (ErrorLogManager.class) {
				if (s_manager == null) {
					s_manager = new ErrorLogManager();
					s_manager.initialize();
				}
			}
		}
		return s_manager;
	}

	public void initialize() {
		Threads.forGroup("cat").start(new Writer());
		Threads.forGroup("cat").start(new LogPruner());
	}

	public void offer(byte[] data) {
		String date = m_sdf.format(new Date());
		boolean result = m_datas.offer(new Entity(date, data));

		if (!result) {
			m_error++;

			if (m_error % 100 == 0) {
				Cat.logEvent("WriteError", "Log", Event.SUCCESS, null);
			}
		}
	}

	public static class Entity {
		private String m_file;

		private byte[] m_datas;

		public Entity(String file, byte[] datas) {
			m_file = file;
			m_datas = datas;
		}

		public byte[] getDatas() {
			return m_datas;
		}

		public String getFile() {
			return m_file;
		}

		public void setDatas(byte[] datas) {
			m_datas = datas;
		}

		public void setFile(String file) {
			m_file = file;
		}
	}

	private class LogPruner implements Task {

		private final static long DURATION = TimeHelper.ONE_DAY;

		@Override
		public String getName() {
			return "log-pruner";
		}

		public Date queryPeriod(int months) {
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.MONTH, months);
			return cal.getTime();
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				Date period = queryPeriod(-1);
				String dayStr = m_sdf.format(TimeHelper.getCurrentDay());
				Transaction t = Cat.newTransaction("LogPrune", dayStr);

				try {
					File dir = new File(LOG_BASE_PATH);
					File[] files = dir.listFiles();

					for (File file : files) {
						Date date = m_sdf.parse(file.getName());

						if (date.before(period)) {
							file.delete();
						}
					}
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
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
	
	private class Writer implements Task {

		private LinkedHashMap<String, FileOutputStream> m_outs = new LinkedHashMap<String, FileOutputStream>();

		private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		private void closeLastHour() {
			String lastDate = m_sdf.format(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000L));
			FileOutputStream out = m_outs.remove(lastDate);

			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
			Cat.logEvent("FileWriteMap", String.valueOf(m_outs.size()), Event.SUCCESS, null);
		}

		@Override
		public String getName() {
			return "log-writer";
		}

		@Override
		public void run() {
			while (true) {
				try {
					Entity entity = m_datas.poll(5, TimeUnit.MILLISECONDS);

					if (entity != null) {
						writeLog(entity);
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		@Override
		public void shutdown() {
		}

		private void writeLog(Entity entity) {
			try {
				String fileName = entity.getFile();
				byte[] datas = entity.getDatas();
				FileOutputStream out = m_outs.get(fileName);

				if (out == null) {
					File file = new File(LOG_BASE_PATH, fileName);
					File parentFile = file.getParentFile();

					if (!parentFile.exists()) {
						boolean result = parentFile.mkdirs();

						if (!result) {
							Cat.logError(new RuntimeException("can't create parent file " + parentFile.getAbsolutePath()));
						}
					}
					out = new FileOutputStream(file, true);

					m_outs.put(fileName, out);
					closeLastHour();
				}
				out.write(m_format.format(new Date()).getBytes());
				out.write('\t');
				out.write(datas);
				out.write('\n');
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

}
