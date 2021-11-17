package com.cmdjojo.rooms.server;

import com.cmdjojo.rooms.core.DataCacher;

public class Main {
    public static void main(String... args) {
        RestApi.start();
        DataCacher.startCacheThread(10);
    }
}
