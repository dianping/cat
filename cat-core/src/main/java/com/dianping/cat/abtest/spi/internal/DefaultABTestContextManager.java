package com.dianping.cat.abtest.spi.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.ABTestName;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class DefaultABTestContextManager extends ContainerHolder implements ABTestContextManager {
	@Inject
	private ABTestEntityManager m_entityManager;

	private InheritableThreadLocal<Entry> m_threadLocal = new InheritableThreadLocal<Entry>() {
		@Override
		protected Entry initialValue() {
			return new Entry();
		}
	};

	@Override
	public ABTestContext getContext(ABTestName testName) {
		Entry entry = m_threadLocal.get();
		Map<String, DefaultABTestContext> map = entry.getContextMap();
		String name = testName.getValue();
		DefaultABTestContext ctx = map.get(name);

		if (ctx == null) {
			ABTestEntity entity = m_entityManager.getEntity(testName);

			ctx = createContext(entity, entry.getHttpServletRequest());
			map.put(name, ctx);
		}

		return ctx;
	}

	private DefaultABTestContext createContext(ABTestEntity entity, HttpServletRequest req) {
		DefaultABTestContext ctx = new DefaultABTestContext(entity);

		if (!entity.isDisabled()) {
			ABTestGroupStrategy groupStrategy = entity.getGroupStrategy();

			ctx.setup(req);
			ctx.setGroupStrategy(groupStrategy);
		}

		return ctx;
	}

	public List<ABTestContext> getContexts() {
		List<ABTestContext> ctxList = m_threadLocal.get().getContextList();

		if (ctxList == null) {
			ctxList = new ArrayList<ABTestContext>(4);

			List<ABTestEntity> entities = m_entityManager.getEntityList();
			Map<String, DefaultABTestContext> ctxMap = m_threadLocal.get().getContextMap();
			Date now = new Date();

			for (ABTestEntity entity : entities) {
				Entry entry = m_threadLocal.get();
				String name = entity.getName();
				DefaultABTestContext ctx = ctxMap.get(name);

				if (ctx == null) {
					ctx = createContext(entity, entry.getHttpServletRequest());

					ctxMap.put(name, ctx);
				}

				ctx.initialize(now);
				ctxList.add(ctx);
			}

			m_threadLocal.get().setContextList(ctxList);

		}

		return ctxList;
	}

	@Override
	public void onRequestEnd() {
		m_threadLocal.remove();
	}

	@Override
	public void onRequestBegin(HttpServletRequest req) {
		Entry entry = m_threadLocal.get();

		entry.setHttpServletRequest(req);

		Map<String, DefaultABTestContext> map = entry.getContextMap();
		for (DefaultABTestContext ctx : map.values()) {
			ctx.setup(req);
		}
	}

	static class Entry {
		private Map<String, DefaultABTestContext> m_map = new HashMap<String, DefaultABTestContext>(4);

		private List<ABTestContext> m_list;

		private HttpServletRequest m_req;

		public Map<String, DefaultABTestContext> getContextMap() {
			return m_map;
		}

		public void setContextList(List<ABTestContext> ctxList) {
			m_list = ctxList;
		}

		public List<ABTestContext> getContextList() {
			return m_list;
		}

		public HttpServletRequest getHttpServletRequest() {
			return m_req;
		}

		public void setHttpServletRequest(HttpServletRequest req) {
			m_req = req;
		}
	}
}
