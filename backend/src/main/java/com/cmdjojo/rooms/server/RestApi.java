package com.cmdjojo.rooms.server;

import com.cmdjojo.rooms.RoomInfo;
import com.cmdjojo.rooms.core.DataCacher;
import com.cmdjojo.rooms.structs.Room;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import io.javalin.Javalin;

import java.time.*;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


public class RestApi {
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final int port = 8080;

    public static void start() {
        Javalin app = Javalin.create().start(port);
        app.get("/info/{roomName}", ctx -> {
            String roomName = ctx.pathParam("roomName");
            var info = DataCacher.getRoomInfo(roomName);
            ctx.result(gson.toJson(info));
        });
    }

}
