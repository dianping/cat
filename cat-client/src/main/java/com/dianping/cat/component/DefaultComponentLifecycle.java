package com.dianping.cat.component;

import com.dianping.cat.component.lifecycle.Disposable;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.component.lifecycle.Logger;

public class DefaultComponentLifecycle implements ComponentLifecycle {
	private ComponentContext m_ctx;

	private Logger m_logger;

	public DefaultComponentLifecycle(ComponentContext ctx) {
		m_ctx = ctx;
	}

	private Logger getLogger() {
		// lazy load to avoid cyclically dependency resolution
		if (m_logger == null) {
			m_logger = m_ctx.lookup(Logger.class);
		}

		return m_logger;
	}

	@Override
	public void onStart(Object component) {
		if (component instanceof LogEnabled) {
			((LogEnabled) component).enableLogging(getLogger());
		}

		if (component instanceof Initializable) {
			((Initializable) component).initialize(m_ctx);
		}
	}

	@Override
	public void onStop(Object component) {
		if (component instanceof Disposable) {
			((Disposable) component).dispose();
		}
	}
}
