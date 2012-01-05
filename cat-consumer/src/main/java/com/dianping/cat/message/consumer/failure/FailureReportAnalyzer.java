package com.dianping.cat.message.consumer.failure;

import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.model.failure.entity.Entry;
import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.entity.Segment;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class FailureReportAnalyzer extends
		AbstractMessageAnalyzer<FailureReport> {

	private FailureReport report = new FailureReport();
	private String domain;
	private long start;
	private long duration;
	
	private String path="/data/appdatas/cat/report/failure";
	private String failure="error,exception";
	private static final String EXCEPTION = "exception";
	private static final String ERROR = "error";

	@Override
	protected void store(FailureReport result) {
		// TODO Auto-generated method stub
		
	}
    
	public static interface Accept{
		//current node
		boolean accept(Message message);
	}
	
	@Override
	public FailureReport generate() {
		// TODO Auto-generated method stub
		return null;
	}
    
	
	@Override
	protected void process(MessageTree tree) {
		// 更新Domain、更新StartTime，更新EndTime，更新machines
		Message message = tree.getMessage();
		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;
			processTransaction(transaction, tree);
		}else if(message instanceof Event){
			processEvent(message,tree);
		}
	}

	private void processTransaction(Transaction transaction, MessageTree tree) {
		List<Message> messageList = transaction.getChildren();
		// process myself
		
		
		for (Message message : messageList) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;
				processTransaction(temp, tree);
			} else if (message instanceof Event) {
				processEvent(message, tree);
			}
		}
	}

	private void processEvent(Message message, MessageTree tree) {
		String messageId = ((DefaultMessageTree) tree).getMessageId();
		String threadId = ((DefaultMessageTree) tree).getThreadId();
		String type = message.getType();
		if (type.equalsIgnoreCase(EXCEPTION) || type.equalsIgnoreCase(ERROR)) {
			Entry entry = new Entry();
			entry.setMessageId(messageId);
			entry.setThreadId(threadId);
			entry.setText(message.getData().toString());
			entry.setType(message.getType());
			long time = message.getTimestamp();
			Map<String, Segment> segmentMap = report.getSegments();
			Segment segment = segmentMap.get(String.valueOf(time));
			if (segment == null) {
				Segment temp = new Segment(String.valueOf(time));
				temp.addEntry(entry);
				segmentMap.put(String.valueOf(time), temp);
			} else {
				segment.addEntry(entry);
			}
		}
	}
}
