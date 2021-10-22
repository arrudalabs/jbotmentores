package jbotmentores.model;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Embeddable
public class Slot implements Comparable<Slot> {

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    /**
     * Don't use it! It's required by JPA
     */
    @Deprecated
    public Slot() {
    }

    public Slot(LocalDateTime from, LocalDateTime to) {
        this.startAt = from;
        this.endAt = to;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setStartAt(LocalDateTime from) {
        this.startAt = from;
    }

    public void setEndAt(LocalDateTime to) {
        this.endAt = to;
    }

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm") ;

    public boolean isWithinRange(Slot dateTimeRange) {
        if (dateTimeRange == null) {
            return false;
        }
        return this.equals(dateTimeRange)
                ||
                ((this.startAt.isEqual(dateTimeRange.startAt) || this.startAt.isBefore(dateTimeRange.startAt)
                        &&
                        (this.endAt.isEqual(dateTimeRange.endAt) || this.endAt.isAfter(dateTimeRange.endAt))));
    }

    public boolean isWithinRange(LocalDateTime localDateTime) {
        return this.isWithinRange(new Slot(localDateTime, localDateTime));
    }


    public static LocalDateTime parseToLocalDateTime(
            LocalDate localDate,
            String hour) {

        LocalDateTime localDateTime = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0);

        if (hour == null || hour.trim().isEmpty() || "h".equals(hour)) {
            return localDateTime;
        }

        if (!hour.matches("^(|[0-9]|[12][0-3]|1[4-9]|[345]{1}[0-9]{1})h(|0|0[0-5]|[1-5][0-9])$")) {
            throw new IllegalArgumentException(String.format("cannot parse the time %s", hour));
        }

        List<Integer> hourPieces =
                Arrays.asList(Optional.ofNullable(hour).orElse("").trim().toLowerCase().split("h"))
                        .stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(x -> !x.isBlank())
                        .map(Integer::valueOf)
                        .collect(Collectors.toList());

        if (hourPieces.isEmpty()) {
            return localDateTime;
        }

        localDateTime = localDateTime.withHour(hourPieces.remove(0));
        if (!hourPieces.isEmpty()) {
            localDateTime = localDateTime.withMinute(hourPieces.get(0));
        }
        return localDateTime;
    }

    public String printTimeRange() {
        return String.format("%s - %s",this.startAt.format(timeFormat),this.endAt.format(timeFormat));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return Objects.equals(startAt, slot.startAt) && Objects.equals(endAt, slot.endAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAt, endAt);
    }

    @Override
    public int compareTo(@NotNull Slot o) {
        return this.getStartAt().compareTo(o.startAt);
    }
}
