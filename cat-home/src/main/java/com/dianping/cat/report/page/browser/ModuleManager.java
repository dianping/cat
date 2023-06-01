package com.dianping.cat.report.page.browser;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.web.JsErrorLogDao;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager implements Initializable {

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	private List<String> m_modules;

	public List<String> getModules() {
		return m_modules;
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("cat").start(new ModuleReloader());
	}

	public class ModuleReloader implements Task {

		private final static long DURATION = TimeHelper.ONE_HOUR;

		private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd.HH");

		@Override
		public String getName() {
			return "Module-Reloader";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				long current = System.currentTimeMillis();
				String hourStr = m_sdf.format(TimeHelper.getCurrentHour());

				Transaction t = Cat.newTransaction("ModuleReloader", hourStr);

				try {
					List<String> moduleList = new ArrayList<String>();

					// no need for finding web monitor
					//List<JsErrorLog> result = m_jsErrorLogDao.findModules(JsErrorLogEntity.READSET_DISTINCT_MODULES);
					//
					//for (JsErrorLog log : result) {
					//	moduleList.add(log.getModules());
					//}

					m_modules = moduleList;
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
