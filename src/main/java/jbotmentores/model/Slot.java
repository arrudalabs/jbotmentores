package jbotmentores.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Slot {

    private final LocalDateTime from;
    private final LocalDateTime to;

    public Slot(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm") ;

    public boolean isWithinRange(Slot dateTimeRange) {
        if (dateTimeRange == null) {
            return false;
        }
        return this.equals(dateTimeRange)
                ||
                ((this.from.isEqual(dateTimeRange.from) || this.from.isBefore(dateTimeRange.from)
                        &&
                        (this.to.isEqual(dateTimeRange.to) || this.to.isAfter(dateTimeRange.to))));
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
        return String.format("%s - %s",this.from.format(timeFormat),this.to.format(timeFormat));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return Objects.equals(from, slot.from) && Objects.equals(to, slot.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
