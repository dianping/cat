package com.dianping.cat.hadoop.build;

import com.dianping.cat.CatCoreModule;
import com.site.dal.jdbc.datasource.JdbcDataSourceConfigurationManager;
import com.site.initialization.AbstractModule;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;

public class CatHadoopModule extends AbstractModule {
	public static final String ID = "cat-hadoop";

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatCoreModule.ID);
	}

	@Override
	protected void execute(ModuleContext ctx) {
		// warm up database connection
		ctx.lookup(JdbcDataSourceConfigurationManager.class);
	}
}
