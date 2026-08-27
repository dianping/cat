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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.Test;

import com.x_twitter_scraper.api.client.XTwitterScraperClient;
import com.x_twitter_scraper.api.client.XTwitterScraperClientImpl;
import com.x_twitter_scraper.api.core.ClientOptions;
import com.x_twitter_scraper.api.core.RequestOptions;
import com.x_twitter_scraper.api.core.http.Headers;
import com.x_twitter_scraper.api.core.http.HttpClient;
import com.x_twitter_scraper.api.core.http.HttpMethod;
import com.x_twitter_scraper.api.core.http.HttpRequest;
import com.x_twitter_scraper.api.core.http.HttpResponse;

public class CatXquikHttpClientTest {
    @Test
    public void rejectsNullDependencies() {
        FakeHttpClient delegate = new FakeHttpClient();

        assertThrows(NullPointerException.class, () -> new CatXquikHttpClient(null));
        assertThrows(NullPointerException.class, () -> new CatXquikHttpClient(delegate, null));
    }

    @Test
    public void recordsSynchronousSuccessAndPreservesArguments() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        HttpResponse response = new FakeHttpResponse(200);
        RequestOptions options = RequestOptions.builder().responseValidation(true).build();
        HttpRequest request = request("x", "tweets", "private-id");

        delegate.response = response;
        HttpResponse actual = new CatXquikHttpClient(delegate, recorder).execute(request, options);

        assertSame(response, actual);
        assertSame(request, delegate.request);
        assertSame(options, delegate.options);
        assertEquals("GET /x", recorder.operation);
        assertFalse(recorder.asynchronous);
        assertEquals(Integer.valueOf(200), recorder.call.statusCode);
        assertNull(recorder.call.error);
    }

    @Test
    public void recordsSynchronousHttpFailureWithoutReplacingTheResponse() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        HttpResponse response = new FakeHttpResponse(429);

        delegate.response = response;
        HttpResponse actual = new CatXquikHttpClient(delegate, recorder).execute(request("credits"));

        assertSame(response, actual);
        assertEquals(Integer.valueOf(429), recorder.call.statusCode);
        assertNull(recorder.call.error);
    }

    @Test
    public void recordsAndRethrowsSynchronousTransportFailure() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        IllegalStateException failure = new IllegalStateException("transport failed");

        delegate.failure = failure;
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new CatXquikHttpClient(delegate, recorder).execute(request("monitors")));

        assertSame(failure, actual);
        assertSame(failure, recorder.call.error);
        assertNull(recorder.call.statusCode);
    }

    @Test
    public void isolatesRecorderStartFailure() {
        FakeHttpClient delegate = new FakeHttpClient();
        HttpResponse response = new FakeHttpResponse(204);

        delegate.response = response;
        XquikCallRecorder recorder = new XquikCallRecorder() {
            @Override
            XquikCall start(String operation, boolean asynchronous) {
                throw new IllegalStateException("CAT unavailable");
            }
        };

        assertSame(response, new CatXquikHttpClient(delegate, recorder).execute(request("events")));
    }

    @Test
    public void isolatesRecorderCompletionFailure() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        HttpResponse response = new FakeHttpResponse(200);

        delegate.response = response;
        recorder.call.completionFailure = new IllegalStateException("CAT unavailable");

        assertSame(response, new CatXquikHttpClient(delegate, recorder).execute(request("styles")));
    }

    @Test
    public void recordsAsynchronousSuccessAfterDispatch() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        HttpResponse response = new FakeHttpResponse(202);
        CompletableFuture<HttpResponse> pending = new CompletableFuture<>();

        delegate.future = pending;
        CompletableFuture<HttpResponse> result =
                new CatXquikHttpClient(delegate, recorder).executeAsync(request("extractions"));

        assertTrue(recorder.asynchronous);
        assertTrue(recorder.call.dispatched);
        assertNull(recorder.call.statusCode);

        pending.complete(response);

        assertSame(response, result.join());
        assertEquals(Integer.valueOf(202), recorder.call.statusCode);
        assertNull(recorder.call.error);
    }

    @Test
    public void recordsAsynchronousHttpFailureWithoutReplacingTheResponse() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        HttpResponse response = new FakeHttpResponse(503);

        delegate.future = CompletableFuture.completedFuture(response);
        HttpResponse actual = new CatXquikHttpClient(delegate, recorder)
                .executeAsync(request("webhooks"), RequestOptions.none())
                .join();

        assertSame(response, actual);
        assertEquals(Integer.valueOf(503), recorder.call.statusCode);
    }

    @Test
    public void unwrapsAsynchronousTransportFailure() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        IllegalArgumentException failure = new IllegalArgumentException("bad response");
        CompletableFuture<HttpResponse> failed = new CompletableFuture<>();

        failed.completeExceptionally(new CompletionException(failure));
        delegate.future = failed;
        CompletableFuture<HttpResponse> result =
                new CatXquikHttpClient(delegate, recorder).executeAsync(request("x"));

        assertThrows(CompletionException.class, result::join);
        assertSame(failure, recorder.call.error);
        assertNull(recorder.call.statusCode);
    }

    @Test
    public void recordsAndRethrowsSynchronousAsyncDispatchFailure() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        IllegalStateException failure = new IllegalStateException("dispatch failed");

        delegate.failure = failure;
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new CatXquikHttpClient(delegate, recorder).executeAsync(request("radar")));

        assertSame(failure, actual);
        assertSame(failure, recorder.call.error);
        assertFalse(recorder.call.dispatched);
    }

    @Test
    public void recordsMissingAsynchronousResponseWithoutChangingTheResult() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();

        delegate.future = CompletableFuture.completedFuture(null);
        HttpResponse actual = new CatXquikHttpClient(delegate, recorder)
                .executeAsync(request("account"))
                .join();

        assertNull(actual);
        assertEquals("Xquik transport returned no response", recorder.call.error.getMessage());
    }

    @Test
    public void isolatesAsynchronousRecorderFailure() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();
        HttpResponse response = new FakeHttpResponse(200);

        recorder.call.completionFailure = new IllegalStateException("CAT unavailable");
        delegate.future = CompletableFuture.completedFuture(response);

        assertSame(response, new CatXquikHttpClient(delegate, recorder)
                .executeAsync(request("support", "tickets"))
                .join());
    }

    @Test
    public void usesOnlyTheFirstPathSegmentForTheOperationName() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();

        delegate.response = new FakeHttpResponse(200);
        new CatXquikHttpClient(delegate, recorder).execute(request("users", "secret-username", "tweets"));

        assertEquals("GET /users", recorder.operation);
    }

    @Test
    public void handlesRequestsWithoutPathSegments() {
        FakeHttpClient delegate = new FakeHttpClient();
        FakeRecorder recorder = new FakeRecorder();

        delegate.response = new FakeHttpResponse(200);
        new CatXquikHttpClient(delegate, recorder).execute(request());

        assertEquals("GET /", recorder.operation);
    }

    @Test
    public void closesTheWrappedTransport() {
        FakeHttpClient delegate = new FakeHttpClient();

        CatXquikHttpClient.wrap(delegate).close();

        assertTrue(delegate.closed);
    }

    @Test
    public void buildsTheDocumentedSdkClient() {
        FakeHttpClient delegate = new FakeHttpClient();
        ClientOptions options = ClientOptions.builder()
                .httpClient(new CatXquikHttpClient(delegate, new FakeRecorder()))
                .apiKey("test-key")
                .build();
        XTwitterScraperClient client = new XTwitterScraperClientImpl(options);

        client.close();

        assertTrue(delegate.closed);
    }

    private static HttpRequest request(String... pathSegments) {
        return HttpRequest.builder()
                .method(HttpMethod.GET)
                .baseUrl("https://xquik.com/api/v1")
                .addPathSegments(pathSegments)
                .putHeader("Authorization", "secret")
                .putQueryParam("q", "private query")
                .build();
    }

    private static final class FakeRecorder extends XquikCallRecorder {
        private boolean asynchronous;
        private FakeCall call = new FakeCall();
        private String operation;

        @Override
        XquikCall start(String operation, boolean asynchronous) {
            this.operation = operation;
            this.asynchronous = asynchronous;
            return call;
        }
    }

    private static final class FakeCall implements XquikCall {
        private boolean dispatched;
        private RuntimeException completionFailure;
        private Throwable error;
        private Integer statusCode;

        @Override
        public void dispatched() {
            dispatched = true;
            throwIfConfigured();
        }

        @Override
        public void succeeded(int statusCode) {
            this.statusCode = statusCode;
            throwIfConfigured();
        }

        @Override
        public void failed(Throwable error) {
            this.error = error;
            throwIfConfigured();
        }

        private void throwIfConfigured() {
            if (completionFailure != null) {
                throw completionFailure;
            }
        }
    }

    private static final class FakeHttpClient implements HttpClient {
        private boolean closed;
        private RuntimeException failure;
        private CompletableFuture<HttpResponse> future;
        private RequestOptions options;
        private HttpRequest request;
        private HttpResponse response;

        @Override
        public HttpResponse execute(HttpRequest request) {
            return execute(request, RequestOptions.none());
        }

        @Override
        public HttpResponse execute(HttpRequest request, RequestOptions options) {
            this.request = request;
            this.options = options;
            if (failure != null) {
                throw failure;
            }
            return response;
        }

        @Override
        public CompletableFuture<HttpResponse> executeAsync(HttpRequest request) {
            return executeAsync(request, RequestOptions.none());
        }

        @Override
        public CompletableFuture<HttpResponse> executeAsync(HttpRequest request, RequestOptions options) {
            this.request = request;
            this.options = options;
            if (failure != null) {
                throw failure;
            }
            return future;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class FakeHttpResponse implements HttpResponse {
        private final int statusCode;

        private FakeHttpResponse(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public Headers headers() {
            return Headers.builder().build();
        }

        @Override
        public InputStream body() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void close() {
        }
    }
}
