package com.dianping.cat.report.view;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

public class UriBuilder {
	public static String action(ViewModel<? extends Page, ? extends Action, ? extends ActionContext<?>> model, Object id) {
		return build(model, id, null, false);
	}

	private static String build(ViewModel<? extends Page, ? extends Action, ? extends ActionContext<?>> model,
	      Object id, String qs, boolean withAction) {
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
