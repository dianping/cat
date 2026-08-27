/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.integration.xquik;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class XquikCallRecorderTest {
    @Test
    public void recordsSynchronousSuccessOnce() {
        TransactionProbe probe = new TransactionProbe(Transaction.class);
        RecordingRecorder recorder = new RecordingRecorder(probe.transaction());
        XquikCall call = recorder.start("GET /x", false);

        call.succeeded(204);
        call.failed(new IllegalStateException("late failure"));

        assertEquals("Xquik", recorder.type);
        assertEquals("GET /x", recorder.operation);
        assertEquals(Integer.valueOf(204), probe.data.get("http.status_code"));
        assertEquals(Message.SUCCESS, probe.status);
        assertNull(probe.error);
        assertEquals(1, probe.completions);
    }

    @Test
    public void recordsSynchronousFailureOnce() {
        TransactionProbe probe = new TransactionProbe(Transaction.class);
        RecordingRecorder recorder = new RecordingRecorder(probe.transaction());
        IllegalArgumentException failure = new IllegalArgumentException("transport failed");
        XquikCall call = recorder.start("POST /webhooks", false);

        call.failed(failure);
        call.succeeded(200);

        assertSame(failure, probe.error);
        assertNull(probe.status);
        assertEquals(1, probe.completions);
    }

    @Test
    public void recordsAsynchronousSuccessAfterDispatch() {
        TransactionProbe probe = new TransactionProbe(Transaction.class);
        RecordingRecorder recorder = new RecordingRecorder(probe.transaction());
        XquikCall call = recorder.start("GET /monitors", true);

        call.succeeded(200);
        assertEquals(0, probe.completions);

        call.dispatched();
        call.succeeded(202);
        call.failed(new IllegalStateException("late failure"));

        assertTrue(probe.durationMillis >= 0L);
        assertTrue(probe.timestamp > 0L);
        assertEquals(Integer.valueOf(202), probe.data.get("http.status_code"));
        assertEquals(Message.SUCCESS, probe.status);
        assertNull(probe.error);
        assertEquals(1, probe.completions);
    }

    @Test
    public void recordsAsynchronousHttpStatusFailure() {
        TransactionProbe probe = new TransactionProbe(Transaction.class);
        RecordingRecorder recorder = new RecordingRecorder(probe.transaction());
        XquikCall call = recorder.start("GET /credits", true);

        call.dispatched();
        call.succeeded(503);

        assertEquals("HTTP_503", probe.status);
        assertEquals(Integer.valueOf(503), probe.data.get("http.status_code"));
        assertEquals(1, probe.completions);
    }

    @Test
    public void recordsAsynchronousDispatchFailureOnTheCallingThread() {
        TransactionProbe probe = new TransactionProbe(Transaction.class);
        RecordingRecorder recorder = new RecordingRecorder(probe.transaction());
        IllegalStateException failure = new IllegalStateException("dispatch failed");
        XquikCall call = recorder.start("POST /extractions", true);

        call.failed(failure);

        assertSame(failure, probe.error);
        assertEquals(1, probe.completions);
    }

    private static final class RecordingRecorder extends XquikCallRecorder {
        private final Transaction transaction;
        private String operation;
        private String type;

        private RecordingRecorder(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        Transaction newTransaction(String type, String operation) {
            this.type = type;
            this.operation = operation;
            return transaction;
        }
    }

    private static final class TransactionProbe implements InvocationHandler {
        private final Map<String, Object> data = new HashMap<>();
        private final Class<?> transactionType;
        private int completions;
        private long durationMillis = -1L;
        private Throwable error;
        private String status;
        private long timestamp;
        private Object proxy;

        private TransactionProbe(Class<?> transactionType) {
            this.transactionType = transactionType;
        }

        private Transaction transaction() {
            return (Transaction) proxy();
        }

        private Object proxy() {
            if (proxy == null) {
                proxy = Proxy.newProxyInstance(
                        transactionType.getClassLoader(), new Class<?>[] {transactionType}, this);
            }
            return proxy;
        }

        @Override
        public Object invoke(Object ignored, Method method, Object[] arguments) {
            String name = method.getName();

            if ("addData".equals(name) && arguments.length == 2) {
                data.put(String.valueOf(arguments[0]), arguments[1]);
            } else if ("complete".equals(name)) {
                completions++;
            } else if ("setDurationInMillis".equals(name)) {
                durationMillis = (Long) arguments[0];
            } else if ("setTimestamp".equals(name)) {
                timestamp = (Long) arguments[0];
            } else if ("setStatus".equals(name) && arguments[0] instanceof Throwable) {
                error = (Throwable) arguments[0];
            } else if ("setStatus".equals(name)) {
                status = String.valueOf(arguments[0]);
            }

            Class<?> returnType = method.getReturnType();
            if (returnType.isInstance(proxy)) {
                return proxy;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            return null;
        }
    }
}
