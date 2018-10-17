/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.message;

public interface MessageProducer {
    /**
     * Create a new message id.
     *
     * @return new message id
     */
    String createMessageId();

    /**
     * Create rpc server message id.
     * <p>
     * domain is the rpc server
     *
     * @return new message id
     */
    String createRpcServerId(String domain);

    /**
     * Log an error.
     *
     * @param cause root cause exception
     */
    void logError(String message, Throwable cause);

    /**
     * Log an error.
     *
     * @param cause root cause exception
     */
    void logError(Throwable cause);

    /**
     * Log an event in one shot with SUCCESS status.
     *
     * @param type
     *           event type
     * @param name
     *           event name
     */

    /**
     * Log an error.
     *
     * @param cause root cause exception
     */
    void logErrorWithCategory(String category, String message, Throwable cause);

    /**
     * Log an error.
     *
     * @param cause root cause exception
     */
    void logErrorWithCategory(String category, Throwable cause);

    /**
     * Log an event in one shot with SUCCESS status.
     *
     * @param type event type
     * @param name event name
     */

    void logEvent(String type, String name);

    /**
     * Log an event in one shot.
     *
     * @param type           event type
     * @param name           event name
     * @param status         "0" means success, otherwise means error code
     * @param nameValuePairs name value pairs in the format of "a=1&b=2&..."
     */
    void logEvent(String type, String name, String status, String nameValuePairs);

    /**
     * Log a heartbeat in one shot.
     *
     * @param type           heartbeat type
     * @param name           heartbeat name
     * @param status         "0" means success, otherwise means error code
     * @param nameValuePairs name value pairs in the format of "a=1&b=2&..."
     */
    void logHeartbeat(String type, String name, String status, String nameValuePairs);

    /**
     * Log a metric in one shot.
     *
     * @param name           metric name
     * @param status         "0" means success, otherwise means error code
     * @param nameValuePairs name value pairs in the format of "a=1&b=2&..."
     */
    void logMetric(String name, String status, String nameValuePairs);

    /**
     * Create a new event with given type and name.
     *
     * @param type event type
     * @param name event name
     */
    Event newEvent(String type, String name);

    /**
     * Create a new heartbeat with given type and name.
     *
     * @param type heartbeat type
     * @param name heartbeat name
     */
    Heartbeat newHeartbeat(String type, String name);

    /**
     * Create a new metric with given type and name.
     *
     * @param type metric type
     * @param name metric name
     */
    Metric newMetric(String type, String name);

    /**
     * Create a new trace with given type and name.
     *
     * @param type trace type
     * @param name trace name
     */
    Trace newTrace(String type, String name);

    /**
     * Create a new transaction with given type and name.
     *
     * @param type transaction type
     * @param name transaction name
     */
    Transaction newTransaction(String type, String name);
}
