package ru.beywer.mobi3.home.back;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import ru.beywer.home.mobi3.lib.Meet;
import ru.beywer.home.mobi3.lib.User;

/**
 * Created by Beywer on 11.11.2015.
 */
public class Main {

    private static Map<String, Meet> meets;
    private static Gson gson = new Gson();

    public static void main (String args[]) {
        System.out.println("Server main");

        meets = new HashMap<String, Meet>();

        User me = new User("Daniil","Miroshnikov", "Jurevich", "admin", "admin");
        Meet defaultMeet = new Meet("meetOne", me, new Date(), new Date(System.currentTimeMillis() + 1000));
        defaultMeet.setDescription("AZAZA ME!!!");
        meets.put("meetOne", defaultMeet);

        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setWebRoot("files");

        router.route("/meetApp/*").handler(staticHandler);

        // REST API
        router.route("/api/*").handler(BodyHandler.create());

        router.get("/api/meets/all").handler(Main::getAllMeets);
        router.get("/api/meets/:id").handler(Main::getMeetById);
        router.put("/api/meets/:id").handler(Main::addMeetById);
        router.post("/api/meets/:id").handler(Main::acceptMeetUpdates);

        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                router.accept(event);
            }
        }).listen(8080);

    }

    private static void acceptMeetUpdates(RoutingContext routingContext) {
    }

    private static void getAllMeets(RoutingContext routingContext){
        List<Meet> answer = new ArrayList<>();

        //TODO показывать только сегодняшние встречи или по интервалу
        for(String id : meets.keySet()){
            Meet meet = meets.get(id);
            answer.add(meet);
        }

        System.out.println("Get all (List) : " + new JsonArray(answer));
        routingContext.response()
                .putHeader("content-type", "application/json; charset=UTF-8")
                .end(new JsonArray(answer).toString());
    }

    private static void getMeetById(RoutingContext routingContext){

        String id = routingContext.request().getParam("id");
        System.out.println("Getting task by id = " + id);
        JsonObject answer;

        Meet meet = meets.get(id);
        if(meet == null){
            answer = createErrorMessage(String.format("Meet with this id (%s) does not exist", id));
        } else {
            String meetJson = Json.encode(meet);
            answer = new JsonObject(meetJson);
        }
        routingContext.response()
                .putHeader("content-type", "application/json; charset=UTF-8")
                .end(answer.toString());
    }

    private static void addMeetById(RoutingContext routingContext){

        String id = routingContext.request().getParam("id");
        System.out.println("Adding task with id = " + id);
        JsonObject answer;

        if(meets.get(id) != null){
            answer = createErrorMessage(String.format("Meet with this id (%s) already exist", id));
        } else {

            String newMeetJSON = routingContext.getBodyAsString();
            System.out.println("newMeetJSON (Str) = " + newMeetJSON);
            Meet meet = gson.fromJson(newMeetJSON, Meet.class);
            System.out.println("newMeetJSON (Obj) = " + meet);
            meets.put(id, meet);

            answer = createSuccessMessage("Meet successfuly created");
        }

        routingContext.response().end(answer.toString());
    }

    private static JsonObject createErrorMessage(String errMes){
        System.out.println(errMes);
        JsonObject mes = new JsonObject();
        mes.put("status", "err");
        mes.put("message", errMes);
        return mes;
    }
    private static JsonObject createSuccessMessage(String sucMes){
        System.out.println(sucMes);
        JsonObject mes = new JsonObject();
        mes.put("status", "err");
        mes.put("message", sucMes);
        return mes;
    }
}
