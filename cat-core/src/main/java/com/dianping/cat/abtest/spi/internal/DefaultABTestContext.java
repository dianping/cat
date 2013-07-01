package com.dianping.cat.abtest.spi.internal;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class DefaultABTestContext implements ABTestContext {
	private String m_groupName = DEFAULT_GROUP;

	private ABTestEntity m_entity;

	private HttpServletRequest m_request;

	private HttpServletResponse m_response;

	private ABTestGroupStrategy m_groupStrategy;

	private Map<String, String> m_cookielets;

	public DefaultABTestContext(ABTestEntity entity) {
		m_entity = entity;
	}

	@Override
	public String getCookielet(String name) {
		if (m_cookielets != null) {
			return m_cookielets.get(name);
		} else {
			return null;
		}
	}

	public Map<String, String> getCookielets() {
		return m_cookielets;
	}

	@Override
	public ABTestEntity getEntity() {
		return m_entity;
	}

	@Override
	public String getGroupName() {
		return m_groupName;
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return m_request;
	}

	@Override
	public HttpServletResponse getHttpServletResponse() {
		return m_response;
	}

	@Override
	public void setCookielet(String name, String value) {
		if (m_cookielets == null) {
			m_cookielets = new LinkedHashMap<String, String>();
		}

		if (value == null) {
			m_cookielets.remove(name);
		} else {
			m_cookielets.put(name, value);
		}
	}

	@Override
	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public void setGroupStrategy(ABTestGroupStrategy groupStrategy) {
		m_groupStrategy = groupStrategy;
	}

	public void setup(HttpServletRequest request, HttpServletResponse response, Map<String, String> cookielets) {
		m_request = request;
		m_response = response;
		m_cookielets = cookielets;

		if (m_entity.isEligible(new Date())) {
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
	}
}
