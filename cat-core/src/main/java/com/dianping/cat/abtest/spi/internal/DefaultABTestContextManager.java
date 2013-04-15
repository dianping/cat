package com.dianping.cat.abtest.spi.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestContextManager;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestEntityManager;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

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
			ctx = new DefaultABTestContext();
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
		Map<Integer, DefaultABTestContext> map = m_threadLocal.get().getContextMap();
		List<ABTestEntity> entities = m_entityManager.getEntityList();

		for (ABTestEntity entity : entities) {
			DefaultABTestContext ctx = new DefaultABTestContext(entity);

			ctx.setup(req);

			if (!entity.isDisabled()) {
				if (entity.isEligible(new Date())) {
					Transaction t = Cat.newTransaction("GroupStrategy", entity.getGroupStrategyName());

					try {
						ABTestGroupStrategy groupStrategy = entity.getGroupStrategy();

						groupStrategy.apply(ctx);

						t.setStatus(Message.SUCCESS);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.logError(e);
					} finally {
						t.complete();
					}
				}
			}

			map.put(entity.getId(), ctx);
		}
	}

	static class Entry {
		private Map<Integer, DefaultABTestContext> m_map = new HashMap<Integer, DefaultABTestContext>(4);

		public Map<Integer, DefaultABTestContext> getContextMap() {
			return m_map;
		}
	}
}
