package com.dianping.cat.component;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.component.lifecycle.Disposable;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;

public class DefaultComponentLifecycle implements ComponentLifecycle, Initializable, Disposable {
	private ComponentContext m_ctx;

	private Logger m_logger;

	private AtomicBoolean m_initialized = new AtomicBoolean();

	public DefaultComponentLifecycle(ComponentContext ctx) {
		m_ctx = ctx;
	}

	@Override
	public void dispose() {
		if (m_initialized.get()) {
			m_logger = null;
			m_initialized.set(false);
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_initialized.set(true);
		m_logger = m_ctx.lookup(Logger.class);
	}

	@Override
	public void onStart(Object component) {
		if (m_initialized.get()) {
			if (component instanceof LogEnabled) {
				((LogEnabled) component).enableLogging(m_logger);
			}

			if (component instanceof Initializable) {
				((Initializable) component).initialize(m_ctx);
			}
		} else {
			throw new IllegalStateException("Component lifecycle has been shutdown!");
		}
	}

	@Override
	public void onStop(Object component) {
		if (m_initialized.get()) {
			if (component instanceof Disposable) {
				((Disposable) component).dispose();
			}
		} else {
			throw new IllegalStateException("Component lifecycle has been shutdown!");
		}
	}
}
