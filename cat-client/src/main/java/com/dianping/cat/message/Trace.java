package com.dianping.cat.message;

/**
 * <p>
 * <code>Trace</code> is used to log anything for trace message info happens at a specific time. Such as an debug or info message.
 * </p>
 * 
 * <p>
 * All CAT message will be constructed as a message tree and send to back-end for further analysis, and for monitoring.
 * Only <code>Transaction</code> can be a tree node, all other message will be the tree leaf.　The transaction without
 * other messages nested is an atomic transaction.
 * </p>
 * 
 * @author Frankie Wu
 */
public interface Trace extends Message {

}
