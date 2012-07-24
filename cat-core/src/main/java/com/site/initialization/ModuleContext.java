package com.site.initialization;

public interface ModuleContext {
	public <T> T getAttribute(String name);

	public void info(String message);

	public void warn(String message);

	public void error(String message);

	public void error(String message, Throwable t);

	public <T> T lookup(Class<T> role);

	public <T> T lookup(Class<T> role, String roleHint);

	public void release(Object component);

	public void setAttribute(String name, Object value);

	public Module[] getModules(String... modules);
}
