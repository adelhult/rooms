package com.cmdjojo.rooms.server;

import biweekly.ICalendar;

public class Main {
    public static void main(String... args) {
        System.out.println("abc");
        ICalendar cal = Downloader.cal();
        System.out.println(cal.getEvents().size());
        
        cal.getEvents().forEach(e -> System.out.printf("%s TO %s IN %s%n", e.getDateStart().getValue(),
                e.getDateEnd().getValue(),
                e.getLocation().getValue()));
    }
}
