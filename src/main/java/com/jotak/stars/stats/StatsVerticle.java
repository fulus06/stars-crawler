package com.jotak.stars.stats;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.jotak.stars.RunningHost;
import com.jotak.stars.crawler.CrawlerVerticle;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import rx.Observable;

/**
 * @author Joel Takvorian
 */
public class StatsVerticle extends AbstractVerticle {

    public static final String EB_PUSH_STATS = "stars.stats";

    private final Multiset<String> raw = HashMultiset.create();
    private final AtomicInteger poolSize = new AtomicInteger(0);
    private final AtomicReference<String> lastURL = new AtomicReference<>("");
    private long runSince = System.currentTimeMillis();

    @Override public void start() throws Exception {
        runSince = System.currentTimeMillis();
        EventBus eb = vertx.eventBus();
        Observable<Object> oFeed = eb.consumer(CrawlerVerticle.EB_FEED).bodyStream().toObservable()
                .doOnNext(body -> raw.add(body.toString()));
        Observable<Object> oPool = eb.consumer(CrawlerVerticle.EB_POOL_SIZE).bodyStream().toObservable()
                .doOnNext(body -> poolSize.set((int) body));
        Observable<Object> oLast = eb.consumer(CrawlerVerticle.EB_LAST_SCAN).bodyStream().toObservable()
                .doOnNext(body -> lastURL.set(body.toString()));

        // When any of these observables get emitted items, push stats, with 1 second time buffer
        oFeed.mergeWith(oPool).mergeWith(oLast)
                .buffer(1, TimeUnit.SECONDS)
                .filter(list -> !list.isEmpty())
                .subscribe(obj -> eb.send(EB_PUSH_STATS, json()));
    }

    private JsonObject json() {
        final List<JsonObject> all = raw.elementSet().stream()
                .map(e -> new JsonObject().put("name", e).put("count", raw.count(e)))
                .sorted((e1, e2) -> e2.getInteger("count") - e1.getInteger("count"))
                .collect(Collectors.toList());
        return new JsonObject()
                .put("since", new Date(runSince).toString())
                .put("id", RunningHost.getHostId())
                .put("poolSize", poolSize.get())
                .put("lastURL", lastURL.get())
                .put("unique", raw.elementSet().size())
                .put("all", new JsonArray(all));
    }
}
