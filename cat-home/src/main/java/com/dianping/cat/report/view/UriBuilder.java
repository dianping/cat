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
package com.dianping.cat.report.view;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

public class UriBuilder {
	public static String action(ViewModel<? extends Page, ? extends Action, ? extends ActionContext<?>> model, Object id) {
		return build(model, id, null, false);
	}

	private static String build(ViewModel<? extends Page, ? extends Action, ? extends ActionContext<?>> model,	Object id,
							String qs, boolean withAction) {
		StringBuilder sb = new StringBuilder(256);

		sb.append(model.getPageUri());

		if (id != null) {
			sb.append('/').append(id);
		}

		boolean flag = false;

		if (withAction) {
			Action action = model.getAction();

			if (action != null && !action.equals(model.getDefaultAction())) {
				sb.append('?').append("op=").append(action.getName());
				flag = true;
			}
		}

		if (qs != null) {
			if (flag) {
				sb.append('&');
			} else {
				sb.append('?');
			}

			sb.append(qs);
		}

		return sb.toString();
	}

	public static String uri(ViewModel<? extends Page, ? extends Action, ? extends ActionContext<?>> model, Object id) {
		return build(model, id, null, true);
	}

	public static String uri2(ViewModel<? extends Page, ? extends Action, ? extends ActionContext<?>> model, Object id,
							String qs) {
		return build(model, id, qs, true);
	}
}
