package com.cmdjojo.rooms.server;

import com.cmdjojo.rooms.RoomInfo;
import com.cmdjojo.rooms.core.DataCacher;
import com.cmdjojo.rooms.structs.Room;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class RestApi {
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();
    
    private static final int port = 8080;
    
    private static int parseOr(String s, int defaultValue) {
        if (s == null) return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private static long parseOr(String s, long defaultValue) {
        if (s == null) return defaultValue;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static void start() {
        Javalin app = Javalin.create(JavalinConfig::enableCorsForAllOrigins).start(port);
        app.get("/info/{roomName}", ctx -> {
            String roomName = ctx.pathParam("roomName");
            var info = DataCacher.getRoomInfo(roomName);
            ctx.result(gson.toJson(info));
        });
        
        
        app.get("/suggestions", ctx -> {
            // get params or else set default values
            int number = parseOr(ctx.queryParam("number"), 1);
            int minSeats = parseOr(ctx.queryParam("minSeats"), 0);
            int minTimeInt = parseOr(ctx.queryParam("minTime"), 15);
            long from = parseOr(ctx.queryParam("from"), System.currentTimeMillis());
            Instant fromDateTime = Instant.ofEpochMilli(from);
            Duration minTime = Duration.of(minTimeInt, ChronoUnit.MINUTES);
            
            String[] equipment = Objects.requireNonNullElse(ctx.queryParam("equipment"), "")
                    .split(",");
            
            List<RoomDataSent> sortedRooms = DataCacher.getRooms()
                    .values()
                    .stream()
                    .filter(room -> {
                        var roomInfo = DataCacher.getRoomInfo(room.name);
                        try {
                            // check minSeats
                            if (roomInfo.roomSeats < minSeats) {
                                return false;
                            }
                            
                            for (var thing : equipment) {
                                if (!roomInfo.equipment.contains(thing))
                                    return false;
                            }
                            
                        } catch (Exception e) {
                            return false;
                        }
                        return true;
                    })
                    .map(room -> new RoomDataSent(room, fromDateTime, minTime, DataCacher.getRoomInfo(room.name)))
                    .sorted(Comparator.comparingInt(rds -> -rds.goodnessScore))
                    .limit(number)
                    .collect(Collectors.toList());
            
            ctx.contentType("application/json");
            ctx.result(gson.toJson(sortedRooms));
        });
    }
    
    static class RoomDataSent {
        String name;
        Room.TimeSlot timeslot;
        String seatcount;
        String building;
        String comment;
        String equipment;
        String chalmersMapsLink;
        double latitude;
        double longitude;
        long duration;
        int goodnessScore;
        
        RoomDataSent(Room room, Instant fromDateTime, Duration minTime, RoomInfo roomInfo) {
            name = room.name;
            timeslot = room.getNextFreeSlot(fromDateTime, minTime);
            goodnessScore = room.getGoodnessScore(fromDateTime, timeslot);
            if (roomInfo != null) {
                seatcount = Integer.toString(roomInfo.roomSeats);
                building = roomInfo.generalBuilding;
                comment = roomInfo.info;
                equipment = roomInfo.equipment;
                chalmersMapsLink = roomInfo.chalmersMapsLink;
                latitude = roomInfo.latitude;
                longitude = roomInfo.longitude;
            }
            duration = timeslot.getDuration().toMillis();
        }
    }
    
}
