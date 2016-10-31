package com.jotak.stars.endpoints;

import com.jotak.stars.stats.StatsVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * @author Joel Takvorian
 */
public class HttpRouterVerticle extends AbstractVerticle {

    @Override public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/stats").handler(ctx -> getStats(ctx.response()));
        router.route("/*").handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private String getStats(HttpServerResponse res) {
        vertx.eventBus().send(StatsVerticle.EB_STATS_QUERY, new JsonObject(), reply -> {
            if (reply.succeeded()) {
                res.end(reply.result().body().toString());
            } else {
                res.end("Error occured: " + reply.cause().toString());
            }
        });
        return "Stats:";
    }
}
