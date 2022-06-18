package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;
import com.dianping.cat.message.tree.MessageTree;

// Component
public class MessageTreeSampler extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "sampler";

	// Inject
	private ConfigureManager m_configureManager;

	@Override
	public int getOrder() {
		return 200;
	}

	@Override
	public void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
		boolean blocked = m_configureManager.getBooleanProperty(ConfigureProperty.BLOCKED, false);
		
		super.handleMessagreTree(ctx, tree);
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);
	}
}
