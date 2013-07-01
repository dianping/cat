package com.dianping.cat.abtest.spi.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.ABTestName;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultABTestContextManager extends ContainerHolder implements ABTestContextManager {
	private static final String ABTEST_COOKIE_NAME = "ab";

	@Inject
	private ABTestEntityManager m_entityManager;

	@Inject
	private MessageManager m_messageManager;

	@Inject
	private ABTestCodec m_cookieCodec;

	private InheritableThreadLocal<Entry> m_threadLocal = new InheritableThreadLocal<Entry>() {
		@Override
		protected Entry initialValue() {
			return new Entry();
		}
	};

	@Override
	public ABTestContext getContext(ABTestName testName) {
		Entry entry = m_threadLocal.get();
		ABTestEntity entity = m_entityManager.getEntity(testName);

		return entry.getContext(entity);
	}

	@Override
	public void onRequestBegin(HttpServletRequest request, HttpServletResponse response) {
		Entry entry = m_threadLocal.get();

		entry.setup(request, response);
	}

	@Override
	public void onRequestEnd() {
		m_threadLocal.remove();
	}

	public class Entry {
		private Map<String, ABTestContext> m_map = new HashMap<String, ABTestContext>(4);

		private ABTestContext createContext(ABTestEntity entity) {
			DefaultABTestContext ctx = new DefaultABTestContext(entity);

			if (!entity.isDisabled()) {
				ABTestGroupStrategy groupStrategy = entity.getGroupStrategy();

				ctx.setGroupStrategy(groupStrategy);
			}

			return ctx;
		}

		public ABTestContext getContext(ABTestEntity entity) {
			String name = entity.getName();
			ABTestContext ctx = m_map.get(name);

			if (ctx == null) {
				ctx = createContext(entity);
				m_map.put(name, ctx);
			}

			return ctx;
		}

		private String getCookie(HttpServletRequest request, String name) {
			Cookie[] cookies = request.getCookies();
			String value = "";

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (name.equals(cookie.getName())) {
						value = cookie.getValue();
						break;
					}
				}
			}

			return value;
		}

		private void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
			Cookie cookie = new Cookie(name, value);
			String server = request.getServerName();

			if (server.endsWith(".dianping.com")) {
				cookie.setDomain(".dianping.com");
			} else if (server.endsWith(".51ping.com")) {
				cookie.setDomain(".51ping.com");
			} else {
				cookie.setDomain(server);
			}

			cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days expiration
			cookie.setPath("/");

			response.addCookie(cookie);
		}

		public void setup(HttpServletRequest request, HttpServletResponse response) {
			List<ABTestEntity> activeEntities = m_entityManager.getEntityList();
			Set<String> activeRuns = m_entityManager.getActiveRun();
			String value = getCookie(request, ABTEST_COOKIE_NAME);
			Map<String, Map<String, String>> map = m_cookieCodec.decode(value, activeRuns);

			for (int i = 0; i < activeEntities.size(); i++) {
				ABTestEntity entity = activeEntities.get(i);
				DefaultABTestContext ctx = (DefaultABTestContext) getContext(entity);
				String key = String.valueOf(entity.getRun().getId());
				Map<String, String> cookielets = map.get(key);

				ctx.setup(request, response, cookielets);

				Map<String, String> newCookielets = ctx.getCookielets();

				if (newCookielets != null) {
					map.put(key, newCookielets);
				} else {
					map.remove(key);
				}
			}

			String newValue = m_cookieCodec.encode(map);

			setCookie(request, response, ABTEST_COOKIE_NAME, newValue);

			if (newValue != null && newValue.length() > 0) {
				((DefaultMessageManager) m_messageManager).setMetricType(newValue);
			}
		}
	}
}
