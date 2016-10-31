package com.jotak.stars.stats;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.jotak.stars.RunningHost;
import com.jotak.stars.crawler.CrawlerVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * @author Joel Takvorian
 */
public class StatsVerticle extends AbstractVerticle {

    public static final String EB_STATS_QUERY = "stars.stats";

    private final Multiset<String> raw = HashMultiset.create();
    private final AtomicInteger poolSize = new AtomicInteger(0);
    private final AtomicReference<String> lastURL = new AtomicReference<>("");
    private long runSince = System.currentTimeMillis();

    @Override public void start() throws Exception {
        runSince = System.currentTimeMillis();
        EventBus eb = vertx.eventBus();
        eb.consumer(CrawlerVerticle.EB_FEED, msg -> raw.add(msg.body().toString()));
        eb.consumer(CrawlerVerticle.EB_POOL_SIZE, msg -> poolSize.set((int) msg.body()));
        eb.consumer(CrawlerVerticle.EB_LAST_SCAN, msg -> lastURL.set(msg.body().toString()));
        eb.consumer(EB_STATS_QUERY, msg -> msg.reply(formatStats()));
    }

    private String formatStats() {
        final List<Map.Entry<String, Integer>> sortedList = raw.elementSet().stream()
                .map(e -> new HashMap.SimpleImmutableEntry<>(e, raw.count(e)))
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .collect(Collectors.toList());
        return "Run since: " + new Date(runSince).toString()
                + "\nHost id: " + RunningHost.getHostId()
                + "\nURLs pool size: " + poolSize.get()
                + "\nLast fetched URL: " + lastURL.get()
                + "\nNumber of unique matches: " + raw.elementSet().size()
                + "\nPodium First: " + getAt(0, sortedList)
                + "\nPodium Second: " + getAt(1, sortedList)
                + "\nPodium Third: " + getAt(2, sortedList)
                + "\n\n\nFull list:" + raw.toString();
    }

    private static String getAt(int idx, List<Map.Entry<String, Integer>> sortedList) {
        if (idx < sortedList.size()) {
            Map.Entry<String, Integer> entry = sortedList.get(idx);
            return entry.getKey() + " (" + entry.getValue() + " occurences)";
        } else {
            return "n/a";
        }
    }
}
