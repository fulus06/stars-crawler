package com.jotak.stars.stats;

import static com.jotak.stars.common.EventBusAddresses.EB_FEED;
import static com.jotak.stars.common.EventBusAddresses.EB_LAST_SCAN;
import static com.jotak.stars.common.EventBusAddresses.EB_POOL_SIZE;
import static com.jotak.stars.common.EventBusAddresses.EB_PUSH_STATS;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.jotak.stars.common.Cluster;
import com.jotak.stars.common.RunningHost;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import rx.Observable;

/**
 * @author Joel Takvorian
 */
public class StatsVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(StatsVerticle.class);
    private final Multiset<String> raw = HashMultiset.create();

    @Override public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        Observable<JsonObject> oFeed = eb.consumer(EB_FEED).bodyStream().toObservable()
                .doOnNext(body -> raw.add(body.toString()))
                // Compute later, after buffering emitted event
                .map(body -> new JsonObject().put("all", "placeholder"));
        Observable<JsonObject> oPool = eb.consumer(EB_POOL_SIZE).bodyStream().toObservable()
                .map(body -> new JsonObject().put("poolSize", body));
        Observable<JsonObject> oLast = eb.consumer(EB_LAST_SCAN).bodyStream().toObservable()
                .map(body -> new JsonObject().put("url", body));

        // When any of these observables get emitted items, push stats, with 1 second time buffer
        oFeed.mergeWith(oPool).mergeWith(oLast)
                .buffer(1, TimeUnit.SECONDS)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().reduce(
                        new JsonObject()
                                .put("host", RunningHost.getHostId())
                                .put("unique", raw.elementSet().size()),
                        JsonObject::mergeIn))
                .subscribe(obj -> {
                    if (obj.containsKey("all")) {
                        List<JsonObject> sorted = raw.elementSet().stream()
                                .map(e -> new JsonObject().put("name", e).put("count", raw.count(e)))
                                .sorted((e1, e2) -> e2.getInteger("count") - e1.getInteger("count"))
                                .collect(Collectors.toList());
                        obj.put("all", new JsonArray(sorted));
                    }
                    LOG.info("Got: " + obj);
                    eb.send(EB_PUSH_STATS, obj);
                }, LOG::error);
    }

    public static void main(String[] args) {
        Cluster.deploy(new StatsVerticle(), LOG::error);
    }
}
