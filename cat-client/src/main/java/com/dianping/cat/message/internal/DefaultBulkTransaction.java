package com.dianping.cat.message.internal;

import com.dianping.cat.message.BulkTransaction;
import com.dianping.cat.message.context.TraceContext;

public class DefaultBulkTransaction extends DefaultTransaction implements BulkTransaction {
	private int m_success;

	private int m_failed;

	private long m_sum;

	public DefaultBulkTransaction(TraceContext ctx, String type, String name) {
		super(ctx, type, name);
	}

	@Override
	public DefaultBulkTransaction addDuration(int success, int failed, long sumOfDurationInMillis) {
		m_success += success;
		m_failed += failed;
		m_sum += sumOfDurationInMillis;
		return this;
	}

	@Override
	public CharSequence getData() {
		if (m_success + m_failed > 0) {
			super.addData("@", m_success + "," + m_failed + "," + m_sum);
		}

		return super.getData();
	}
}
