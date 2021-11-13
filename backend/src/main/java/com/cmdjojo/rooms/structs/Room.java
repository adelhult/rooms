package com.cmdjojo.rooms.structs;

import biweekly.component.VEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class Room {
    public @NotNull String name;
    public @NotNull ArrayList<TimeSlot> bookings;
    private final int prioOrdinal;
    private transient Priority priority; // do not serialize
    
    public Room(@NotNull String name) {
        this.name = name;
        bookings = new ArrayList<>();
        priority = Priority.search(name);
        prioOrdinal = priority.ordinal();
    }
    
    public boolean isOccupiedAt(@NotNull Instant d) {
        return bookingAt(d) != null;
    }
    
    public @Nullable TimeSlot bookingAt(@NotNull Instant d) {
        Objects.requireNonNull(d);
        return bookings.stream()
                .filter(booking -> booking.start.isBefore(d) && booking.end.isAfter(d))
                .findFirst()
                .orElse(null);
    }
    
    public @NotNull TimeSlot getNextFreeSlot(Instant d) {
        TimeSlot current = bookingAt(d);
        Instant start = current == null ? d : current.end;
        Instant end = bookings.stream()
                .map(TimeSlot::getStart)
                .filter(bookingStart -> bookingStart.isAfter(start) || bookingStart.equals(start))
                .sorted()
                .findFirst()
                .orElse(d.plus(10, ChronoUnit.DAYS));
        return new TimeSlot(start, end);
    }
    
    public @NotNull Priority getPriority() {
        if (priority == null) priority = Priority.values()[prioOrdinal];
        return priority;
    }
    
    
    public @NotNull Duration getTimeUntilFree(Instant d) {
        TimeSlot current = bookingAt(d);
        if (current == null) return Duration.ZERO;
        else return Duration.between(d, current.end);
    }
    
    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", priority='" + priority.toString() + '\'' +
                ", bookings=" + bookings +
                '}';
    }

//    public static int getPriority(String name) {
//        if (name.startsWith("EG-2")) {
//            return 1;
//        }
//    }
    
    public enum Priority {
        NC_FLOOR_1("EG-251[56]"),
        NC_FLOOR_2("EG-350[3-8]"),
        EDIT_FLOOR_3("EG-3.+"),
        EDIT_FLOOR_4("EG-4.*"),
        EDIT_FLOOR_5("EG-5.*"),
        EDIT_FLOOR_6("EG-6.*"),
        UNKNOWN(".*");
        
        private final Pattern pattern;
        public final double timeMultiplier;
        
        Priority(String regex) {
            pattern = Pattern.compile(regex);
            timeMultiplier = Math.pow(1.1, ordinal());
        }
        
        static Priority search(String name) {
            for (Priority value : values()) {
                if (value.pattern.matcher(name).matches()) return value;
            }
            return UNKNOWN;
        }
    }
    
    public static class TimeSlot {
        
        public final @NotNull Instant start;
        public final @NotNull Instant end;
        
        public TimeSlot(VEvent event) {
            this(event.getDateStart().getValue().toInstant(), event.getDateEnd().getValue().toInstant());
        }
        
        public TimeSlot(@NotNull Instant start, @NotNull Instant end) {
            this.start = start;
            this.end = end;
        }
        
        public @NotNull Instant getStart() {
            return start;
        }
        
        public @NotNull Instant getEnd() {
            return end;
        }
        
        public @NotNull Duration getDuration() {
            return Duration.between(start, end);
        }
        
        public @NotNull Duration timeUntilStart(Instant now) {
            if (start.isBefore(now)) return Duration.ZERO;
            else return Duration.between(now, start);
        }
        
        @Override
        public String toString() {
            return "TimeSlot{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}
