package com.dianping.cat.report.page.storage.transform;

import java.util.Set;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.BaseVisitor;

public class StorageOperationFilter extends BaseVisitor {

	private StorageReport m_storageReport;

	private String m_currentMachine;

	private String m_currentOperation;

	private String m_currentDomain;

	private Set<String> m_operations;

	public StorageOperationFilter(Set<String> ops) {
		m_operations = ops;
	}

	public StorageReport getStorageReport() {
		return m_storageReport;
	}

	private void mergeOperation(Operation operation) {
		Operation to = m_storageReport.findOrCreateMachine(m_currentMachine).findOrCreateDomain(m_currentDomain)
		      .findOrCreateOperation(m_currentOperation);

		to.setCount(to.getCount() + operation.getCount());
		to.setLongCount(to.getLongCount() + operation.getLongCount());
		to.setError(to.getError() + operation.getError());
		to.setSum(to.getSum() + operation.getSum());
		to.setAvg(to.getCount() > 0 ? to.getSum() / to.getCount() : 0);
	}

	private void mergeSegment(Segment segment) {
		Segment to = m_storageReport.findOrCreateMachine(m_currentMachine).findOrCreateDomain(m_currentDomain)
		      .findOrCreateOperation(m_currentOperation).findOrCreateSegment(segment.getId());

		to.setCount(to.getCount() + segment.getCount());
		to.setLongCount(to.getLongCount() + segment.getLongCount());
		to.setError(to.getError() + segment.getError());
		to.setSum(to.getSum() + segment.getSum());
		to.setAvg(to.getCount() > 0 ? to.getSum() / to.getCount() : 0);
	}

	@Override
	public void visitDomain(Domain domain) {
		m_currentDomain = domain.getId();
		super.visitDomain(domain);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentMachine = machine.getId();
		super.visitMachine(machine);
	}

	@Override
	public void visitOperation(Operation operation) {
		if (m_operations.contains(operation.getId())) {
			m_currentOperation = operation.getId();

			m_storageReport.getOps().add(m_currentOperation);
			mergeOperation(operation);
			super.visitOperation(operation);
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		mergeSegment(segment);
		super.visitSegment(segment);
	}

	@Override
	public void visitStorageReport(StorageReport storageReport) {
		m_storageReport = new StorageReport(storageReport.getId());

		m_storageReport.setName(storageReport.getName()).setType(storageReport.getType())
		      .setStartTime(storageReport.getStartTime()).setEndTime(storageReport.getEndTime());
		m_storageReport.getIds().addAll(storageReport.getIds());
		m_storageReport.getIps().addAll(storageReport.getIps());
		super.visitStorageReport(storageReport);
	}

}
