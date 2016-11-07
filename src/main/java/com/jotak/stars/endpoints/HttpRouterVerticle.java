package com.jotak.stars.endpoints;

import com.jotak.stars.stats.StatsVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * @author Joel Takvorian
 */
public class HttpRouterVerticle extends AbstractVerticle {

    @Override public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route("/eventbus/*").handler(SockJSHandler.create(vertx)
            .bridge(new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress(StatsVerticle.EB_PUSH_STATS))));
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
