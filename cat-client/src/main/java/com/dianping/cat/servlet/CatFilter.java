package com.dianping.cat.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.unidal.helper.Joiners;
import org.unidal.helper.Joiners.IBuilder;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;

public class CatFilter implements Filter {
	private static Map<MessageFormat, String> s_patterns = new LinkedHashMap<MessageFormat, String>();

	private List<Handler> m_handlers = new ArrayList<Handler>();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		Context ctx = new Context(new RequestWrapper((HttpServletRequest) request), new ResponseWrapper(
		      (HttpServletResponse) response), chain, m_handlers);

		ctx.handle();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String pattern = filterConfig.getInitParameter("pattern");

		if (pattern != null) {
			try {
				String[] patterns = pattern.split(";");

				for (String temp : patterns) {
					String[] temps = temp.split(":");

					s_patterns.put(new MessageFormat(temps[0].trim()), temps[1].trim());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		m_handlers.add(CatHandler.ENVIRONMENT);
		m_handlers.add(CatHandler.ID_SETUP);
		m_handlers.add(CatHandler.LOG_SPAN);
		m_handlers.add(CatHandler.LOG_CLIENT_PAYLOAD);
	}

	private static enum CatHandler implements Handler {
		ENVIRONMENT {
			protected int detectMode(HttpServletRequest req) {
				String source = req.getHeader("X-CAT-SOURCE");
				String id = req.getHeader("X-CAT-ID");
				
				Cat.setup(null);

				if ("container".equals(source)) {
					return 2;
				} else if (id != null && id.length() > 0) {
					return 1;
				} else {
					return 0;
				}
			}

			@Override
			public void handle(Context ctx) throws IOException, ServletException {
				HttpServletRequest req = ctx.getRequest();
				boolean top = !Cat.getManager().hasContext();

				ctx.setTop(top);

				if (top) {
					ctx.setMode(detectMode(req));
					ctx.setType(CatConstants.TYPE_URL);

					setTraceMode(req);
				} else {
					ctx.setType(CatConstants.TYPE_URL_FORWARD);
				}

				try {
					ctx.handle();
				} finally {
					if (top) {
						Cat.reset();
					}
				}
			}

			protected void setTraceMode(HttpServletRequest req) {
				String traceMode = "X-CAT-TRACE-MODE";
				String headMode = req.getHeader(traceMode);

				if ("true".equals(headMode)) {
					Cat.getManager().setTraceMode(true);
				}
			}
		},

		ID_SETUP {
			private String m_servers;

			private String getCatServer() {
				if (m_servers == null) {
					DefaultMessageManager manager = (DefaultMessageManager) Cat.getManager();
					List<Server> servers = manager.getConfigManager().getServers();

					m_servers = Joiners.by(',').join(servers, new IBuilder<Server>() {
						@Override
						public String asString(Server server) {
							String ip = server.getIp();
							Integer httpPort = server.getHttpPort();

							if ("127.0.0.1".equals(ip)) {
								ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
							}

							return ip + ":" + httpPort;
						}
					});
				}

				return m_servers;
			}

			@Override
			public void handle(Context ctx) throws IOException, ServletException {
				boolean isTraceMode = Cat.getManager().isTraceMode();

				HttpServletRequest req = ctx.getRequest();
				HttpServletResponse res = ctx.getResponse();
				MessageProducer producer = Cat.getProducer();
				int mode = ctx.getMode();

				switch (mode) {
				case 0:
					ctx.setId(producer.createMessageId());
					break;
				case 1:
					ctx.setRootId(req.getHeader("X-CAT-ROOT-ID"));
					ctx.setParentId(req.getHeader("X-CAT-PARENT-ID"));
					ctx.setId(req.getHeader("X-CAT-ID"));
					break;
				case 2:
					ctx.setRootId(producer.createMessageId());
					ctx.setParentId(ctx.getRootId());
					ctx.setId(producer.createMessageId());
					break;
				default:
					throw new RuntimeException(String.format("Internal Error: unsupported mode(%s)!", mode));
				}

				if (isTraceMode) {
					MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

					tree.setMessageId(ctx.getId());
					tree.setParentMessageId(ctx.getParentId());
					tree.setRootMessageId(ctx.getRootId());

					res.setHeader("X-CAT-SERVER", getCatServer());

					switch (mode) {
					case 0:
						res.setHeader("X-CAT-ROOT-ID", ctx.getId());
						break;
					case 1:
						res.setHeader("X-CAT-ROOT-ID", ctx.getRootId());
						res.setHeader("X-CAT-PARENT-ID", ctx.getParentId());
						res.setHeader("X-CAT-ID", ctx.getId());
						break;
					case 2:
						res.setHeader("X-CAT-ROOT-ID", ctx.getRootId());
						res.setHeader("X-CAT-PARENT-ID", ctx.getParentId());
						res.setHeader("X-CAT-ID", ctx.getId());
						break;
					}
				}

				ctx.handle();
			}
		},

		LOG_CLIENT_PAYLOAD {
			@Override
			public void handle(Context ctx) throws IOException, ServletException {
				HttpServletRequest req = ctx.getRequest();
				String type = ctx.getType();

				if (ctx.isTop()) {
					logRequestClientInfo(req, type);
					logRequestPayload(req, type);
				} else {
					logRequestPayload(req, type);
				}

				ctx.handle();
			}

			protected void logRequestClientInfo(HttpServletRequest req, String type) {
				StringBuilder sb = new StringBuilder(1024);
				String ip = "";
				String ipForwarded = req.getHeader("x-forwarded-for");

				if (ipForwarded == null) {
					ip = req.getRemoteAddr();
				} else {
					ip = ipForwarded;
				}

				sb.append("IPS=").append(ip);
				sb.append("&VirtualIP=").append(req.getRemoteAddr());
				sb.append("&Server=").append(req.getServerName());
				sb.append("&Referer=").append(req.getHeader("referer"));
				sb.append("&Agent=").append(req.getHeader("user-agent"));

				Cat.logEvent(type, type + ".Server", Message.SUCCESS, sb.toString());
			}

			protected void logRequestPayload(HttpServletRequest req, String type) {
				StringBuilder sb = new StringBuilder(256);

				sb.append(req.getScheme().toUpperCase()).append('/');
				sb.append(req.getMethod()).append(' ').append(req.getRequestURI());

				String qs = req.getQueryString();

				if (qs != null) {
					sb.append('?').append(qs);
				}

				Cat.logEvent(type, type + ".Method", Message.SUCCESS, sb.toString());
			}
		},

		LOG_SPAN {

			private String getRequestURI(HttpServletRequest req) {
				String requestURI = req.getRequestURI();

				if (s_patterns.size() == 0) {
					return requestURI;
				} else {
					for (Entry<MessageFormat, String> entry : s_patterns.entrySet()) {
						MessageFormat format = entry.getKey();

						try {
							format.parse(requestURI);

							return entry.getValue();
						} catch (Exception e) {
							// ignore
						}
					}
					return requestURI;
				}
			}

			@Override
			public void handle(Context ctx) throws IOException, ServletException {
				HttpServletRequest req = ctx.getRequest();
				Transaction t = Cat.newTransaction(ctx.getType(), getRequestURI(req));

				try {
					ctx.handle();

					// page uri and status are customizable
					Object catPageUri = req.getAttribute(CatConstants.CAT_PAGE_URI);
					Object catStatus = req.getAttribute(CatConstants.CAT_STATE);

					if (t instanceof DefaultTransaction && catPageUri instanceof String) {
						((DefaultTransaction) t).setName(catPageUri.toString());
					}
					if (catStatus != null) {
						t.setStatus(catStatus.toString());
					} else {
						t.setStatus(Message.SUCCESS);
					}
				} catch (ServletException e) {
					Cat.logError(e);
					t.setStatus(e);
					throw e;
				} catch (IOException e) {
					Cat.logError(e);
					t.setStatus(e);
					throw e;
				} catch (RuntimeException e) {
					Cat.logError(e);
					t.setStatus(e);
					throw e;
				} catch (Error e) {
					Cat.logError(e);
					t.setStatus(e);
					throw e;
				} finally {
					t.complete();
				}
			}
		};
	}

	protected static class Context {
		private FilterChain m_chain;

		private List<Handler> m_handlers;

		private int m_index;

		private int m_mode;

		private String m_rootId;

		private String m_parentId;

		private String m_id;

		private HttpServletRequest m_request;

		private HttpServletResponse m_response;

		private boolean m_top;

		private String m_type;

		public Context(HttpServletRequest request, HttpServletResponse response, FilterChain chain, List<Handler> handlers) {
			m_request = request;
			m_response = response;
			m_chain = chain;
			m_handlers = handlers;
		}

		public String getId() {
			return m_id;
		}

		public int getMode() {
			return m_mode;
		}

		public String getParentId() {
			return m_parentId;
		}

		public HttpServletRequest getRequest() {
			return m_request;
		}

		public HttpServletResponse getResponse() {
			return m_response;
		}

		public String getRootId() {
			return m_rootId;
		}

		public String getType() {
			return m_type;
		}

		public void handle() throws IOException, ServletException {
			if (m_index < m_handlers.size()) {
				Handler handler = m_handlers.get(m_index++);

				handler.handle(this);
			} else {
				m_chain.doFilter(m_request, m_response);
			}
		}

		public boolean isTop() {
			return m_top;
		}

		public void setId(String id) {
			m_id = id;
		}

		public void setMode(int mode) {
			m_mode = mode;
		}

		public void setParentId(String parentId) {
			m_parentId = parentId;
		}

		public void setRootId(String rootId) {
			m_rootId = rootId;
		}

		public void setTop(boolean top) {
			m_top = top;
		}

		public void setType(String type) {
			m_type = type;
		}
	}

	public static class CookieWrapper extends Cookie {
		private Cookie m_cookie;

		public CookieWrapper(Cookie cookie) {
			super(cookie.getName(), cookie.getValue());
			m_cookie = cookie;
		}

		public CookieWrapper(String name, String value) {
			super(name, value);
		}

		public Object clone() {
			return m_cookie.clone();
		}

		public boolean equals(Object obj) {
			return m_cookie.equals(obj);
		}

		public String getComment() {
			return m_cookie.getComment();
		}

		public String getDomain() {
			return m_cookie.getDomain();
		}

		public int getMaxAge() {
			return m_cookie.getMaxAge();
		}

		public String getName() {
			return m_cookie.getName();
		}

		public String getPath() {
			return m_cookie.getPath();
		}

		public boolean getSecure() {
			return m_cookie.getSecure();
		}

		public String getValue() {
			Event event = Cat.newEvent(Cat.getManager().getDomain() + ":ReadCookie", m_cookie.getName());

			event.setStatus(Event.SUCCESS);
			event.addData("domain", m_cookie.getDomain());
			event.addData("path", m_cookie.getPath());
			event.addData("maxAge", m_cookie.getMaxAge());
			event.complete();
			return m_cookie.getValue();
		}

		public int getVersion() {
			return m_cookie.getVersion();
		}

		public int hashCode() {
			return m_cookie.hashCode();
		}

		public void setComment(String purpose) {
			m_cookie.setComment(purpose);
		}

		public void setDomain(String pattern) {
			m_cookie.setDomain(pattern);
		}

		public void setMaxAge(int expiry) {
			m_cookie.setMaxAge(expiry);
		}

		public void setPath(String uri) {
			m_cookie.setPath(uri);
		}

		public void setSecure(boolean flag) {
			m_cookie.setSecure(flag);
		}

		public void setValue(String newValue) {
			m_cookie.setValue(newValue);
		}

		public void setVersion(int v) {
			m_cookie.setVersion(v);
		}

		public String toString() {
			return m_cookie.toString();
		}
	}

	protected static interface Handler {
		public void handle(Context ctx) throws IOException, ServletException;
	}

	public static class RequestWrapper extends HttpServletRequestWrapper {

		private HttpServletRequest m_request;

		public RequestWrapper(HttpServletRequest request) {
			super(request);
			m_request = request;
		}

		@Override
		public Cookie[] getCookies() {
			Cookie[] cookies = m_request.getCookies();

			if (cookies != null) {
				int length = cookies.length;
				CookieWrapper[] wappers = new CookieWrapper[length];

				for (int i = 0; i < length; i++) {
					wappers[i] = new CookieWrapper(cookies[i]);
				}
				return wappers;
			} else {
				return null;
			}
		}

	}

	public static class ResponseWrapper extends HttpServletResponseWrapper {

		public ResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void addCookie(Cookie cookie) {
			Event event = Cat.newEvent(Cat.getManager().getDomain() + ":SetCookie", cookie.getName());

			event.setStatus(Event.SUCCESS);
			event.addData("domain", cookie.getDomain());
			event.addData("path", cookie.getPath());
			event.addData("maxAge", cookie.getMaxAge());
			event.complete();
			super.addCookie(cookie);
		}

		@Override
		public void addHeader(String name, String value) {
			Event event = Cat.newEvent(Cat.getManager().getDomain() + ":SetHead", name);

			event.setStatus(Event.SUCCESS);
			event.addData("value", value);
			event.complete();
			super.addHeader(name, value);
		}
	}
}
