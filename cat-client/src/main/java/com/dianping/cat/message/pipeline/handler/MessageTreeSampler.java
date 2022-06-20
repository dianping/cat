package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;
import com.dianping.cat.message.context.MessageTree;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

// Component
public class MessageTreeSampler extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "message-tree-sampler";

	// Inject
	private ConfigureManager m_configureManager;

	@Override
	public int getOrder() {
		return 200;
	}

	@Override
	protected void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
		boolean blocked = m_configureManager.getBooleanProperty(ConfigureProperty.BLOCKED, false);

		if (blocked) {
			// stop here
		} else {
			super.handleMessagreTree(ctx, tree);
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);
	}
}
