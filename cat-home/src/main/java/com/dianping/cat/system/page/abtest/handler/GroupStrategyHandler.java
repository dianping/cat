package com.dianping.cat.system.page.abtest.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.system.page.abtest.Action;
import com.dianping.cat.system.page.abtest.Context;
import com.dianping.cat.system.page.abtest.Model;
import com.dianping.cat.system.page.abtest.Payload;
import com.dianping.cat.system.page.abtest.ResponseJson;
import com.dianping.cat.system.page.abtest.service.ABTestService;
import com.dianping.cat.system.page.abtest.util.GroupStrategyParser;
import com.dianping.cat.system.page.abtest.util.GsonManager;
import com.google.gson.Gson;

public class GroupStrategyHandler implements SubHandler {
	
	public static final String ID = "groupstrategy_handler";
	
	@Inject
	private ABTestService m_service;
	
	@Inject
	private GsonManager m_gsonManager;
	
	@Inject
	private GroupStrategyParser m_parser;
	
	@Override
	public void handleInbound(Context ctx, Payload payload) {
		Action action = payload.getAction();

		if (action == Action.AJAX_ADDGROUPSTRATEGY) {
			handleCreateGroupStrategyAction(ctx, payload);
		} else if (action == Action.AJAX_PARSEGROUPSTRATEGY) {
			handleParseGroupStrategyAction(ctx, payload);
		}
	}
	
	private void handleCreateGroupStrategyAction(Context ctx, Payload payload) {
		GroupStrategy groupStrategy = new GroupStrategy();

		String name = payload.getGroupStrategyName();
		groupStrategy.setClassName(payload.getGroupStrategyClassName());
		groupStrategy.setName(name);
		groupStrategy.setFullyQualifiedName(payload.getGroupStrategyFullName());
		groupStrategy.setDescriptor(payload.getGroupStrategyDescriptor());
		groupStrategy.setDescription(payload.getGroupStrategyDescription());
		groupStrategy.setStatus(1);

		try {
			List<GroupStrategy> groupStrategies = m_service.getGroupStrategyByName(name);

			if (groupStrategies == null || groupStrategies.size() == 0) {
				m_service.insertGroupStrategy(groupStrategy);
			} else {
				throw new DalException("Aready to has a groupstrategy which has the same name...");
			}

			ctx.setResponseJson(responseJson(0, "successfully create a groupstrategy!"));
		} catch (DalException e) {
			Cat.logError(e);
			ctx.setResponseJson(responseJson(1, e.getMessage()));
		}
	}
	
	private void handleParseGroupStrategyAction(Context ctx, Payload payload) {
		InputStream stream;
		try {
			stream = new ByteArrayInputStream(payload.getSrcCode().getBytes("UTF-8"));
			GroupstrategyDescriptor descriptor = m_parser.parse(stream);

			Gson gson = m_gsonManager.getGson();
			ctx.setResponseJson(gson.toJson(descriptor, GroupstrategyDescriptor.class));
		} catch (Throwable e) {
			ctx.setResponseJson("{}");
		}
	}
	
	
	public String responseJson(int code, String msg) {
		Gson gson = m_gsonManager.getGson();
		return gson.toJson(new ResponseJson(code, msg), ResponseJson.class);
	}

	@Override
   public void handleOutbound(Context ctx, Model model, Payload payload) {
		throw new UnsupportedOperationException();
   }

}
