package com.dianping.cat.broker.api.log;

import java.text.SimpleDateFormat;
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
import com.dianping.cat.broker.api.app.proto.AppDataProto;
import com.dianping.data.warehouse.IMarinLog;
import com.dianping.data.warehouse.LogTypeEnum;
import com.dianping.data.warehouse.MarinLog;
import com.dianping.data.warehouse.MarinPrinter;

public class AppLogManager implements Initializable {

	private BlockingQueue<AppDataProto> m_datas = new LinkedBlockingQueue<AppDataProto>(2000);

	private MarinPrinter m_marinPrinter;

	@Override
	public void initialize() throws InitializationException {
		m_marinPrinter = new MarinPrinter();
		m_marinPrinter.setFileName("app");
		m_marinPrinter.setBusiness("broker-service");
		m_marinPrinter.setType(LogTypeEnum.FILE);
		m_marinPrinter.init();

		Threads.forGroup("cat").start(new StoreManager());
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
						log.putInt("dp_user_id", Integer.parseInt(proto.getDpid()));
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
}
