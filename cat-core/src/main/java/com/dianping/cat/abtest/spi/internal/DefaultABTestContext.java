package com.dianping.cat.abtest.spi.internal;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class DefaultABTestContext implements ABTestContext {
	private String m_groupName = DEFAULT_GROUP;

	private ABTestEntity m_entity;

	private HttpServletRequest m_req;

	private ABTestGroupStrategy m_groupStrategy;

	private boolean m_initialized;

	public DefaultABTestContext(ABTestEntity entity) {
		m_entity = entity;
	}

	@Override
	public String getGroupName() {
		if (!m_initialized) {
			initialize(new Date());
		}

		return m_groupName;
	}

	public void initialize(Date timestamp) {
		if (!m_initialized) {
			if (m_entity.isEligible(timestamp)) {
				Transaction t = Cat.newTransaction("GroupStrategy", m_entity.getGroupStrategyName());

				try {
					m_groupStrategy.apply(this);

					t.setStatus(Message.SUCCESS);
				} catch (Throwable e) {
					t.setStatus(e);
					Cat.logError(e);
				} finally {
					t.complete();
				}
			}

			m_initialized = true;
		}
	}

	@Override
	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public void setup(HttpServletRequest req) {
		m_req = req;
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return m_req;
	}

	@Override
	public ABTestEntity getEntity() {
		return m_entity;
	}

	public void setGroupStrategy(ABTestGroupStrategy groupStrategy) {
		m_groupStrategy = groupStrategy;
	}
}
