package com.dianping.cat.abtest.spi.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestContextManager;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestEntityManager;
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
	public ABTestContext getContext(ABTestId testId) {
		Entry entry = m_threadLocal.get();
		Map<Integer, DefaultABTestContext> map = entry.getContextMap();
		int id = testId.getValue();
		DefaultABTestContext ctx = map.get(id);

		if (ctx == null) {
			ABTestEntity entity = m_entityManager.getEntity(testId);

			ctx = createContext(entity, entry.getHttpServletRequest());

			map.put(id, ctx);
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
			Map<Integer, DefaultABTestContext> ctxMap = m_threadLocal.get().getContextMap();

			for (ABTestEntity entity : entities) {
				Entry entry = m_threadLocal.get();
				int id = entity.getId();
				DefaultABTestContext ctx = ctxMap.get(id);

				if (ctx == null) {
					ctx = createContext(entity, entry.getHttpServletRequest());

					ctxMap.put(id, ctx);
				}

				ctx.getGroupName();// Make sure GroupName is calculated (if GroupName is null, this will trigger GroupName to be calculated)
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

		Map<Integer, DefaultABTestContext> map = entry.getContextMap();
		for (DefaultABTestContext ctx : map.values()) {
			ctx.setup(req);
		}
	}

	static class Entry {
		private Map<Integer, DefaultABTestContext> m_map = new HashMap<Integer, DefaultABTestContext>(4);

		private List<ABTestContext> m_list;

		private HttpServletRequest m_req;

		public Map<Integer, DefaultABTestContext> getContextMap() {
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
