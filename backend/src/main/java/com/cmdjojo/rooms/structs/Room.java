package com.cmdjojo.rooms.structs;

import biweekly.component.VEvent;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class Room {
    public static final int TOUGHNESS_TO_WAIT = 10;
    public static final int TOUGHNESS_TO_WALK = 5;
    public static final Duration MAX_GOODNESS_DURATION = Duration.of(4, ChronoUnit.HOURS);

    public @NotNull String name;
    public @NotNull ArrayList<TimeSlot> bookings;
    private transient Priority priority; // do not serialize

    public Room(@NotNull String name) {
        this.name = name;
        bookings = new ArrayList<>();
        priority = Priority.search(name);
    }

    public boolean isOccupiedAt(@NotNull Instant d) {
        return bookingAt(d) != null;
    }

    public @Nullable TimeSlot bookingAt(@NotNull Instant d) {
        Objects.requireNonNull(d);
        return bookings.stream()
                .filter(booking -> (booking.getStart().isBefore(d) || booking.getStart().equals(d)) && booking.getEnd().isAfter(d))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a booking within a specific duration from the specified instant, exclusively. If the time now is 10:00 and
     * the within is set to 15m, bookings starting earlier than 10:15 (and ending after 10:00) is found
     *
     * @param d      The instant to find a booking from
     * @param within The first disallowed duration between the instant and the start time of the booking
     * @return A booking which ends after the provided instant and starts before d+within
     */
    public @Nullable TimeSlot findBookingWithin(@NotNull Instant d, @NotNull Duration within) {
        Objects.requireNonNull(d);
        Instant mustStartBefore = d.plus(within);
        return bookings.stream()
                .filter(booking -> booking.getStart().compareTo(mustStartBefore) < 0 && booking.getEnd().isAfter(d))
                .findAny()
                .orElse(null);
    }

    /**
     * Gets the next time slot this room is free, which starts at earliest at the provided instant and is at least
     * the provided duration (inclusively) long. The time slot stretches to the start of the next booking, or 20 days,
     * whichever occurs first.
     *
     * @param d           The instant to find the slot from
     * @param minDuration The minimum duration the slot may have
     * @return The next free slot which is at least minDuration long
     */
    public @NotNull TimeSlot getNextFreeSlot(@NotNull Instant d, Duration minDuration) {
        Instant startt = d;
        TimeSlot atStart;
        //as long as there is a booking within minDuration from
        //noinspection ConstantConditions
        while ((atStart = findBookingWithin(startt, minDuration)) != null) startt = atStart.endInstant;

        final Instant start = startt;
        Instant end = bookings.stream()
                .map(TimeSlot::getStart)
                .filter(bookingStart -> bookingStart.isAfter(start))
                .sorted()
                .findFirst()
                .orElse(d.plus(20, ChronoUnit.DAYS));
        return new TimeSlot(start, end);
    }

    public int getGoodnessScore(Instant d, TimeSlot assignedSlot) {
        Duration freeDuration = assignedSlot.getDuration().compareTo(MAX_GOODNESS_DURATION) < 0 ?
                assignedSlot.getDuration() : MAX_GOODNESS_DURATION;
        Duration timeUntilFree = Duration.between(d, assignedSlot.getStart()).multipliedBy(TOUGHNESS_TO_WAIT);
        return (int) (freeDuration.toMinutes()
                - timeUntilFree.toMinutes()
                - (long) getPriority().timeToWalk * TOUGHNESS_TO_WALK);
    }

    /**
     * Gets the calculated priority of this room. Do NOT read the priority from the field itself since it isn't
     * serialized and might not always be availible.
     *
     * @return The priority of this room
     */
    public @NotNull Priority getPriority() {
        if (priority == null) priority = Priority.search(name);
        return priority;
    }

    public @NotNull Duration getTimeUntilFree(Instant d, Duration minDuration) {
        return Duration.between(d, getNextFreeSlot(d, minDuration).startInstant);
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", priority='" + priority.toString() + '\'' +
                ", bookings=" + bookings +
                '}';
    }

    public enum Priority {
        NC_FLOOR_1("EG-251\\d", 1),
        NC_FLOOR_2("EG-350\\d", 2),
        EDIT_FLOOR_3("EG-3.+", 2),
        EDIT_FLOOR_4("EG-4.+", 3),
        EDIT_FLOOR_5("EG-5.+", 4),
        EDIT_FLOOR_6("EG-6.+", 5),
        MASKIN("M.+", 8),
        FYSIK("F.+", 15),
        UNKNOWN("", 20);

        private final Pattern pattern;
        public final int timeToWalk;

        Priority(String regex, int timeToWalk) {
            pattern = Pattern.compile(regex);
            this.timeToWalk = timeToWalk;
        }

        static Priority search(String name) {
            for (Priority value : values()) {
                if (value.pattern.matcher(name).matches()) return value;
            }
            System.out.printf("Room %s has no priority set%n", name);
            return UNKNOWN;
        }
    }

    public static class TimeSlot {

        @SerializedName("start")
        private long startMillis;
        @SerializedName("end")
        private long endMillis;


        private transient @Nullable Instant startInstant;
        private transient @Nullable Instant endInstant;

        public TimeSlot(VEvent event) {
            this(event.getDateStart().getValue().toInstant(), event.getDateEnd().getValue().toInstant());
        }

        public TimeSlot(@NotNull Instant start, @NotNull Instant end) {
            startMillis = start.toEpochMilli();
            endMillis = end.toEpochMilli();
            this.startInstant = start;
            this.endInstant = end;
        }

        public @NotNull Instant getStart() {
            if (startInstant == null) startInstant = Instant.ofEpochMilli(startMillis);
            return startInstant;
        }

        public @NotNull Instant getEnd() {
            if (endInstant == null) endInstant = Instant.ofEpochMilli(startMillis);
            return endInstant;
        }

        public @NotNull Duration getDuration() {
            return Duration.between(getStart(), getEnd());
        }

        public @NotNull Duration timeUntilStart(Instant now) {
            if (getStart().isBefore(now)) return Duration.ZERO;
            else return Duration.between(now, startInstant);
        }

        @Override
        public String toString() {
            return "TimeSlot{" +
                    "start=" + startInstant +
                    ", end=" + endInstant +
                    '}';
        }
    }
}
