package ru.beywer.mobi3.home.back;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
    private static Map<String, User> users;
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    public static void main (String args[]) {
        System.out.println("Server main");

        users = new HashMap<String, User>();
        meets = new HashMap<String, Meet>();

        User me = new User("Daniil","Miroshnikov", "Jurevich", "Beywer", "admin");
        me.setPassword("37927");
        users.put(me.getLogin(), me);
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
        router.route("/api/*").handler(Main::authRequest);

        router.get("/api/meets/all").handler(Main::getAllMeets);
        router.get("/api/meets/:id").handler(Main::getMeetById);
        router.put("/api/meets/:id").handler(Main::addMeetById);
        router.post("/api/meets/:id").handler(Main::acceptMeetUpdates);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);

    }

    private static void authRequest(RoutingContext routingContext){
        String authorization = routingContext.request().getHeader("Authorization");
        System.out.println("New request. Perform authorization: " + authorization);
        if(authorization == null){
            System.out.println("HTTP 401");
            routingContext.response().putHeader("WWW-Authenticate","Basic realm=\"Loh\"");
            routingContext.fail(401);
        }else{
            final String encodedUserPassword = authorization.replaceFirst("Basic ", "");
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(
                        encodedUserPassword);
                String usernameAndPassword = new String(decodedBytes, "UTF-8");
                final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                final String username = tokenizer.nextToken();
                final String password = tokenizer.nextToken();
                System.out.println("Perform authorization. Get login and password: " + username + "   " + password);

                User user = users.get(username);
                if(user!=null && user.getPassword()!=null && user.getPassword().equals(password)){
                    System.out.println("User founded");
                    routingContext.next();
                }else{
                    System.out.println("User " + username +" not found or password incorrect. HTTP 401   " + users);
                    routingContext.response().putHeader("WWW-Authenticate","Basic realm=\"Loh\"");
                    routingContext.fail(401);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void acceptMeetUpdates(RoutingContext routingContext) {
    }

    private static void getAllMeets(RoutingContext routingContext){
        System.out.println("Perform getAllMeets");

        List<Meet> answer = new ArrayList<>();

        //TODO показывать только сегодняшние встречи или по интервалу
        for(String id : meets.keySet()){
            Meet meet = meets.get(id);
            System.out.println("Takeed meet " + id + "     " + meet.getName());
            answer.add(meet);
        }

        System.out.println(gson.toJson(answer));
        routingContext.response()
                .putHeader("content-type", "application/json; charset=UTF-8")
                .end(new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create().toJson(answer));
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
            if(newMeetJSON != null && !newMeetJSON.equals("")){
                System.out.println("newMeetJSON (Str) = " + newMeetJSON);
                Meet meet = gson.fromJson(newMeetJSON, Meet.class);
                System.out.println("newMeetJSON (Obj) = " + meet);
                meets.put(id, meet);

                answer = createSuccessMessage("Meet successfuly created");
            }else{
                answer = createSuccessMessage("Empty body");
            }
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
