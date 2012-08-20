package com.dianping.cat.report.page.cache;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TransactionReportVistor extends BaseVisitor {

	private CacheReport m_cacheReport = new CacheReport();

	private EventReport m_eventReport;

	private String m_currentIp;

	private String m_currentType;

	private String m_type;

	private Set<String> m_cacheTypes = new HashSet<String>();

	private String m_sortBy = "missed";

	public CacheReport getCacheReport() {
		return m_cacheReport;
	}

	public TransactionReportVistor setType(String type) {
		m_type = type;
		return this;
	}

	public TransactionReportVistor setEventReport(EventReport eventReport) {
		m_eventReport = eventReport;
		return this;
	}

	public void setSortBy(String sortBy) {
		if(sortBy!=null){
			m_sortBy  = sortBy;
		}
   }

	@Override
	public void visitName(TransactionName transactionName) {
		com.dianping.cat.consumer.event.model.entity.Machine machine = m_eventReport.findOrCreateMachine(m_currentIp);
		EventType eventType = machine.findOrCreateType(m_currentType);
		EventName eventName = eventType.findOrCreateName(transactionName.getId() + ":missed");
		m_cacheReport.addNewNameItem(transactionName, eventName);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();
		super.visitMachine(machine);
	}

	@Override
	public void visitType(TransactionType transactionType) {
		String id = transactionType.getId();
		if (m_cacheTypes.contains(id)) {
			if (StringUtils.isEmpty(m_type)) {
				m_currentType = transactionType.getId();
				com.dianping.cat.consumer.event.model.entity.Machine machine = m_eventReport
				      .findOrCreateMachine(m_currentIp);
				EventType eventType = machine.findOrCreateType(m_currentType);
				m_cacheReport.addNewTypeItem(transactionType, eventType);

				super.visitType(transactionType);
			} else if (id.equalsIgnoreCase(m_type)) {
				m_currentType = transactionType.getId();
				super.visitType(transactionType);
			}
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_cacheTypes.add("Cache.memcached");
		m_cacheTypes.add("Cache.web");
		m_cacheTypes.add("Cache.kvdb");
		m_cacheReport.setSortBy(m_sortBy);
		
		super.visitTransactionReport(transactionReport);
		m_cacheReport.setDomain(transactionReport.getDomain());
		m_cacheReport.setDomainNames(transactionReport.getDomainNames());
		m_cacheReport.setStartTime(transactionReport.getStartTime());
		m_cacheReport.setEndTime(transactionReport.getEndTime());
		m_cacheReport.setIps(transactionReport.getIps());
	}
	
}
