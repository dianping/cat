package com.dianping.cat.component;

import com.dianping.cat.component.lifecycle.Disposable;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;

public class DefaultComponentLifecycle implements ComponentLifecycle, Initializable {
	private ComponentContext m_ctx;

	private Logger m_logger;

	public DefaultComponentLifecycle(ComponentContext ctx) {
		m_ctx = ctx;
	}

	@Override
	public void onStart(Object component) {
		if (component instanceof LogEnabled) {
			((LogEnabled) component).enableLogging(m_logger);
		}

		if (component instanceof Initializable) {
			((Initializable) component).initialize(m_ctx);
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_logger = m_ctx.lookup(Logger.class);
	}

	@Override
	public void onStop(Object component) {
		if (component instanceof Disposable) {
			((Disposable) component).dispose();
		}
	}
}
