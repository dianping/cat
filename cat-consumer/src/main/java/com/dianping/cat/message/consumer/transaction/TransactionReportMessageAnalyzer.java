/**
 * 
 */
package com.dianping.cat.message.consumer.transaction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cat.consumer.model.transaction.entity.TransactionName;
import com.dianping.cat.consumer.model.transaction.entity.TransactionReport;
import com.dianping.cat.consumer.model.transaction.entity.TransactionType;
import com.dianping.cat.consumer.model.transaction.transform.DefaultJsonBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Files;

/**
 * @author sean.wang
 * @since Jan 5, 2012
 */
public class TransactionReportMessageAnalyzer extends AbstractMessageAnalyzer<TransactionReport> {
	private static final Log logger = LogFactory.getLog(TransactionReportMessageAnalyzer.class);

	private TransactionReport report;

	private File reportFile;

	@Override
	protected void store(TransactionReport result) {
		DefaultJsonBuilder builder = new DefaultJsonBuilder();
		report.accept(builder);
		try {
			Files.forIO().writeTo(reportFile, builder.getString());
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	@Override
	public TransactionReport generate() {
		computeMeanSquareDeviation();
		return report;
	}

	@Override
	protected void process(MessageTree tree) {
		if (report == null) {
			this.report = new TransactionReport(tree.getDomain());
		}
		Message message = tree.getMessage();
		process(message, tree.getMessageId());
	}

	private void computeMeanSquareDeviation() {
		Collection<TransactionType> types = report.getTypes().values();

		for (TransactionType transactionType : types) {
			Collection<TransactionName> names = transactionType.getNames().values();
			for (TransactionName name : names) {
				Integer count = name.getTotalCount();
				double ave = name.getSum() / count;
				double std = Math.sqrt(name.getSum2() / count - 2 * ave * ave + ave * ave);
				double failPercent = 100.0 * name.getFailCount() / count;
				name.setFailPercent(failPercent);
				name.setAvg(ave);
				name.setStd(std);
			}
		}
	}

	private void process(Message message, String messageId) {
		if (message instanceof Transaction) {
			Transaction t = (Transaction) message;
			String tType = t.getType();
			String tName = t.getName();
			TransactionType type = report.getTypes().get(tType);
			if (type == null) {
				type = new TransactionType(tType);
				report.addType(type);
			}
			TransactionName name = type.getNames().get(tName);
			if (name == null) {
				name = new TransactionName(tName);
				type.addName(name);
			}
			name.setTotalCount(name.getTotalCount() + 1);
			if (!t.isSuccess()) {
				name.setFailCount(name.getFailCount() + 1);
			}
			if (messageId != null) {
				if (t.isSuccess()) {
					name.setSampleSuccessMessageId(messageId);
				} else {
					name.setSampleFailMessageId(messageId);
				}
			}
			long duration = t.getDuration();
			name.setMax(Math.max(name.getMax(), duration));
			name.setMin(Math.min(name.getMin(), duration));
			name.setSum(name.getSum() + duration);
			name.setSum2(name.getSum2() + duration * duration);
			if (!t.hasChildren()) {
				return;
			}
			List<Message> children = t.getChildren();
			for (Message child : children) {
				process(child, null);
			}
		}

	}

	public File getReportFile() {
		return reportFile;
	}

	public void setReportFile(File reportFile) {
		this.reportFile = reportFile;
	}

	@Override
	protected boolean isTimeout() {
		return false;
	}
}
