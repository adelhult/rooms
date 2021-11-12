package com.cmdjojo.rooms.server;

import biweekly.Biweekly;
import biweekly.ICalendar;

import java.io.IOException;

public class Downloader {
    
    public static ICalendar cal() {
        try {
            return Biweekly.parse(Downloader.class.getClassLoader().getResourceAsStream("example.ics")).first();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
