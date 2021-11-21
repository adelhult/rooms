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
import java.util.Arrays;

public class RoomInfo {
    static Gson gson = new GsonBuilder().create();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static RoomInfo getRoomInfo(String room) {
        try {
            var searchRes = getFromUrl("http://maps.chalmers.se/v2/live_search?lang=sv&charset=UTF-8&scope=location&scopes%5B%5D=chalmers&scopes%5B%5D=gothenburg&query=" + URLEncoder.encode(room, StandardCharsets.UTF_8));
            SearchResult searchResult = gson.fromJson(searchRes.body(), SearchResult.class);

            var docId = Arrays.stream(searchResult.suggestions)
                    .filter(s -> s.value.equals(room))
                    .findFirst().orElseThrow().docId;
            var geoJsonRes = getFromUrl("http://maps.chalmers.se/v2/geojson?docid=" + URLEncoder.encode(docId, StandardCharsets.UTF_8) + "&format=json&lang=sv");
            GeoJson geoJson = gson.fromJson(geoJsonRes.body(), GeoJson.class);

            String roomId = geoJson.features[0].properties.timeeditId;

            var roomRes = getFromUrl("http://maps.chalmers.se/v2/webservices/timeedit/room/" + URLEncoder.encode(roomId, StandardCharsets.UTF_8) + "/json");

            var roomInfo = gson.fromJson(roomRes.body(), RoomInfo.class);
            if (roomInfo.info != null) {
                roomInfo.info = roomInfo.info.replace(
                        "Behöver men hela rummet",
                        "Behöver man hela rummet"
                ).replace("\n", ". ");
                if (!roomInfo.info.endsWith(".")) roomInfo.info = roomInfo.info + ".";

                if (roomInfo.roomName.startsWith("EG-3213")) {
                    roomInfo.info = roomInfo.info.replace("EG-3211A", "EG3213A");
                }
            }
            roomInfo.chalmersMapsLink = "https://maps.chalmers.se/#" + docId;
            roomInfo.generalBuilding = geoJson.features[0].properties.buildingName;
            roomInfo.latitude = geoJson.features[0].properties.latitude;
            roomInfo.longitude = geoJson.features[0].properties.longitude;
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
    @SerializedName("room.equipment")
    public String roomEquipment;
    @SerializedName("room.webrespage")
    public String roomWebrespage;
    @SerializedName("room.culRoom")
    public String roomCulRoom;
    @SerializedName("general.objectComment")
    public String generalObjectComment;
    @SerializedName("alla.id_ref")
    public String allaIdRef;
    @SerializedName("room.seats-exam")
    public String roomSeatsExam;
    @SerializedName("room.price per seats")
    public String roomPricePerSeats;
    @SerializedName("room.computercount")
    public String roomComputerCount;
    @SerializedName("seats")
    public String seats;
    @SerializedName("equipment")
    public String equipment;
    @SerializedName("info")
    public String info;
    public String chalmersMapsLink;
    public double latitude;
    public double longitude;
}

class SearchResult {
    static class Suggestion {
        String value;
        @SerializedName("doc_id")
        String docId;
    }

    Suggestion[] suggestions;
}

class GeoJson {
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

    Feature[] features;
}
