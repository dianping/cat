package com.dianping.cat.report.tool;

import java.util.List;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class ReportUtils {
	public static FailureReport parseFailureReportXML(String xml) {
		com.dianping.cat.consumer.failure.model.transform.DefaultXmlParser parser = new com.dianping.cat.consumer.failure.model.transform.DefaultXmlParser();
		try {
			return parser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static FailureReport mergeFailureReports(List<FailureReport> reports) {
		FailureReport result = reports.get(0);
		for (int i = 1; i < reports.size(); i++) {
			mergeFailureReport(result, reports.get(i));
		}
		return result;
	}

	public static void mergeFailureReport(FailureReport targetReport, FailureReport mergeReport) {
		mergeReport.accept(new com.dianping.cat.consumer.failure.model.transform.DefaultMerger(targetReport));
	}

	public static TransactionReport parseTransactionReportXML(String xml) {
		com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser parser = new com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser();
		try {
			return parser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static TransactionReport mergeTransactionReports(List<TransactionReport> reports) {
		TransactionReport result = reports.get(0);
		for (int i = 1; i < reports.size(); i++) {
			mergeTransactionReport(result, reports.get(i));
		}
		return result;
	}

	public static void mergeTransactionReport(TransactionReport targetReport, TransactionReport mergeReport) {
		mergeReport.accept(new com.dianping.cat.consumer.transaction.model.transform.DefaultMerger(targetReport) {

			private double std(long count, double ave, double sum2) {
				return Math.sqrt(sum2 / count - 2 * ave * ave + ave * ave);
			}

			@Override
			protected void mergeName(TransactionName old, TransactionName name) {
				if (old.getId() == null) {
					System.out.println("TransactionName old is null");
					old = name;
				} else if (name.getId() == null) {
					System.out.println("TransactionName new is null");
					return;
				}

				// TODO Auto-generated method stub
				// super.mergeName(old, name);
				old.setTotalCount(old.getTotalCount() + name.getTotalCount());
				old.setFailCount(old.getFailCount() + name.getFailCount());
				old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
				double min = name.getMin();
				if (min < old.getMin()) {
					old.setMin(min);
				}

				double max = name.getMax();
				if (max > old.getMax()) {
					old.setMax(max);
				}

				old.setSum(old.getSum() + name.getSum());
				old.setAvg((double) old.getSum() / (double) old.getTotalCount());

				old.setSum2(old.getSum2() + name.getSum2());

				double std = std(old.getTotalCount(), old.getAvg(), old.getSum2());
				old.setStd(std);
			}

			@Override
			protected void mergeTransactionReport(TransactionReport old, TransactionReport transactionReport) {
				// TODO Auto-generated method stub
				super.mergeTransactionReport(old, transactionReport);

			}

			@Override
			protected void mergeType(TransactionType old, TransactionType name) {
				if (old.getId() == null) {
					System.out.println("TransactionType old is null");
					old = name;
				} else if (name.getId() == null) {
					System.out.println("TransactionType new is null");
					return;
				}
				// TODO Auto-generated method stub
				// super.mergeType(old, type);

				old.setTotalCount(old.getTotalCount() + name.getTotalCount());
				old.setFailCount(old.getFailCount() + name.getFailCount());
				old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
				double min = name.getMin();
				if (min < old.getMin()) {
					old.setMin(min);
				}

				double max = name.getMax();
				if (max > old.getMax()) {
					old.setMax(max);
				}

				old.setSum(old.getSum() + name.getSum());
				old.setAvg((double) old.getSum() / (double) old.getTotalCount());

				old.setSum2(old.getSum2() + name.getSum2());

				double std = std(old.getTotalCount(), old.getAvg(), old.getSum2());
				old.setStd(std);
			}

		});
	}

	public static IpReport parseIpReportXML(String xml) {
		com.dianping.cat.consumer.ip.model.transform.DefaultXmlParser parser = new com.dianping.cat.consumer.ip.model.transform.DefaultXmlParser();
		try {
			return parser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IpReport mergeIpReports(List<IpReport> reports) {
		IpReport result = reports.get(0);
		for (int i = 1; i < reports.size(); i++) {
			mergeIpReport(result, reports.get(i));
		}
		return result;
	}

	static void mergeIpReport(IpReport targetReport, IpReport mergeReport) {
		mergeReport.accept(new com.dianping.cat.consumer.ip.model.transform.DefaultMerger(targetReport) {
			@Override
			protected void mergeIp(Ip old, Ip ip) {
				old.setCount(old.getCount() + ip.getCount());
			}
		});
	}
}
