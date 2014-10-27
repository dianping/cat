package com.dianping.cat.broker.api.log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;

public class LogManager {

	public volatile static LogManager s_manager;

	private BlockingQueue<Entity> m_datas = new LinkedBlockingQueue<Entity>(2000);

	private int m_error = -1;

	public static LogManager getInstance() {
		if (s_manager == null) {
			synchronized (LogManager.class) {
				if (s_manager == null) {
					s_manager = new LogManager();
					s_manager.initialize();
				}
			}
		}
		return s_manager;
	}

	public void initialize() {
		Threads.forGroup("cat").start(new Writer());
	}

	public void offer(byte[] data) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		String date = sdf.format(new Date());
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

	private class Writer implements Task {

		private String m_baseDir = "/data/appdatas/cat/app-error-log";

		private LinkedHashMap<String, FileOutputStream> m_outs = new LinkedHashMap<String, FileOutputStream>();

		private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		private void closeLastHour() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
			String lastDate = sdf.format(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000L));
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
			return "writer-log";
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
					File file = new File(m_baseDir, fileName);
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
