package com.dianping.cat.message;

/**
 * <p>
 * <code>Heartbeat</code> is used to log data that happens in a regular intervals, for example once per second, such as
 * system load, CPU percentage, memory usage, thread pool statistics, cache hit/miss rate, service manifest etc., and
 * even some configuration could be carried by <code>Heartbeat</code>. There could be some good use cases, for example
 * health checker and load balancer, that make good use of it.
 * </p>
 * 
 * <p>
 * <code>Heartbeat</code> should never be used per request since the request is not regular predictable, instead it
 * could be logged in a daemon background thread, or something like a Timer.
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
public interface Heartbeat extends Message {

}
