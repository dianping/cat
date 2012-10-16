package com.dianping.cat.system.notify.render;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;

public class TransactionRender extends BaseVisitor {

	private Date m_date;

	private String m_dateStr;

	private String m_domain;

	private Map<Object, Object> m_result = new HashMap<Object, Object>();

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	private int m_totalDays;

	private String m_currentIp;

	private String m_host;

	private String m_transactionLink = "http://%s/cat/r/t?op=history&domain=%s&date=%s&reportType=day";

	private String m_typeGraphLink = "http://%s/cat/r/t?op=historyGraph&domain=%s&date=%s&ip=All&reportType=day&type=%s";

	private List<Type> m_types = new ArrayList<Type>();

	public TransactionRender(Date date, String domain, int day) {
		m_domain = domain;
		m_date = date;
		m_dateStr = m_sdf.format(date);
		m_totalDays = day;

		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		if (ip.startsWith("10.")) {
			m_host = CatString.ONLINE;
		} else {
			m_host = CatString.OFFLINE;
		}
	}

	private String buildGraphUrl(TransactionType type) {
		return String.format(m_typeGraphLink, m_host, m_domain, m_dateStr, type.getId());
	}

	private String buildTransactionUrl(Date date) {
		String dateStr = m_sdf.format(m_date);

		return String.format(m_transactionLink, m_host, m_domain, dateStr);
	}

	public Map<Object, Object> getRenderResult() {
		return m_result;
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();
		super.visitMachine(machine);
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);

		Date lastDay = new Date(m_date.getTime() - TimeUtil.ONE_DAY);
		Date lastWeek = new Date(m_date.getTime() - 7 * TimeUtil.ONE_DAY);
		String currentUrl = buildTransactionUrl(m_date);
		String lastDayUrl = buildTransactionUrl(lastDay);
		String lastWeekUrl = buildTransactionUrl(lastWeek);

		m_result.put("current", currentUrl);
		m_result.put("lastDay", lastDayUrl);
		m_result.put("lastWeek", lastWeekUrl);
		m_result.put("types", m_types);
	}

	@Override
	public void visitType(TransactionType type) {
		if (m_currentIp.equals(CatString.ALL_IP)) {
			Type temp = new Type();

			type.setTps(type.getTotalCount() / (double) TimeUtil.ONE_DAY / m_totalDays);
			temp.setType(type);
			temp.setUrl(buildGraphUrl(type));
			m_types.add(temp);
		}
	}

	public static class Type {
		private TransactionType m_type;

		private String m_url;

		public TransactionType getType() {
			return m_type;
		}

		public String getUrl() {
			return m_url;
		}

		public void setType(TransactionType type) {
			m_type = type;
		}

		public void setUrl(String url) {
			m_url = url;
		}
	}

}
