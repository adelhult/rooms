package com.cmdjojo.rooms.structs;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Room {
    public String name;
    public ArrayList<Booking> bookings;
    
    boolean isOccupiedAt(Date d) {
        return bookingAt(d) != null;
    }
    
    Booking bookingAt(Date d) {
        Objects.requireNonNull(d);
        return bookings.stream()
                .filter(booking -> booking.start.before(d) && booking.end.after(d))
                .findFirst()
                .orElse(null);
    }
    
    
    Duration getTimeUntilFree(Date d) {
        if(bookingAt(d) == null) return Duration.ZERO;
        
        
        Date now = new Date(System.currentTimeMillis());
        
        return null;
    }
    
    
    static class Booking {
        Date start, end;
        
        public Booking(Date start, Date end) {
            this.start = start;
            this.end = end;
        }
    }
}
