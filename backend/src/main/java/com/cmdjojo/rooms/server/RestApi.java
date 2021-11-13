package com.cmdjojo.rooms.server;
//import com.cmdjojo.rooms.RoomInfo;
import com.google.gson.Gson;
import io.javalin.Javalin;


public class RestApi {
    private static Gson gson = new Gson();

    private static final int port = 8080;

    public static void start() {
        Javalin app = Javalin.create().start(port);
        app.get("/info/{roomName}", ctx -> {
            String roomName = ctx.pathParam("roomName");
            //var info = RoomInfo.getRoomInfo(roomName);
            //ctx.json(info);
            //ctx.result("Hello World");
        });
    }

}
