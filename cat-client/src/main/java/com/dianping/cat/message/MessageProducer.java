package com.dianping.cat.message;

/**
 * <p>
 * Message factory is used to create new transaction,event and/or heartbeat.
 * </p>
 * 
 * <p>
 * Normally, application code logs message in following ways, for example:
 * <ul>
 * <li>Event
 * 
 * <pre>
 * public class MyClass { 
 *    public static MessageFactory CAT = Cat.getFactory();
 * 
 *    public void bizMethod() { 
 *       Event event = CAT.newEvent("Review", "New");
 * 
 *       event.addData("id", 12345); 
 *       event.addData("user", "john");
 *       ...
 *       event.setStatus("0"); 
 *       event.complete(); 
 *    }
 *    ...
 * }
 * </pre>
 * 
 * </li>
 * <li>Heartbeat
 * 
 * <pre>
 * public class MyClass { 
 *    public static MessageFactory CAT = Cat.getFactory();
 * 
 *    public void bizMethod() { 
 *       Heartbeat event = CAT.newHeartbeat("System", "Status");
 * 
 *       event.addData("ip", "192.168.10.111");
 *       event.addData("host", "host-1");
 *       event.addData("load", "2.1");
 *       event.addData("cpu", "0.12,0.10");
 *       event.addData("memory.total", "2G");
 *       event.addData("memory.free", "456M");
 *       event.setStatus("0");
 *       event.complete();
 *    }
 *    ...
 * }
 * </pre>
 * 
 * </li>
 * <li>Transaction
 * 
 * <pre>
 * public class MyClass { 
 *    public static MessageFactory CAT = Cat.getFactory();
 * 
 *    public void bizMethod() { 
 *       Transaction t = CAT.newTransaction("URL", "MyPage");
 * 
 *       try {
 *          // do your business here
 *          t.addData("k1", "v1");
 *          t.addData("k2", "v2");
 *          t.addData("k3", "v3");
 *          Thread.sleep(30);
 * 
 *          t.setStatus("0");
 *       } catch (Exception e) {
 *          t.setStatus(e);
 *       } finally {
 *          t.complete();
 *       }
 *    }
 *    ...
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * 
 * or logs event or heartbeat in one shot, for example:
 * <ul>
 * <li>Event
 * 
 * <pre>
 * public class MyClass { 
 *    public static MessageFactory CAT = Cat.getFactory();
 * 
 *    public void bizMethod() { 
 *       CAT.logEvent("Review", "New", "0", "id=12345&user=john");
 *    }
 *    ...
 * }
 * </pre>
 * 
 * </li>
 * <li>Heartbeat
 * 
 * <pre>
 * public class MyClass { 
 *    public static MessageFactory CAT = Cat.getFactory();
 * 
 *    public void bizMethod() { 
 *       CAT.logHeartbeat("System", "Status", "0", "ip=192.168.10.111&host=host-1&load=2.1&cpu=0.12,0.10&memory.total=2G&memory.free=456M");
 *    }
 *    ...
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * </p>
 * 
 * @author Frankie Wu
 */
public interface MessageProducer {
	/**
	 * Create rpc server message id.
	 * 
	 * domain is the rpc server 
	 * @return new message id
	 */
	public String createRpcServerId(String domain);

	/**
	 * Create a new message id.
	 * 
	 * @return new message id
	 */
	public String createMessageId();

	/**
	 * Check if the CAT client is enabled for current domain.
	 * 
	 * @return true if CAT client is enabled, false means CAT client is disabled.
	 */
	public boolean isEnabled();

	/**
	 * Log an error.
	 * 
	 * @param cause
	 *           root cause exception
	 */
	public void logError(String message, Throwable cause);

	/**
	 * Log an error.
	 * 
	 * @param cause
	 *           root cause exception
	 */
	public void logError(Throwable cause);

	/**
	 * Log an event in one shot with SUCCESS status.
	 * 
	 * @param type
	 *           event type
	 * @param name
	 *           event name
	 */
	public void logEvent(String type, String name);

	/**
	 * Log an event in one shot.
	 * 
	 * @param type
	 *           event type
	 * @param name
	 *           event name
	 * @param status
	 *           "0" means success, otherwise means error code
	 * @param nameValuePairs
	 *           name value pairs in the format of "a=1&b=2&..."
	 */
	public void logEvent(String type, String name, String status, String nameValuePairs);

	/**
	 * Log a heartbeat in one shot.
	 * 
	 * @param type
	 *           heartbeat type
	 * @param name
	 *           heartbeat name
	 * @param status
	 *           "0" means success, otherwise means error code
	 * @param nameValuePairs
	 *           name value pairs in the format of "a=1&b=2&..."
	 */
	public void logHeartbeat(String type, String name, String status, String nameValuePairs);

	/**
	 * Log a metric in one shot.
	 * 
	 * @param name
	 *           metric name
	 * @param status
	 *           "0" means success, otherwise means error code
	 * @param nameValuePairs
	 *           name value pairs in the format of "a=1&b=2&..."
	 */
	public void logMetric(String name, String status, String nameValuePairs);

	/**
	 * Log an trace in one shot with SUCCESS status.
	 * 
	 * @param type
	 *           trace type
	 * @param name
	 *           trace name
	 */
	public void logTrace(String type, String name);

	/**
	 * Log an trace in one shot.
	 * 
	 * @param type
	 *           trace type
	 * @param name
	 *           trace name
	 * @param status
	 *           "0" means success, otherwise means error code
	 * @param nameValuePairs
	 *           name value pairs in the format of "a=1&b=2&..."
	 */
	public void logTrace(String type, String name, String status, String nameValuePairs);

	/**
	 * Create a new event with given type and name.
	 * 
	 * @param type
	 *           event type
	 * @param name
	 *           event name
	 */
	public Event newEvent(String type, String name);

	/**
	 * Create a forked transaction for child thread.
	 * 
	 * @param type
	 *           transaction type
	 * @param name
	 *           transaction name
	 * @return forked transaction
	 */
	public ForkedTransaction newForkedTransaction(String type, String name);

	/**
	 * Create a new heartbeat with given type and name.
	 * 
	 * @param type
	 *           heartbeat type
	 * @param name
	 *           heartbeat name
	 */
	public Heartbeat newHeartbeat(String type, String name);

	/**
	 * Create a new metric with given type and name.
	 * 
	 * @param type
	 *           metric type
	 * @param name
	 *           metric name
	 */
	public Metric newMetric(String type, String name);

	/**
	 * Create a tagged transaction for another process or thread.
	 * 
	 * @param type
	 *           transaction type
	 * @param name
	 *           transaction name
	 * @param tag
	 *           tag applied to the transaction
	 * @return tagged transaction
	 */
	public TaggedTransaction newTaggedTransaction(String type, String name, String tag);

	/**
	 * Create a new trace with given type and name.
	 * 
	 * @param type
	 *           trace type
	 * @param name
	 *           trace name
	 */
	public Trace newTrace(String type, String name);

	/**
	 * Create a new transaction with given type and name.
	 * 
	 * @param type
	 *           transaction type
	 * @param name
	 *           transaction name
	 */
	public Transaction newTransaction(String type, String name);
}
