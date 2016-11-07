package com.jotak.stars.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.http.HttpClient;

/**
 * @author Joel Takvorian
 */
public class CrawlerVerticle extends AbstractVerticle {

    public static final String EB_FEED = "stars.feed";
    public static final String EB_LAST_SCAN = "stars.last-scan";
    public static final String EB_POOL_SIZE = "stars.pool-size";

    private static final Pattern URL_FINDER = Pattern.compile("(http[s]?://[\\w_.-]+(:[0-9]+)?(/[\\w_.%&;#:+=-]*)*)");
    private static final Pattern STAR_XXX_FINDER = Pattern.compile("(star[s]? [\\w]+)", Pattern.CASE_INSENSITIVE);

    private final Deque<URL> urls = new RandomInsertLinkedList<>();
    private final Set<URL> fetched = new HashSet<>();

    @Override public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        HttpClient httpClient = vertx.createHttpClient();
        HttpClient httpsClient = vertx.createHttpClient(new HttpClientOptions()
                .setSsl(true)
                .setTrustAll(true));

        final URL startingPoint = new URL("https://en.wikipedia.org/wiki/Comparison_of_Star_Trek_and_Star_Wars");
        urls.push(startingPoint);

        // Scan next every 20 seconds (be fair with wikipedia!)
        vertx.setPeriodic(20000, v -> {
            eb.send(EB_POOL_SIZE, urls.size());
            URL url = urls.poll();
            if (url != null) {
                if (urls.size() > 10000) {
                    urls.clear();
                    fetched.clear();
                }
                final int port;
                final HttpClient clientToUse;
                if (url.getProtocol().equals("https")) {
                    port = (url.getPort() <= 0) ? 443 : url.getPort();
                    clientToUse = httpsClient;
                } else {
                    port = (url.getPort() <= 0) ? 80 : url.getPort();
                    clientToUse = httpClient;
                }
                fetched.add(url);
                clientToUse.getNow(port, url.getHost(), url.getPath(), res -> res.bodyHandler(this::analyseResponse));
                eb.send(EB_LAST_SCAN, url.toString());
            } else {
                urls.push(startingPoint);
                fetched.clear();
            }
        });
    }

    private void analyseResponse(Buffer buffer) {
        EventBus eb = vertx.eventBus();
        vertx.executeBlocking(
                future -> {
                    String content = buffer.getString(0, buffer.length());
                    Matcher matcher = URL_FINDER.matcher(content);
                    while (matcher.find()) {
                        try {
                            URL url = new URL(matcher.group());
                            if (!fetched.contains(url)) {
                                // (Add restriction to Wikipedia to get better results)
                                if (url.getHost().contains("wikipedia")) {
                                    urls.push(url);
                                }
                            }
                        } catch (MalformedURLException e) {
                            // Ignore
                        }
                    }
                    Matcher starsMatcher = STAR_XXX_FINDER.matcher(content);
                    while (starsMatcher.find()) {
                        eb.send(EB_FEED, starsMatcher.group().toLowerCase());
                    }
                    future.complete();
                },
                result -> {}
        );
    }

    private static class RandomInsertLinkedList<T> extends LinkedList<T> {
        @Override public void push(T t) {
            int randomPos = ThreadLocalRandom.current().nextInt(0, 1+size());
            this.add(randomPos, t);
        }
    }
}
