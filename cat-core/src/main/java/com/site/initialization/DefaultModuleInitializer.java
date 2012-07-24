package com.site.initialization;

import java.util.LinkedHashSet;
import java.util.Set;

import com.site.lookup.annotation.Inject;

public class DefaultModuleInitializer implements ModuleInitializer {
	@Inject
	private ModuleManager m_manager;

	@Inject
	private boolean m_verbose;

	private int m_index = 1;

	@Override
	public void execute(ModuleContext ctx) {
		Module[] modules = m_manager.getTopLevelModules();

		execute(ctx, modules);
	}

	@Override
	public void execute(ModuleContext ctx, Module... modules) {
		Set<Module> all = new LinkedHashSet<Module>();

		info(ctx, "Initializing top level modules:");

		for (Module module : modules) {
			info(ctx, "   " + module.getClass().getName());
		}

		try {
			expandAll(ctx, modules, all);

			for (Module module : all) {
				if (!module.isInitialized()) {
					executeModule(ctx, module, m_index++);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when initializing modules!", e);
		}
	}

	private void info(ModuleContext ctx, String message) {
		if (m_verbose) {
			ctx.info(message);
		}
	}

	private synchronized void executeModule(ModuleContext ctx, Module module, int index) throws Exception {
		long start = System.currentTimeMillis();

		// set flat to avoid re-entrance
		module.setInitialized(true);

		info(ctx, index + " ------ " + module.getClass().getName());

		// execute itself after its dependencies
		module.initialize(ctx);

		long end = System.currentTimeMillis();
		info(ctx, index + " ------ " + module.getClass().getName() + " DONE in " + (end - start) + " ms.");
	}

	private void expandAll(ModuleContext ctx, Module[] modules, Set<Module> all) throws Exception {
		if (modules != null) {
			for (Module module : modules) {
				expandAll(ctx, module.getDependencies(ctx), all);

				if (!all.contains(module)) {
					if (module instanceof AbstractModule) {
						((AbstractModule) module).setup(ctx);
					}

					all.add(module);
				}
			}
		}
	}

	public void setVerbose(boolean verbose) {
		m_verbose = verbose;
	}
}
