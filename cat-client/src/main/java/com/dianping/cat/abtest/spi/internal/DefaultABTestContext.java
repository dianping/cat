package com.dianping.cat.abtest.spi.internal;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.ConversionRule;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class DefaultABTestContext implements ABTestContext {
	private String m_groupName = DEFAULT_GROUP;

	private ABTestEntity m_entity;

	private HttpServletRequest m_request;

	private HttpServletResponse m_response;

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

	private void isEligableRequest(List<ConversionRule> conversionRules, HttpServletRequest request) {
		if (m_cookielets == null) {
			return;
		}

		String actual = (String) request.getAttribute("url-rewrite-original-url");

		if (isEmptyString(actual)) { // no url-rewrite
			actual = request.getRequestURL().toString();
		}

		boolean isAccept = false;

		for (ConversionRule rule : conversionRules) {
			if (actual.equalsIgnoreCase(rule.getText())) {
				isAccept = true;
				break;
			}
		}

		if (!isAccept) {
			m_cookielets.clear();
		}
	}

	private boolean isEmptyString(String str) {
		if (str != null && str.length() > 0) {
			return false;
		} else {
			return true;
		}
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

	public void setCookielets(Map<String, String> cookielets) {
		m_cookielets = cookielets;
	}

	@Override
	public void setGroupName(String groupName) {
		m_groupName = groupName;
		setCookielet("ab", groupName);
	}

	public void setup(HttpServletRequest request, HttpServletResponse response, Map<String, String> cookielets) {
		m_request = request;
		m_response = response;
		m_cookielets = cookielets;

		Invocable inv = m_entity.getInvocable();

		if (inv != null && m_entity.isEligible(new Date())) {
			boolean isAccept = false;
			Transaction t = Cat.newTransaction("ABTest-GroupStrategy", m_entity.getGroupStrategyName());

			try {
				isAccept = (Boolean) inv.invokeFunction("isEligible", request);

				if (isAccept) {
					m_entity.apply(this);
				}

				t.setStatus(Message.SUCCESS);
			} catch (Throwable e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}

		List<ConversionRule> conversionRules = m_entity.getConversionRules();

		if (conversionRules != null && conversionRules.size() > 0) {
			isEligableRequest(conversionRules, request);
		}
	}
}
