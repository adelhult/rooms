package com.cmdjojo.rooms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class RoomInfo {
    final static String TIMEEDIT_SEARCH_API = "https://cloud.timeedit.net/chalmers/web/public/objects.html?partajax=t&types=186&search_text=";
    final static Pattern TIMEEDIT_SEARCH_EXTRACTOR = Pattern.compile("data-idonly=\"(\\d+)\"");
    final static String TIMEEDIT_INFO_API = "https://cloud.timeedit.net/chalmers/web/public/objects/";
    // note: you need to add .html to end (or .json seems to work LOL)
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    static Gson gson = new GsonBuilder().create();
    @SerializedName("room.id")
    public String roomId;
    @SerializedName("room.name")
    public String roomName;
    @SerializedName("general.building")
    public String generalBuilding;
    @SerializedName("room.type")
    public String roomType;
    @SerializedName("room.seats")
    public int roomSeats;
    @SerializedName("equipment")
    public String equipment;
    @SerializedName("info")
    public String info;
    
    public RoomInfo(int roomId, RoomDataResult source) {
        this.roomId = String.valueOf(roomId);
        
        roomName = source.getFieldByFieldID(24);
        roomSeats = Integer.parseInt(source.getFieldByFieldID(25));
        roomType = source.getFieldByFieldID(27);
        equipment = source.getFieldByFieldID(28);
        generalBuilding = source.getFieldByFieldID(79);
        
        info = source.getFieldByFieldID(11);
    }
    
    public static RoomInfo getRoomInfo(String room) {
        try {
            var queryUrl = TIMEEDIT_SEARCH_API + URLEncoder.encode(room, StandardCharsets.UTF_8);
            var searchRes = getFromUrl(queryUrl);
            var matcher = TIMEEDIT_SEARCH_EXTRACTOR.matcher(searchRes.body());
            if (!matcher.find()) {
                System.err.println("Could not get ID for room " + room + " using regex...");
                return null;
            }
            var roomId = Integer.parseInt(matcher.group(1));
            var roomRes = getFromUrl(TIMEEDIT_INFO_API + roomId + ".json").body();
            var dumbFormat = gson.fromJson(roomRes, RoomDataResult.class);
            var roomInfo = new RoomInfo(roomId, dumbFormat);
            
            if (roomInfo.info != null) {
                roomInfo.info = roomInfo.info.replace(
                        "Behöver men hela rummet",
                        "Behöver man hela rummet"
                ).replace("\n", ". ");
                if (!roomInfo.info.endsWith(".")) roomInfo.info = roomInfo.info + ".";
                
                if (room.equals("EG-3213B")) {
                    roomInfo.info = roomInfo.info.replace("EG-3211A", "EG-3213A");
                }
            }
            
            return roomInfo;
        } catch (Exception e) {
            System.out.println("Failed to get room info for " + room + ".");
            return null;
        }
    }
    
    private static HttpResponse<String> getFromUrl(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        HttpRequest req = HttpRequest.newBuilder().GET().uri(uri).build();
        return CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

class RoomDataResult {
    @SerializedName("records")
    InternalFieldWrapper[] records;
    
    String getFieldByFieldID(int id) {
        for (StupidInternalField field : records[0].fields) {
            if (field.id == id) {
                return field.values.length > 0 ? field.values[0] : null;
            }
        }
        return null;
    }
    
    static class InternalFieldWrapper {
        StupidInternalField[] fields;
    }
    
    static class StupidInternalField {
        int numberOfValues;
        int[] valuesAsInteger;
        boolean[] valuesAsBoolean;
        String[] values;
        String extId;
        int id;
    }
}

class SearchResult {
    Suggestion[] suggestions;
    
    static class Suggestion {
        String value;
        @SerializedName("doc_id")
        String docId;
    }
}

class GeoJson {
    Feature[] features;
    
    static class Feature {
        Properties properties;
    }
    
    static class Properties {
        @SerializedName("timeedit_id")
        String timeeditId;
        @SerializedName("building_name")
        String buildingName;
        double latitude;
        double longitude;
    }
}
