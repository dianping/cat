package com.site.initialization;

public interface ModuleInitializer {
	public void execute(ModuleContext ctx);

	public void execute(ModuleContext ctx, Module... modules);
}
