/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.support.servlet;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.TraceContextHelper;
import com.dianping.cat.message.internal.DefaultTransaction;

public class CatFilter implements Filter {
	private static Map<MessageFormat, String> s_patterns = new LinkedHashMap<MessageFormat, String>();

	private List<Handler> m_handlers = new ArrayList<Handler>();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	      throws IOException, ServletException {
		Context ctx = new Context((HttpServletRequest) request, (HttpServletResponse) response, chain, m_handlers);

		ctx.handle();
	}

	protected String getOriginalUrl(ServletRequest request) {
		return ((HttpServletRequest) request).getRequestURI();
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
			@Override
			public void handle(Context ctx) throws IOException, ServletException {
				boolean top = !TraceContextHelper.threadLocal().hasPeekTransaction();

				ctx.setTop(top);

				if (top) {
					ctx.setType(CatClientConstants.TYPE_URL);
				} else {
					ctx.setType(CatClientConstants.TYPE_URL_FORWARD);
				}

				ctx.handle();
			}
		},

		ID_SETUP {
			@Override
			public void handle(Context ctx) throws IOException, ServletException {
				HttpServletRequest req = ctx.getRequest();
				String id = req.getHeader("x-cat-id");
				String parentId = req.getHeader("x-cat-parent-id");
				String rootId = req.getHeader("x-cat-root-id");

				if (id != null) {
					MessageTree tree = TraceContextHelper.threadLocal().getMessageTree();

					tree.setMessageId(id);
					tree.setParentMessageId(parentId);
					tree.setRootMessageId(rootId);
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
			private void customizeStatus(Transaction t, HttpServletRequest req) {
				Object catStatus = req.getAttribute(CatClientConstants.CAT_STATE);

				if (catStatus != null) {
					t.setStatus(catStatus.toString());
				} else {
					t.setStatus(Message.SUCCESS);
				}
			}

			private void customizeUri(Transaction t, HttpServletRequest req) {
				if (t instanceof DefaultTransaction) {
					Object catPageType = req.getAttribute(CatClientConstants.CAT_PAGE_TYPE);

					if (catPageType instanceof String) {
						((DefaultTransaction) t).setType(catPageType.toString());
					}

					Object catPageUri = req.getAttribute(CatClientConstants.CAT_PAGE_URI);

					if (catPageUri instanceof String) {
						((DefaultTransaction) t).setName(catPageUri.toString());
					}
				}
			}

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
					customizeStatus(t, req);
				} catch (ServletException e) {
					t.setStatus(e);
					Cat.logError(e);
					throw e;
				} catch (IOException e) {
					t.setStatus(e);
					Cat.logError(e);
					throw e;
				} catch (Throwable e) {
					t.setStatus(e);
					Cat.logError(e);
					throw new RuntimeException(e);
				} finally {
					customizeUri(t, req);
					t.complete();
				}
			}
		};
	}

	private static class Context {
		private FilterChain m_chain;

		private List<Handler> m_handlers;

		private int m_index;

		private HttpServletRequest m_request;

		private HttpServletResponse m_response;

		private boolean m_top;

		private String m_type;

		public Context(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		      List<Handler> handlers) {
			m_request = request;
			m_response = response;
			m_chain = chain;
			m_handlers = handlers;
		}

		public HttpServletRequest getRequest() {
			return m_request;
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

		public void setTop(boolean top) {
			m_top = top;
		}

		public void setType(String type) {
			m_type = type;
		}
	}

	private static interface Handler {
		public void handle(Context ctx) throws IOException, ServletException;
	}
}