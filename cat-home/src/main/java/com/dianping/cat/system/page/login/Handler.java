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
package com.dianping.cat.system.page.login;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.jsp.function.CodecFunction;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ErrorObject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.model.entity.InboundActionModel;

import com.dianping.cat.system.SystemContext;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.login.service.Credential;
import com.dianping.cat.system.page.login.service.LoginMember;
import com.dianping.cat.system.page.login.service.Session;
import com.dianping.cat.system.page.login.service.SigninContext;
import com.dianping.cat.system.page.login.service.SigninService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private SigninService m_signinService;

	private SigninContext createSigninContext(Context ctx) {
		return new SigninContext(ctx.getHttpServletRequest(), ctx.getHttpServletResponse());
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "login")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		if (payload.isSubmit() && action == Action.LOGIN) {
			String account = payload.getAccount();
			String password = payload.getPassword();

			if (account != null && account.length() != 0 && password != null) {
				SigninContext sc = createSigninContext(ctx);
				Credential credential = new Credential(account, password);
				Session session = m_signinService.signin(sc, credential);

				if (session == null) {
					ctx.addError(new ErrorObject("biz.login"));
				} else {
					redirect(ctx, payload);
					return;
				}
			} else {
				ctx.addError(new ErrorObject("biz.login.input").addArgument("account", account).addArgument("password",	password));
			}
		} else if (action == Action.LOGOUT) {
			SigninContext sc = createSigninContext(ctx);

			m_signinService.signout(sc);
			redirect(ctx, payload);
			return;
		} else {
			if (shouldLogin(ctx)) {
				SigninContext sc = createSigninContext(ctx);
				Session session = m_signinService.validate(sc);

				if (session != null) {
					ActionContext<?> parent = ctx.getParent();

					if (parent instanceof SystemContext) {
						SystemContext<?> context = (SystemContext<?>) parent;
						LoginMember member = session.getMember();
						context.setSigninMember(member);
						logAccess(ctx, member);
						return;
					} else if (parent != null) {
						throw new RuntimeException(String.format("%s should extend %s!", ctx.getClass(), SystemContext.class));
					}
				}
			} else {
				return;
			}
		}

		// skip actual action, show sign-in form
		ctx.skipAction();
	}

	private boolean shouldLogin(Context ctx) {
		try {
			ActionContext<?> parent = ctx.getParent();
			InboundActionModel inAction = parent.getRequestContext().getInboundAction();
			LoginAction meta = inAction.getActionMethod().getAnnotation(LoginAction.class);

			if (meta != null) {
				String[] includes = meta.includes();

				if (includes.length > 0) {
					String name = parent.getPayload().getAction().getName();

					for (String action : includes) {
						if (action.equals(name)) {
							return true;
						}
					}

					return false;
				}
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@OutboundActionMeta(name = "login")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.LOGIN);
		model.setAction(Action.LOGIN);

		if (ctx.getParent() != null && (payload.getRtnUrl() == null || payload.getRtnUrl().length() == 0)) {
			HttpServletRequest request = ctx.getHttpServletRequest();
			String qs = request.getQueryString();
			String requestURI = request.getRequestURI();

			if (qs != null) {
				payload.setRtnUrl(requestURI + "?" + qs);
			} else {
				payload.setRtnUrl(requestURI);
			}
		}

		m_jspViewer.view(ctx, model);
	}

	@SuppressWarnings("unchecked")
	private void logAccess(Context ctx, LoginMember member) {
		StringBuilder sb = new StringBuilder(256);
		SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
		HttpServletRequest request = ctx.getHttpServletRequest();
		String actionUri = ctx.getRequestContext().getActionUri();

		sb.append(dateFormat.format(new Date()));
		// sb.append(" ").append(member.getLoginName()).append('/').append(member.getLoginId()).append(' ');

		if (request.getMethod().equalsIgnoreCase("post")) {
			Enumeration<String> names = request.getParameterNames();
			boolean hasQuestion = actionUri.indexOf('?') >= 0;

			sb.append(actionUri);

			while (names.hasMoreElements()) {
				String name = names.nextElement();
				String[] attributes = request.getParameterValues(name);

				for (String attribute : attributes) {
					if (attribute.length() > 0) {
						if (!hasQuestion) {
							sb.append('?');
							hasQuestion = true;
						} else {
							sb.append('&');
						}

						sb.append(name).append('=').append(CodecFunction.urlEncode(attribute));
					}
				}
			}
		} else {
			sb.append(actionUri);
		}
	}

	private void redirect(Context ctx, Payload payload) {
		String url = payload.getRtnUrl();
		String loginUrl = ctx.getRequestContext().getActionUri(SystemPage.LOGIN.getName());

		if (url == null || url.length() == 0 || url.equals(loginUrl)) {
			url = ctx.getRequestContext().getActionUri("");
		}
		if (url.indexOf("/cat/s/login") > -1) {
			url = "/cat/r/home";
		}

		ctx.redirect(url);
		ctx.stopProcess();
	}
}
