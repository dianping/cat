//package com.dianping.cat.report.page.model.problem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.unidal.dal.jdbc.DalException;
//import org.unidal.lookup.annotation.Inject;
//
//import com.dianping.cat.Cat;
//import com.dianping.cat.consumer.core.aggregation.AggregationHandler;
//import com.dianping.cat.consumer.core.aggregation.DefaultAggregationHandler;
//import com.dianping.cat.consumer.problem.model.entity.Entry;
//import com.dianping.cat.consumer.problem.model.entity.Machine;
//import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
//import com.dianping.cat.home.dal.report.AggregationRule;
//import com.dianping.cat.home.dal.report.AggregationRuleDao;
//import com.dianping.cat.home.dal.report.AggregationRuleEntity;
//
//public class ProblemReportMergerWithAggregation extends ProblemReportMerger {
//	
//	@Inject
//	private AggregationRuleDao m_aggregationRuleDao;
//	
//	protected ProblemReportMergerWithAggregation(ProblemReport problemReport) {
//		super(problemReport);
//	}
//
//	private AggregationHandler aggregationHandler = new DefaultAggregationHandler();
//
//	@Override
//	public void visitEntry(Entry entry) {
//		Machine machine = (Machine) getObjects().peek();
//		String status = entry.getStatus();
//		status = aggregationHandler.handle(status);
//		entry.setStatus(status);
//		Entry old = findEntry(machine, entry);
//
//		if (old == null) {
//			old = new Entry();
//			old.setType(entry.getType()).setStatus(entry.getStatus());
//			machine.addEntry(old);
//		}
//
//		visitEntryChildren(old, entry);
//	}
//	@Override
//	public void visitProblemReport(ProblemReport problemReport) {
//		List<AggregationRule> rules = null;
//		try {
//			rules = m_aggregationRuleDao.findAll(AggregationRuleEntity.READSET_FULL);
//		} catch (DalException e) {
//			Cat.logError(e);
//		}
//		
//		List<String> formats = new ArrayList<String>();
//		for(AggregationRule rule:rules){
//			if(rule.getDomain().toLowerCase().equals("cat"))
//			formats.add(rule.getPattern());
//		}
//		aggregationHandler.register(formats);
//		super.visitProblemReport(problemReport);
//	}
//	
//}
