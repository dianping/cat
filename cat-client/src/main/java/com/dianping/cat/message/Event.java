package com.dianping.cat.message;

/**
 * <p>
 * <code>Event</code> is used to log anything interesting happens at a specific time. Such as an exception thrown, a
 * review added by user, a new user registered, an user logged into the system etc.
 * </p>
 * 
 * <p>
 * However, if it could be failure, or last for a long time, such as a remote API call, database call or search engine
 * call etc. It should be logged as a <code>Transaction</code>
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
public interface Event extends Message {

}
