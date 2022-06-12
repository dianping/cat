package com.dianping.cat.message;

/**
 * Bulk transaction to indicate same transaction happened many times within a certain period.
 * 
 * @author Frankie Wu(qmwu2000@gmail.com)
 */
public interface BulkTransaction extends Transaction {
   public BulkTransaction addDuration(int success, int failed, long sumOfDurationInMillis);
}
