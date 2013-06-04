package com.dianping.cat.system.page.aggregation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import com.dainping.cat.consumer.core.dal.AggregationRule;
import com.dainping.cat.consumer.core.dal.AggregationRuleDao;
import com.dainping.cat.consumer.core.dal.AggregationRuleEntity;
import com.dianping.cat.Cat;

import com.dianping.cat.system.SystemPage;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private AggregationRuleDao m_aggregationRuleDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "aggregation")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "aggregation")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.AGGREGATION);
		Action action = payload.getAction();
		
		model.setAction(action);
		switch (action) {
		case ALL:
			model.setAggregationRules(queryAllAggregationRules());
			break;
		case UPDATE:
			model.setAggregationRule(queryAggregationRuleById(payload.getId()));
			break;
		case UPDATE_SUBMIT:
			updateAggregationRule(payload);
			model.setAggregationRules(queryAllAggregationRules());
			break;
		case DELETE:
			deleteAggregationRule(payload);
			model.setAggregationRules(queryAllAggregationRules());
			break;
		default:
			break;
		}		
		m_jspViewer.view(ctx, model);
	}
	
	private void deleteAggregationRule(Payload payload) {
		AggregationRule proto = new AggregationRule();
		proto.setKeyId(payload.getId());
		try {
			m_aggregationRuleDao.deleteByPK(proto);
		} catch (DalException e) {
			e.printStackTrace();
		}
		
		
	}

	private void updateAggregationRule(Payload payload) {
		AggregationRule proto = new AggregationRule();
		proto.setId(payload.getId());
		proto.setDisplayName(payload.getDisplayName());
		proto.setDomain(payload.getDomain());
		proto.setPattern(payload.getPattern());
		proto.setSample(payload.getSample());
		proto.setType(payload.getType());
		proto.setKeyId(payload.getId());
		try {
			if(proto.getKeyId() == 0){
				m_aggregationRuleDao.insert(proto);
			} else {
				m_aggregationRuleDao.updateByPK(proto, AggregationRuleEntity.UPDATESET_FULL);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private List<AggregationRule> queryAllAggregationRules(){
		List<AggregationRule> aggregationRules = new ArrayList<AggregationRule>();
		try {
			aggregationRules = m_aggregationRuleDao.findAll(AggregationRuleEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return aggregationRules;
	}
	
	private AggregationRule queryAggregationRuleById(int id){
		try {
			return m_aggregationRuleDao.findByPK(id, AggregationRuleEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
			return null;
		}
	}
}
