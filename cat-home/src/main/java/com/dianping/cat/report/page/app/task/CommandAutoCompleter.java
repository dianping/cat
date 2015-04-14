package com.dianping.cat.report.page.app.task;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.site.lookup.util.StringUtils;

public class CommandAutoCompleter {

	@Inject
	private AppConfigManager m_configManager;

	@Inject
	private TransactionReportService m_reportService;

	private static String SERVER = "warp-connection-server";

	public void autoCompleteDomain(Date period) {
		Collection<Command> commands = m_configManager.getRawCommands().values();
		Date end = new Date(period.getTime() + TimeHelper.ONE_DAY);
		Set<String> domains = m_reportService.queryAllDomainNames(period, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			try {
				TransactionReportVisitor visitor = new TransactionReportVisitor();
				TransactionReport report = m_reportService.queryDailyReport(domain, period, end);

				visitor.visitTransactionReport(report);

				Map<String, String> urlToDomains = visitor.getUrlToDomains();

				for (Command command : commands) {
					String commandDomain = command.getDomain();

					if (StringUtils.isEmpty(commandDomain) || SERVER.equals(commandDomain)) {
						String commandUrl = command.getName();

						for (Entry<String, String> entry : urlToDomains.entrySet()) {
							if (entry.getKey().endsWith(commandUrl)) {
								command.setDomain(entry.getValue());
							}
						}
					}
				}
				m_configManager.storeConfig();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	public static class TransactionReportVisitor extends BaseVisitor {

		private String m_domain;

		private Map<String, String> m_urlToDomains = new LinkedHashMap<String, String>();

		public Map<String, String> getUrlToDomains() {
			return m_urlToDomains;
		}

		@Override
		public void visitName(TransactionName name) {
			String id = name.getId();

			m_urlToDomains.put(id, m_domain);
		}

		@Override
		public void visitType(TransactionType type) {
			if ("URL".equals(type.getId())) {
				super.visitType(type);
			}
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			m_domain = transactionReport.getDomain();

			if (!SERVER.equals(m_domain)) {
				super.visitTransactionReport(transactionReport);
			}
		}
	}

}
