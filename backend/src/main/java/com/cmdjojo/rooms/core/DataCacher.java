package com.cmdjojo.rooms.core;

import com.cmdjojo.rooms.structs.Room;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DataCacher {
    private static Data newData;
    private static CacheStatus status = CacheStatus.NEVER_LOADED;
    
    
    public static boolean loadFromCachedFile(File f) {
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
    Room[] rooms;
}
