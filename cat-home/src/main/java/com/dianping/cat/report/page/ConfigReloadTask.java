package com.dianping.cat.report.page;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.BlackListManager;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

public class ConfigReloadTask implements Task {

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private RouterConfigManager m_routerConfigManager;

	@Inject
	private BlackListManager m_blackListManager;

	@Inject
	private AllReportConfigManager m_allTransactionConfigManager;

	@Override
	public String getName() {
		return "Config-Reload";
	}

	@Override
	public void run() {
		boolean active = true;
		while (active) {
			try {
				m_productLineConfigManager.refreshConfig();
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				m_metricConfigManager.refreshConfig();
			} catch (Exception e) {
				Cat.logError(e);
			}

			Transaction t = Cat.newTransaction("ReloadConfig", "router");
			try {
				m_routerConfigManager.refreshConfig();
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
			}

			try {
				m_blackListManager.refreshConfig();
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				m_allTransactionConfigManager.refreshConfig();
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(TimeHelper.ONE_MINUTE);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
