package com.dianping.cat.message;

/**
 * Bulk event to indicate same event happened many times within a certain period.
 *
 * @author Frankie Wu(qmwu2000@gmail.com)
 */
public interface BulkEvent extends Event {
   public BulkEvent addCount(int success, int failed);

}
