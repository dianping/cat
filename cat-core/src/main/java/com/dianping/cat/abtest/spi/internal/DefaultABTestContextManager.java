package com.dianping.cat.abtest.spi.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.helper.Splitters;
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

		private Map<String, String> getGroupsFromCookie(HttpServletRequest request) {
			Map<String, String> map = new HashMap<String, String>();
			Cookie[] cookies = request.getCookies();

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (ABTEST_COOKIE_NAME.equals(cookie.getName())) {
						String value = cookie.getValue();
						List<String> parts = Splitters.by('|').noEmptyItem().trim().split(value);

						for (String part : parts) {
							int pos = part.indexOf(':');

							if (pos > 0) {
								map.put(part.substring(0, pos), part.substring(pos + 1));
							}
						}
					}
				}
			}

			return map;
		}
		
		private String getABTestCookie(HttpServletRequest request) {
			Cookie[] cookies = request.getCookies();

			String value = "";
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (ABTEST_COOKIE_NAME.equals(cookie.getName())) {
						value = cookie.getValue();
					}
				}
			}

			return value;
		}

		private String setGroupsToCookie(HttpServletRequest request, HttpServletResponse response,
		      Map<String, String> result) {
			StringBuilder sb = new StringBuilder(64);
			boolean first = true;

			for (Map.Entry<String, String> e : result.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append('|');
				}

				sb.append(e.getKey()).append(':').append(e.getValue());
			}

			String value = sb.toString();
			Cookie cookie = new Cookie(ABTEST_COOKIE_NAME, value);
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
			return value;
		}
		
		private String setABTestCookie(HttpServletRequest request, HttpServletResponse response,
		      String value) {
			Cookie cookie = new Cookie(ABTEST_COOKIE_NAME, value);
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
			return value;
		}

		public void setup(HttpServletRequest request, HttpServletResponse response) {
			List<ABTestEntity> activeEntities = m_entityManager.getEntityList();
			Set<String> activeRuns = m_entityManager.getActiveRun();
			Map<String,Map<String,String>> result = new HashMap<String,Map<String,String>>();
			Map<String,Map<String,String>> map = decode(getABTestCookie(request));
			
			for (String id : activeRuns) {
				if (map.containsKey(id)) {
					result.put(id, map.get(id));
				}
			}

			for (int i = 0 ; i < activeEntities.size(); i++) {
				ABTestEntity entity = activeEntities.get(i);
				DefaultABTestContext ctx = (DefaultABTestContext) getContext(entity);
				String key = String.valueOf(ctx.getEntity().getRun().getId());

				ctx.setCookielets(map.get(key));
				ctx.setup(request, response, new Date());
				result.put(key, ctx.getCookielets());
			}
			

			String value = setABTestCookie(request, response, encode(result));
			((DefaultMessageManager) m_messageManager).setMetricType(value);
		}

		public Map<String, Map<String, String>> decode(String cookieValue) {
			Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
			char[] chars = cookieValue.toCharArray();
			int pos = 0;
			String id = null, name = null, value = null;
			Map<String, String> cookielets = new HashMap<String, String>();

			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '=') {
					id = cookieValue.substring(pos, i);
					pos = i + 1;
				}else if (chars[i] == ':') {
					name = cookieValue.substring(pos, i);
					pos = i + 1;
				}else if (chars[i] == '|') {
					value = cookieValue.substring(pos, i);
					cookielets.put(name, value);
					pos = i + 1;
				}else if (chars[i] == '&') {
					value = cookieValue.substring(pos, i);
					cookielets.put(name, value);
					map.put(id, cookielets);
					cookielets = new HashMap<String, String>();
					pos = i + 1;
				}
				
				if (i == chars.length - 1) {
					value = cookieValue.substring(pos, i + 1);
					cookielets.put(name, value);
					map.put(id, cookielets);
				}
			}
			return map;
		}
		
		public String encode(Map<String,Map<String,String>> maps){
			StringBuilder sb = new StringBuilder();
			String[] keySet = maps.keySet().toArray(new String[0]);
			
			for(int i = 0 ; i < keySet.length ; i++){
				sb.append(keySet[i]).append("=");
				
				Map<String, String> values = maps.get(keySet[i]);
				if(values!= null){
					String[] keySetofValues = values.keySet().toArray(new String[0]);
					for(int j = 0 ; j < keySetofValues.length ; j++){
						if(j != keySetofValues.length - 1){
							sb.append(keySetofValues[j]).append(":").append(values.get(keySetofValues[j])).append("|");
						}else{
							sb.append(keySetofValues[j]).append(":").append(values.get(keySetofValues[j]));
						}
					}
					
					if(i != keySet.length - 1){
						sb.append("&");
					}
				}
				
			}

			return sb.toString();
		}
	}
}
