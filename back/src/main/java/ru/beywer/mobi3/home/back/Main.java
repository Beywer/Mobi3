package ru.beywer.mobi3.home.back;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
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

public class Main {

    private static Map<String, Meet> meets;
    private static Map<String, User> users;
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");

    public static void main (String args[]) {
        System.out.println("Server main");

        users = new HashMap<String, User>();
        meets = new HashMap<String, Meet>();

        User me = new User("Daniil","Miroshnikov", "Jurevich", "Beywer", "admin");
        User me2 = new User("Daniil","Miroshnikov", "Jurevich", "Beywer2", "admin");
        me.setPassword("37927");
        me2.setPassword("37927");
        users.put(me.getLogin(), me);
        Meet defaultMeet = new Meet("meetOne", me2, new Date(), new Date(System.currentTimeMillis() + 1000));
        defaultMeet.setId("meetOne");
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
        router.delete("/api/meets/:id").handler(Main::deleteTask);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);

    }

    private static void deleteTask(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        meets.remove(id);
        routingContext.response().end(createSuccessMessage("Task " + id + " successfully deleted").toString());
    }

    private static void authRequest(RoutingContext routingContext){
        String authorization = routingContext.request().getHeader("Authorization");
        System.out.println("New request (" + routingContext.normalisedPath() + "). Perform authorization: " + authorization);
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
                    routingContext.data().put("Login", username);
                    routingContext.next();
                }else{
                    System.out.println("User " + username +" not found or password incorrect. HTTP 401   " + users);
                    routingContext.response().putHeader("WWW-Authenticate","Basic realm=\"AZAZA Loh!\"");
                    routingContext.fail(401);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void acceptMeetUpdates(RoutingContext routingContext) {

        JsonObject req = routingContext.getBodyAsJson();
        String id = routingContext.request().getParam("id");
        System.out.println("Performing update " + req + " for task " + id);

        String login = req.getString("login");
        User newParticipant = users.get(login);
        String type = req.getString("type");
        switch (type){
            case "add":
                meets.get(id).addParticipant(newParticipant);
                routingContext.response().end(createSuccessMessage("Participant " + login + " added").toString());
                System.out.println("Added");
                break;
            case "remove":
                meets.get(id).removeParticipant(newParticipant);
                routingContext.response().end(createSuccessMessage("Participant " + login + " removed").toString());
                System.out.println("Deleted");
                break;
        }

    }

    private static void getAllMeets(RoutingContext routingContext) {

        String fromStr = routingContext.request().getParam("from");
        String toStr = routingContext.request().getParam("to");
        String search = routingContext.request().getParam("search");

        String login = (String)routingContext.data().get("Login");
        System.out.println("Perform getAllMeets for " + login);

        List<Meet> answer = new ArrayList<>();


        if(search!=null && !search.equals("")){
            System.out.println("Perform SEARCH get");
            for(String id : meets.keySet()){
                Meet meet = meets.get(id);
                System.out.println("Perform meet " + meet.getName());
                if(meet.getDescription().contains(search)){
                    answer.add(meet);
                }
            }
        } else {
            Date from = null;
            Date to = null;
            if(fromStr!=null && toStr!=null && !fromStr.equals("") && !toStr.equals("")){
                System.out.println("Perform INTERVAL get");
                try {
                    from = sdf.parse(fromStr);
                    to = sdf.parse(toStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println("Perform get TODAY");
                from = new Date();
                to = from;
            }

            for(String id : meets.keySet()){
                Meet meet = meets.get(id);
                System.out.println("Perform meet " + meet.getName() + "  " + meet.getStart() + "  " + meet.getEnd());
                if(isAfter(meet.getStart(), from) && isBefore(meet.getEnd(),to)){
                    System.out.println("Taken meet " + id + "     " + meet.getName());
                    answer.add(meet);
                }
            }
        }

        System.out.println("All meets  " + meets.size() + "   get  " + answer.size());
        System.out.println(gson.toJson(answer)+"\n\n");
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
                .end(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create().toJson(meet));
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
                String login = meet.getOwner().getLogin();
                User owner = users.get(login);
                if(owner != null){
                    meet.setOwner(owner);
                    meet.getParticipants().clear();
                    meet.addParticipant(owner);
                    System.out.println("newMeetJSON (Obj) = " + meet);
                    meets.put(id, meet);
                }

                answer = createSuccessMessage("Meet successfuly created  " + meets.size());
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

    private static boolean isToday(Date date){
        Date today = new Date();
        int todayYear = today.getYear();
        int todayDay = today.getDay();
        int todayMonth = today.getMonth();

        int year = date.getYear();
        int day = date.getDay();
        int month = date.getMonth();

        if(todayYear == year && todayDay == day && todayMonth == month){
            return  true;
        } else {
            return false;
        }
    }
    private static boolean isBefore(Date date, Date before){
        int beforeYear = before.getYear();
        int beforeMonth = before.getMonth();
        int beforeDay = before.getDay();

        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        long longDate = year*1000000 + month*1000 + day;
        long longBefore = beforeYear*1000000 + beforeMonth*1000 + beforeDay;
        System.out.println("date" +  year + "/" + month + "/" + day + "   "+ longDate);
        System.out.println("after" +  beforeYear + "/" + beforeMonth + "/" + beforeDay + "   "+ longBefore);

        if(longDate<=longBefore){
            System.out.println(date + "  isBefore  " + before);
            return  true;
        }else{
            System.out.println(date + " not isBefore  " + before);
            return false;
        }
    }
    private static boolean isAfter(Date date, Date after){
        int afterYear = after.getYear();
        int afterMonth = after.getMonth();
        int afterDay = after.getDay();

        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        long longDate = year*1000000 + month*1000 + day;
        long longAfter = afterYear*1000000 + afterMonth*1000 + afterDay;
        System.out.println("date" +  year + "/" + month + "/" + day + "   "+ longDate);
        System.out.println("after" +  afterYear + "/" + afterMonth + "/" + afterDay + "   "+ longAfter);

        if(longDate >= longAfter){
            System.out.println(date + "  isAfter  " + after);
            return  true;
        }else{
            System.out.println(date + " not isAfter  " + after);
            return false;
        }
    }
}
