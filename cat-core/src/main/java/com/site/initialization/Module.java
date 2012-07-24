package com.site.initialization;

public interface Module {
	public Module[] getDependencies(ModuleContext ctx);

	public void initialize(ModuleContext ctx) throws Exception;

	public boolean isInitialized();

	public void setInitialized(boolean initialized);
}
