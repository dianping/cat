package com.dianping.cat.broker.api.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.broker.api.app.proto.AppDataProto;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.data.warehouse.IMarinLog;
import com.dianping.data.warehouse.LogTypeEnum;
import com.dianping.data.warehouse.MarinLog;
import com.dianping.data.warehouse.MarinPrinter;
import com.site.lookup.util.StringUtils;

public class AppLogManager implements Initializable {

	private BlockingQueue<AppDataProto> m_datas = new LinkedBlockingQueue<AppDataProto>(2000);

	private MarinPrinter m_marinPrinter;

	private final static String FILE_NAME_PREFIX = Constants.APP;

	@Override
	public void initialize() throws InitializationException {
		m_marinPrinter = new MarinPrinter();
		m_marinPrinter.setFileName(FILE_NAME_PREFIX);
		m_marinPrinter.setBusiness(Constants.BROKER_SERVICE);
		m_marinPrinter.setType(LogTypeEnum.FILE);
		m_marinPrinter.init();

		Threads.forGroup("cat").start(new StoreManager());
		Threads.forGroup("cat").start(new LogPruner());
	}

	public boolean offer(AppDataProto proto) {
		return m_datas.offer(proto);
	}

	public class StoreManager implements Task {

		private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
						IMarinLog log = new MarinLog();

						log.putLong("event_id", UUID.randomUUID().getMostSignificantBits());
						String dpid = proto.getDpid();

						if (StringUtils.isNotEmpty(dpid)) {
							try {
								log.putInt("dp_user_id", Integer.parseInt(dpid));
							} catch (Exception e) {
								// ignore
							}
						}

						log.putString("user_ip", proto.getIp());
						log.putString("request_start_time", m_sdf.format(new Date(proto.getTimestamp())));

						log.putString("path/method", proto.getCommandStr());
						log.putInt("response_code", proto.getCode());
						log.putInt("eclapse_time", proto.getResponseTime());
						log.putString("client_version", String.valueOf(proto.getVersion()));

						log.putString("requestbyte", String.valueOf(proto.getRequestByte()));
						log.putString("responsebyte", String.valueOf(proto.getResponseByte()));
						log.putInt("tunel", proto.getConnectType());
						log.putInt("network", proto.getNetwork());
						log.putInt("platform", proto.getPlatform());
						log.putString("province", proto.getCityStr());
						log.putString("operator", proto.getOperatorStr());

						m_marinPrinter.print(log);
					}
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		@Override
		public void shutdown() {
		}

	}

	private class LogPruner implements Task {

		private final static long DURATION = TimeHelper.ONE_HOUR;

		private final static String LOG_BASE_PATH = "/data/applogs/broker-service/logs/";

		private final static String FILENAME = FILE_NAME_PREFIX + ".log";

		private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd.HH");

		@Override
		public String getName() {
			return "app-log-pruner";
		}

		public Date queryPeriod(int days) {
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.HOUR_OF_DAY, days);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.MONTH, 0);
			return cal.getTime();
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				Date period = queryPeriod(-1);
				String hourStr = m_sdf.format(TimeHelper.getCurrentHour());
				Transaction t = Cat.newTransaction("AppLogPrune", hourStr);

				try {
					File dir = new File(LOG_BASE_PATH);
					File[] files = dir.listFiles();

					for (File file : files) {
						String name = file.getName();
						String[] fields = name.split(FILENAME + ".");

						if (fields.length > 1) {
							String dateStr = fields[1];
							Date date = m_sdf.parse(dateStr);

							if (date.before(period)) {
								file.delete();
							}
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
}
