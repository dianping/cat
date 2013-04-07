package com.dianping.cat.abtest.spi.internal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestContextManager;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestEntityManager;

public class DefaultABTestContextManager implements ABTestContextManager {
	@Inject
	private ABTestEntityManager m_entityManager;

	private InheritableThreadLocal<Entry> m_threadLocal = new InheritableThreadLocal<Entry>() {
		@Override
		protected Entry initialValue() {
			return new Entry();
		}
	};

	@Override
	public ABTestContext getContext(ABTestId testId) {
		Entry entry = m_threadLocal.get();
		Map<Integer, DefaultABTestContext> map = entry.getContextMap();
		int id = testId.getValue();
		DefaultABTestContext ctx = map.get(id);

		if (ctx == null) {
			ABTestEntity entity = m_entityManager.getEntity(testId);

			ctx = new DefaultABTestContext(entity);
			ctx.setup(entry.getHttpServletRequest());
			map.put(id, ctx);
		}

		return ctx;
	}

	@Override
	public void onRequestEnd() {
		m_threadLocal.remove();
	}

	@Override
	public void onRequestBegin(HttpServletRequest req) {
		Entry entry = m_threadLocal.get();

		entry.setHttpServletRequest(req);

		Map<Integer, DefaultABTestContext> map = entry.getContextMap();
		for (DefaultABTestContext ctx : map.values()) {
			ctx.setup(req);
		}
	}

	static class Entry {
		private Map<Integer, DefaultABTestContext> m_map = new HashMap<Integer, DefaultABTestContext>(4);

		private HttpServletRequest m_req;

		public Map<Integer, DefaultABTestContext> getContextMap() {
			return m_map;
		}

		public HttpServletRequest getHttpServletRequest() {
			return m_req;
		}

		public void setHttpServletRequest(HttpServletRequest req) {
			m_req = req;
		}
	}
}
