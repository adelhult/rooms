package com.cmdjojo.rooms.server;

import com.cmdjojo.rooms.core.DataCacher;
import com.cmdjojo.rooms.structs.Room;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;

public class Main {
    public static void main(String... args) {
        DataCacher.cacheNewInstantly();
        var map = DataCacher.getRooms();
        assert map != null;
        var sim = LocalDateTime.of(2021, 11, 17, 12, 20, 0, 0).toInstant(ZoneOffset.ofHours(0));
        System.out.printf("%d rooms found: %s%n", map.size(), String.join(", ", map.keySet()));
        map.values().forEach(room -> room.bookings.sort(Comparator.comparing(Room.TimeSlot::getStart)));
        for (Room value : map.values()) {
            System.out.printf("Room %s has %d bookings:%n", value.name, value.bookings.size());
            value.bookings.forEach(System.out::println);
            System.out.println("It is free in " + value.getTimeUntilFree(sim));
        }
        System.out.println("Time is " + sim.toString());


//        System.out.println("abc");
//        ICalendar cal = Downloader.cal();
//        System.out.println(cal.getEvents().size());
//
//        cal.getEvents().forEach(e -> System.out.printf("%s TO %s IN %s%n", e.getDateStart().getValue(),
//                e.getDateEnd().getValue(),
//                e.getLocation().getValue()));
    }
}
