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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.x_twitter_scraper.api.core.RequestOptions;
import com.x_twitter_scraper.api.core.http.HttpClient;
import com.x_twitter_scraper.api.core.http.HttpRequest;
import com.x_twitter_scraper.api.core.http.HttpResponse;

/** Records Xquik SDK HTTP calls as CAT transactions. */
public final class CatXquikHttpClient implements HttpClient {
    private static final XquikCall NOOP_CALL = new XquikCall() {
        @Override
        public void dispatched() {
        }

        @Override
        public void succeeded(int statusCode) {
        }

        @Override
        public void failed(Throwable error) {
        }
    };

    private final HttpClient delegate;
    private final XquikCallRecorder recorder;

    /** Wraps an SDK transport with CAT instrumentation. */
    public CatXquikHttpClient(HttpClient delegate) {
        this(delegate, new XquikCallRecorder());
    }

    CatXquikHttpClient(HttpClient delegate, XquikCallRecorder recorder) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.recorder = Objects.requireNonNull(recorder, "recorder");
    }

    /** Wraps an SDK transport with CAT instrumentation. */
    public static CatXquikHttpClient wrap(HttpClient delegate) {
        return new CatXquikHttpClient(delegate);
    }

    @Override
    public HttpResponse execute(HttpRequest request) {
        return execute(request, RequestOptions.none());
    }

    @Override
    public HttpResponse execute(HttpRequest request, RequestOptions requestOptions) {
        XquikCall call = start(request, false);

        try {
            HttpResponse response = delegate.execute(request, requestOptions);

            succeed(call, response);
            return response;
        } catch (RuntimeException error) {
            fail(call, error);
            throw error;
        }
    }

    @Override
    public CompletableFuture<HttpResponse> executeAsync(HttpRequest request) {
        return executeAsync(request, RequestOptions.none());
    }

    @Override
    public CompletableFuture<HttpResponse> executeAsync(HttpRequest request, RequestOptions requestOptions) {
        XquikCall call = start(request, true);
        CompletableFuture<HttpResponse> response;

        try {
            response = delegate.executeAsync(request, requestOptions);
            dispatch(call);
        } catch (RuntimeException error) {
            fail(call, error);
            throw error;
        }

        return response.whenComplete((result, error) -> {
            if (error != null) {
                fail(call, unwrap(error));
            } else if (result != null) {
                succeed(call, result);
            } else {
                fail(call, new IllegalStateException("Xquik transport returned no response"));
            }
        });
    }

    @Override
    public void close() {
        delegate.close();
    }

    private static String operation(HttpRequest request) {
        List<String> segments = request.pathSegments();
        String group = segments.isEmpty() ? "/" : "/" + segments.get(0);

        return request.method().name() + " " + group;
    }

    private static Throwable unwrap(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }
        return error;
    }

    private XquikCall start(HttpRequest request, boolean asynchronous) {
        try {
            return recorder.start(operation(request), asynchronous);
        } catch (RuntimeException ignored) {
            return NOOP_CALL;
        }
    }

    private static void dispatch(XquikCall call) {
        try {
            call.dispatched();
        } catch (RuntimeException ignored) {
        }
    }

    private static void succeed(XquikCall call, HttpResponse response) {
        try {
            call.succeeded(response.statusCode());
        } catch (RuntimeException ignored) {
        }
    }

    private static void fail(XquikCall call, Throwable error) {
        try {
            call.failed(error);
        } catch (RuntimeException ignored) {
        }
    }
}
