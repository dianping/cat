package com.dianping.cat.report.page.transaction.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class DistributionDetailVisitor extends BaseVisitor {

	private String m_type;

	private String m_name;

	private String m_ip;

	private List<DistributionDetail> m_details = new ArrayList<DistributionDetail>();

	public DistributionDetailVisitor(String type, String name) {
		m_type = type;
		m_name = name;
	}

	public List<DistributionDetail> getDetails() {
		Collections.sort(m_details, new Comparator<DistributionDetail>() {

			@Override
			public int compare(DistributionDetail o1, DistributionDetail o2) {
				long gap = o2.getTotalCount() - o1.getTotalCount();

				if (gap > 0) {
					return 1;
				} else if (gap < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return m_details;
	}

	@Override
	public void visitMachine(Machine machine) {
		if (!Constants.ALL.equals(machine.getIp())) {
			m_ip = machine.getIp();

			super.visitMachine(machine);
		}
	}

	@Override
	public void visitName(TransactionName name) {
		if (m_name.equals(name.getId())) {
			DistributionDetail detail = new DistributionDetail();

			detail.setTotalCount(name.getTotalCount()).setFailCount(name.getFailCount())
			      .setFailPercent(name.getFailPercent()).setIp(m_ip).setAvg(name.getAvg()).setMin(name.getMin())
			      .setMax(name.getMax()).setStd(name.getStd());
			m_details.add(detail);
		}
	}

	@Override
	public void visitType(TransactionType type) {
		if (m_type != null && m_type.equals(type.getId())) {
			if (StringUtils.isEmpty(m_name)) {
				DistributionDetail detail = new DistributionDetail();

				detail.setTotalCount(type.getTotalCount()).setFailCount(type.getFailCount())
				      .setFailPercent(type.getFailPercent()).setIp(m_ip).setAvg(type.getAvg()).setMin(type.getMin())
				      .setMax(type.getMax()).setStd(type.getStd());
				m_details.add(detail);
			} else {
				super.visitType(type);
			}
		}
	}

	public class DistributionDetail {

		private String m_ip;

		private long m_totalCount;

		private long m_failCount;

		private double m_failPercent;

		private double m_min;

		private double m_max;

		private double m_avg;

		private double m_std;

		public double getAvg() {
			return m_avg;
		}

		public long getFailCount() {
			return m_failCount;
		}

		public double getFailPercent() {
			return m_failPercent;
		}

		public String getIp() {
			return m_ip;
		}

		public double getMax() {
			return m_max;
		}

		public double getMin() {
			return m_min;
		}

		public double getStd() {
			return m_std;
		}

		public long getTotalCount() {
			return m_totalCount;
		}

		public DistributionDetail setAvg(double avg) {
			m_avg = avg;
			return this;
		}

		public DistributionDetail setFailCount(long failCount) {
			m_failCount = failCount;
			return this;
		}

		public DistributionDetail setFailPercent(double failPercent) {
			m_failPercent = failPercent;
			return this;
		}

		public DistributionDetail setIp(String ip) {
			m_ip = ip;
			return this;
		}

		public DistributionDetail setMax(double max) {
			m_max = max;
			return this;
		}

		public DistributionDetail setMin(double min) {
			m_min = min;
			return this;
		}

		public DistributionDetail setStd(double std) {
			m_std = std;
			return this;
		}

		public DistributionDetail setTotalCount(long totalCount) {
			m_totalCount = totalCount;
			return this;
		}

	}
}
