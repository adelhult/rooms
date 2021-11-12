package com.cmdjojo.rooms.core;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.cmdjojo.rooms.structs.Room;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DataCacher {
    private static volatile Data newData;
    private static volatile Data oldData;
    private static volatile CacheStatus status = CacheStatus.NEVER_LOADED;
    
    private static final List<String> ICS_URLS;
    
    private static final List<HttpRequest> ICS_REQS;
    private static final HttpClient CLIENT;
    
    static {
        ICS_URLS = List.of(
                "https://cloud.timeedit.net/chalmers/web/public/ri6Y623ZX55Z6QQ1866650565Q0753y8Z441446Q617X6Xn855.ics"
        );
        
        ICS_REQS = ICS_URLS.stream().map(url ->
                {
                    try {
                        return HttpRequest.newBuilder()
                                .GET()
                                .uri(new URL(url).toURI())
                                .timeout(Duration.of(5, ChronoUnit.SECONDS))
                                .build();
                    } catch (URISyntaxException | MalformedURLException e) {
                        System.err.println("Error fixing ICS URLs in initialization for DataCacher");
                        e.printStackTrace();
                    }
                    return null;
                }
        ).collect(Collectors.toUnmodifiableList());
        
        CLIENT = HttpClient.newHttpClient();
    }
    
    
    public static void cacheNewInstantly() {
        oldData = newData;
        status = CacheStatus.NEW_CACHE_COMMENCING;
        newData = new Data();
        System.out.printf("Caching new data from %d ics urls...%n", ICS_REQS.size());
        
        CompletableFuture<Void> allIcsResponses = CompletableFuture.allOf(
                ICS_REQS.stream().map(req -> CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> {
                            if (res.statusCode() != 200) {
                                System.err.printf("Could not get data from url %s%n", res.uri().toString());
                            } else {
                                DataCacher.acceptNewIcsData(res.body());
                            }
                        })).toArray(CompletableFuture[]::new)
        );
        
        try {
            allIcsResponses.get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong when waiting for ical responses");
            e.printStackTrace();
        }
    }
    
    private static void acceptNewIcsData(String s) {
        ICalendar cal = Biweekly.parse(s).first();
        for (VEvent event : cal.getEvents()) {
            //newData.rooms.
        }
    }
    
    public static Map<String, Room> getRooms() {
        if (status == CacheStatus.NEVER_LOADED) return null;
        else if (status == CacheStatus.NEW_CACHE_PRESENT) return newData.rooms;
        else return oldData.rooms;
    }
    
    public static boolean loadFromFile(File f) {
        try {
            newData = new Gson().fromJson(new FileReader(f), Data.class);
            status = CacheStatus.NEW_CACHE_PRESENT;
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Could not load cached data!");
            e.printStackTrace();
            return false;
        }
    }
}

enum CacheStatus {
    NEVER_LOADED,
    NEW_CACHE_PRESENT,
    NEW_CACHE_COMMENCING
}

class Data {
    Map<String, Room> rooms;
    
    public Data() {
        this.rooms = new HashMap<>();
    }
}
