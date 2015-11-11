package ru.beywer.mobi3.back;

import java.io.IOException;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.impl.StaticHandlerImpl;
import io.vertx.ext.web.impl.RouterImpl;

/**
 * Created by Beywer on 11.11.2015.
 */
public class Main {

    public static void main (String args[]) {
        Vertx vertx = Vertx.factory.vertx();

        final Router router = new RouterImpl(vertx);

        StaticHandler staticHandler = new StaticHandlerImpl();
        staticHandler.setWebRoot("files");

        router.route("/meetApp/*").handler(staticHandler);

        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                router.accept(event);
            }
        }).listen(8080);

    }
}
