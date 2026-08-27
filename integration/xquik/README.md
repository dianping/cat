# Monitor Xquik Java SDK calls with CAT

This integration records Xquik Java SDK requests as CAT transactions. It wraps
the SDK transport without changing request or response behavior.

Use it to monitor Twitter search, follower exports, webhooks, and X automation
from Java services already running CAT.

## Recorded data

The wrapper records these fields:

- HTTP method
- First path segment, such as `/x`, `/monitors`, or `/webhooks`
- HTTP status code
- Duration and thrown transport errors

It never records full URLs, path identifiers, queries, headers, bodies,
credentials, or response content. The first-segment rule limits cardinality and
keeps user input out of CAT.

Synchronous and asynchronous requests use the `Xquik` transaction type.
Asynchronous completions create duration-correct transactions on their
completion thread without changing future scheduling.
SDK retries appear as separate transport transactions.

## Build

Build and test the integration from the CAT repository root:

```bash
mvn -f integration/xquik/pom.xml verify
```

The test suite uses fake transports. It makes no network requests.

Install the integration into your local Maven repository:

```bash
mvn -f integration/xquik/pom.xml install
```

## Add dependencies

Add the integration and Xquik OkHttp transport:

```xml
<dependency>
  <groupId>com.dianping.cat.integration</groupId>
  <artifactId>cat-xquik</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>com.xquik.api</groupId>
  <artifactId>x-twitter-scraper-java-client-okhttp</artifactId>
  <version>0.10.3</version>
</dependency>
```

Configure CAT for your application before creating the client.

## Create a monitored client

Build the public Xquik client with the wrapped transport:

```java
import com.dianping.cat.integration.xquik.CatXquikHttpClient;
import com.x_twitter_scraper.api.client.XTwitterScraperClient;
import com.x_twitter_scraper.api.client.XTwitterScraperClientImpl;
import com.x_twitter_scraper.api.client.okhttp.OkHttpClient;
import com.x_twitter_scraper.api.core.ClientOptions;
import com.x_twitter_scraper.api.core.http.HttpClient;

HttpClient transport = OkHttpClient.builder().build();
HttpClient monitored = CatXquikHttpClient.wrap(transport);
ClientOptions options = ClientOptions.builder()
    .httpClient(monitored)
    .fromEnv()
    .build();

XTwitterScraperClient client = new XTwitterScraperClientImpl(options);
```

Set `X_TWITTER_SCRAPER_API_KEY` before starting the application. The SDK also
supports OAuth through `X_TWITTER_SCRAPER_BEARER_TOKEN`.

Now use the typed SDK normally:

```java
import com.x_twitter_scraper.api.models.x.tweets.TweetSearchParams;

client.x().tweets().search(
    TweetSearchParams.builder()
        .q("open source monitoring")
        .limit(10L)
        .build()
);
```

Close the Xquik client during application shutdown. It closes the CAT wrapper
and its underlying transport.

Xquik is an independent third-party service. Not affiliated with X Corp.
"Twitter" and "X" are trademarks of X Corp.
