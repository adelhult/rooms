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
import java.util.*;
import java.util.stream.Collectors;


public class RestApi {
    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final int PORT = 8080;

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
        System.out.println("Staring REST Api listener...");
        Javalin app = Javalin.create(JavalinConfig::enableCorsForAllOrigins).start(PORT);
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
            boolean onlyBookable = Optional.ofNullable(ctx.queryParam("onlyBookable")).orElse("true").equals("true");
            long from = parseOr(ctx.queryParam("from"), System.currentTimeMillis());
            Instant fromDateTime = Instant.ofEpochMilli(from);
            Duration minTime = Duration.of(minTimeInt, ChronoUnit.MINUTES);

            String[] equipment = Objects.requireNonNullElse(ctx.queryParam("equipment"), "")
                    .split(",");

            List<RoomDataSent> sortedRooms =
                    Objects.requireNonNullElse(DataCacher.getRooms(), Collections.<String, Room>emptyMap())
                            .values()
                            .stream()
                            .filter(room -> {
                                var roomInfo = DataCacher.getRoomInfo(room.name);
                                try {
                                    if (onlyBookable && room.isGhostRoom())
                                        return false;

                                    // check minSeats
                                    //noinspection ConstantConditions
                                    if (roomInfo.roomSeats < minSeats) {
                                        return false;
                                    }

                                    for (var thing : equipment) {
                                        if (thing.isBlank()) continue;
                                        if (!roomInfo.equipment.contains(thing))
                                            return false;
                                    }

                                    if (onlyBookable && roomInfo.roomType.contains("kvarn"))
                                        return false;

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
//                chalmersMapsLink = roomInfo.chalmersMapsLink;
//                latitude = roomInfo.latitude;
//                longitude = roomInfo.longitude;
            }
            duration = timeslot.getDuration().toMillis();
        }
    }

}
