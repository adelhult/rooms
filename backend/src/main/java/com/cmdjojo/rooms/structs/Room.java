package com.cmdjojo.rooms.structs;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

public class Room {
    public String name;
    public ArrayList<TimeSlot> bookings;
    
    boolean isOccupiedAt(Instant d) {
        return bookingAt(d) != null;
    }
    
    TimeSlot bookingAt(Instant d) {
        Objects.requireNonNull(d);
        return bookings.stream()
                .filter(booking -> booking.start.isBefore(d) && booking.end.isAfter(d))
                .findFirst()
                .orElse(null);
    }
    
    TimeSlot getNextFreeSlot(Instant d) {
        TimeSlot current = bookingAt(d);
        Instant start = current == null ? d : current.end;
        Instant end = bookings.stream()
                .map(TimeSlot::getStart)
                .filter(bookingStart -> bookingStart.isAfter(start) || bookingStart.equals(start))
                .sorted()
                .findFirst()
                .orElse(null);
        return new TimeSlot(start, end);
    }
    
    
    Duration getTimeUntilFree(Instant d) {
        TimeSlot current = bookingAt(d);
        if (current == null) return Duration.ZERO;
        else return Duration.between(d, current.end);
    }
    
    
    static class TimeSlot {
        public Instant start, end;
        
        public TimeSlot(Instant start, Instant end) {
            this.start = start;
            this.end = end;
        }
        
        public Instant getStart() {
            return start;
        }
        
        public Instant getEnd() {
            return end;
        }
    }
}
