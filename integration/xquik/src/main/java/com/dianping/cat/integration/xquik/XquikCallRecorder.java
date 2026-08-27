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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

interface XquikCall {
    void dispatched();

    void succeeded(int statusCode);

    void failed(Throwable error);
}

class XquikCallRecorder {
    private static final String TYPE = "Xquik";

    XquikCall start(String operation, boolean asynchronous) {
        if (asynchronous) {
            return new AsyncCall(operation);
        }
        return new SyncCall(newTransaction(TYPE, operation));
    }

    Transaction newTransaction(String type, String operation) {
        return Cat.newTransaction(type, operation);
    }

    private static void complete(Transaction transaction, int statusCode) {
        transaction.addData("http.status_code", statusCode);
        transaction.setStatus(statusCode >= 200 && statusCode < 400 ? Message.SUCCESS : "HTTP_" + statusCode);
        transaction.complete();
    }

    private static void fail(Transaction transaction, Throwable error) {
        transaction.setStatus(error);
        transaction.complete();
    }

    private static final class SyncCall implements XquikCall {
        private final AtomicBoolean completed = new AtomicBoolean();
        private final Transaction transaction;

        private SyncCall(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void dispatched() {
        }

        @Override
        public void succeeded(int statusCode) {
            if (completed.compareAndSet(false, true)) {
                complete(transaction, statusCode);
            }
        }

        @Override
        public void failed(Throwable error) {
            if (completed.compareAndSet(false, true)) {
                fail(transaction, error);
            }
        }
    }

    private final class AsyncCall implements XquikCall {
        private static final int STARTED = 0;
        private static final int DISPATCHED = 1;
        private static final int COMPLETED = 2;

        private final String operation;
        private final AtomicInteger state = new AtomicInteger(STARTED);
        private final long startedAtNanos = System.nanoTime();

        private AsyncCall(String operation) {
            this.operation = operation;
        }

        @Override
        public void dispatched() {
            state.compareAndSet(STARTED, DISPATCHED);
        }

        @Override
        public void succeeded(int statusCode) {
            if (state.compareAndSet(DISPATCHED, COMPLETED)) {
                recordCompletion(statusCode, null);
            }
        }

        @Override
        public void failed(Throwable error) {
            if (state.compareAndSet(STARTED, COMPLETED)) {
                recordCompletion(0, error);
            } else if (state.compareAndSet(DISPATCHED, COMPLETED)) {
                recordCompletion(0, error);
            }
        }

        private void recordCompletion(int statusCode, Throwable error) {
            long durationMillis = (System.nanoTime() - startedAtNanos) / 1_000_000L;
            Transaction transaction = newTransaction(TYPE, operation);

            transaction.setDurationInMillis(durationMillis);
            transaction.setTimestamp(System.currentTimeMillis() - durationMillis);

            if (error == null) {
                complete(transaction, statusCode);
            } else {
                fail(transaction, error);
            }
        }
    }
}
