//package com.dianping.cat.report.page.model.problem;
//
//import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
//
//public class ProblemReportMergerManager {
//	public static ProblemReportMerger getProblemReportMerger(ProblemReport report){
//		if(report.getDomain().toLowerCase().equals("cat")){
//			return new ProblemReportMergerWithAggregation(report);
//		} else{
//			return new ProblemReportMerger(report);
//		}
//	}
//}
