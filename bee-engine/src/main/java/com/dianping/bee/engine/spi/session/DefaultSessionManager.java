package com.dianping.bee.engine.spi.session;

public class DefaultSessionManager implements SessionManager {
	private ThreadLocal<Session> m_threadLocalSession = new ThreadLocal<Session>() {
		@Override
		protected Session initialValue() {
			return new DefaultSession();
		}
	};

	@Override
	public Session getSession() {
		return m_threadLocalSession.get();
	}

	@Override
	public void removeSession() {
		m_threadLocalSession.remove();
	}
}
