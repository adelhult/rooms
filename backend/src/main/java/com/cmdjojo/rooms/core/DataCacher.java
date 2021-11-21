package com.cmdjojo.rooms.core;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.cmdjojo.rooms.RoomInfo;
import com.cmdjojo.rooms.structs.Room;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DataCacher {
    private static volatile Data newData;
    private static volatile Data oldData;
    private static volatile CacheStatus status = CacheStatus.NEVER_LOADED;

    private static volatile Map<String, RoomInfo> cachedRoomInfo;

    private static final Object LOCK = new Object();
    private static Thread cacheThread;
    private static final List<String> ICS_URLS;
    private static final List<HttpRequest> ICS_REQS;
    private static final HttpClient CLIENT;

    static {
        ICS_URLS = List.of(
                "https://cloud.timeedit.net/chalmers/web/public/ri6Y623XX55Z64Q186665066560753587461446Q617XXX2885142276X6566485XX47274X456X794262461XX5770855565276X62764544655X865776765X67WXX6X6858855874X86454X6456867264668X55664X1X984783455858XX664468156Y6665X5366XXX767836648566X5X666395446635614557XX66888765X36255W68730495X7846652864X55516565XY77X666X3347X6375X3406487X6X76376654X4XX6887X7X596644566751827650686999X5XX666904369696766088969666010609990XXXX9690006666XW60108XXX62120X3092525656081XX816196666865YX83720600808X1668646868826XX8X8932X66266X20000X6626255580881X26800X6X420226066262XX76002776788886X6807XX2286276808886X6X7X387Y05700W820883XX08225206X1846677Z67Q096X225205ZyQ8Xn86185.ics"
        );

        ICS_REQS = ICS_URLS.stream().map(url ->
                {
                    try {
                        return HttpRequest.newBuilder()
                                .GET()
                                .uri(new URL(url).toURI())
                                .timeout(Duration.of(12, ChronoUnit.SECONDS))
                                .build();
                    } catch (URISyntaxException | MalformedURLException e) {
                        System.err.println("Error fixing ICS URLs in initialization for DataCacher");
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toUnmodifiableList());

        CLIENT = HttpClient.newHttpClient();
    }

    /**
     * Starts the cache thread with the specified duration as cache interval
     *
     * @param duration The duration in seconds between each cache
     * @return True if the thread wasn't already started, false otherwise
     */
    public static boolean startCacheThread(final int duration) {
        if (cacheThread != null) return false;
        cacheThread = new Thread(() -> {
            while (true) {
                System.out.println("Caching...");
                cacheNewInstantly();
                try {
                    //noinspection BusyWait
                    Thread.sleep(duration * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        cacheThread.start();
        return true;
    }

    public static void cacheNewInstantly() {
        synchronized (LOCK) {
            oldData = newData;
            status = CacheStatus.NEW_CACHE_COMMENCING;
        }
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
            status = CacheStatus.NEW_CACHE_PRESENT;

            Map<String, Room> rooms = getRooms();
            if (rooms != null) {
                Set<String> missingRoomInfos = getRooms().keySet();
                if (cachedRoomInfo != null) missingRoomInfos.removeAll(cachedRoomInfo.keySet());
                else cachedRoomInfo = new HashMap<>(missingRoomInfos.size());
                ExecutorService s = Executors.newFixedThreadPool(8);
                missingRoomInfos.forEach(roomName -> s.submit(() ->
                        cachedRoomInfo.put(roomName, RoomInfo.getRoomInfo(roomName))));
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Something went wrong when waiting for ical responses");
            e.printStackTrace();
        }
    }

    /**
     * Accepts ical data as a string, and adds it to the cache map of the rooms
     *
     * @param s The ical data to save in cache
     */
    private static void acceptNewIcsData(String s) {
        ICalendar cal = Biweekly.parse(s).first();
        for (VEvent event : cal.getEvents()) {
            String[] roomNames = event.getLocation().getValue().split("[, ]+");
            if (roomNames.length == 0) {
                System.err.printf("Could not find room name for event with start at %s and end at %s, " +
                                "location was %s%n",
                        event.getDateStart().getValue(), event.getDateEnd().getValue(),
                        event.getLocation().getValue());
                continue;
            }

            Room.TimeSlot slot = new Room.TimeSlot(event);
            for (String roomName : roomNames) {
                newData.rooms.computeIfAbsent(roomName, Room::new);
                newData.rooms.get(roomName).bookings.add(slot);
            }
        }
    }

    /**
     * Gets a map between room names and room information, which contains information about bookings, and can derive
     * time slots where it's free
     *
     * @return An map of the rooms, if one exists in cache, otherwise null
     */
    @Nullable
    public static Map<String, Room> getRooms() {
        synchronized (LOCK) {
            if (status == CacheStatus.NEVER_LOADED) return null;
            else if (status == CacheStatus.NEW_CACHE_PRESENT) return newData.rooms;
            else return oldData.rooms;
        }
    }

    /**
     * Writes the Room map cache as JSON to the specified file. If no cache exists, no write will occur and the method
     * will return false.
     *
     * @param f The file to write to
     * @return Whether the write succeeded.
     */
    public static boolean writeToFile(@NotNull File f) {
        Objects.requireNonNull(f);
        try (FileWriter fw = new FileWriter(f)) {
            synchronized (LOCK) {
                if (status == CacheStatus.NEW_CACHE_PRESENT) {
                    new Gson().toJson(newData, fw);
                } else {
                    new Gson().toJson(oldData, fw);
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Could not save cached data!");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Loads the Room map cache as JSON from the specified file. If no file exists, the method will return false
     *
     * @param f The file which contains the cache to load
     * @return Whether the operation succeeds
     */
    public static boolean loadFromFile(File f) {
        synchronized (LOCK) {
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

    public static void cacheRoomInfoAsync() {
        new Thread(DataCacher::cacheRoomInfo).start();
    }

    public static void cacheRoomInfo() {
        assert getRooms() != null;
        cachedRoomInfo = new HashMap<>();
        for (String room : getRooms().keySet()) {
            var roomInfo = RoomInfo.getRoomInfo(room);
            if (roomInfo != null) {
                cachedRoomInfo.put(room, roomInfo);
            }
        }
    }

    @Nullable
    public static RoomInfo getRoomInfo(String room) {
        if (cachedRoomInfo == null) return null;
        return cachedRoomInfo.get(room);
    }
}

enum CacheStatus {
    NEVER_LOADED,
    NEW_CACHE_PRESENT,
    NEW_CACHE_COMMENCING
}

class Data {
    HashMap<String, Room> rooms;

    public Data() {
        this.rooms = new HashMap<>();
    }
}
