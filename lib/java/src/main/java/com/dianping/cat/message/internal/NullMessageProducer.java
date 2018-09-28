package com.dianping.cat.message.internal;

import com.dianping.cat.message.*;
import com.dianping.cat.message.spi.internal.NullMessageTree;

import java.util.concurrent.atomic.AtomicInteger;

public class NullMessageProducer implements MessageProducer {
    public static final NullMessageProducer NULL_MESSAGE_PRODUCER = new NullMessageProducer();
    private AtomicInteger seq = new AtomicInteger(0);

    @Override
    public String createMessageId() {
        return NullMessageTree.UNKNOWN + "-00000000-000000-" + seq.get();
    }

    @Override
    public String createRpcServerId(String domain) {
        return createMessageId();
    }

    @Override
    public void logError(String message, Throwable cause) {

    }

    @Override
    public void logError(Throwable cause) {

    }

    @Override
    public void logErrorWithCategory(String category, String message, Throwable cause) {
    }

    @Override
    public void logErrorWithCategory(String category, Throwable cause) {
    }

    @Override
    public void logEvent(String type, String name) {
    }

    @Override
    public void logEvent(String type, String name, String status, String nameValuePairs) {
    }

    @Override
    public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
    }

    @Override
    public void logMetric(String name, String status, String nameValuePairs) {
    }

    @Override
    public Event newEvent(String type, String name) {
        return NullMessage.EVENT;
    }

    @Override
    public Heartbeat newHeartbeat(String type, String name) {
        return NullMessage.HEARTBEAT;
    }

    @Override
    public Metric newMetric(String type, String name) {
        return NullMessage.METRIC;
    }

    @Override
    public Trace newTrace(String type, String name) {
        return NullMessage.TRACE;
    }

    @Override
    public Transaction newTransaction(String type, String name) {
        return NullMessage.TRANSACTION;
    }

}
