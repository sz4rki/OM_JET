package pl.edu.vistula.dataobject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public record Ticket(TicketType type, long buyDate, long expirationDate, int quantity) {
    public String remainingTime() {
        ZonedDateTime expirationDateTime = Instant.ofEpochMilli(expirationDate).atZone(ZoneId.systemDefault());
        long secondsRemaining = ChronoUnit.SECONDS.between(ZonedDateTime.now(), expirationDateTime);

        long hours = secondsRemaining / 3600;
        long minutes = (secondsRemaining % 3600) / 60;
        long seconds = secondsRemaining % 60;

        if (hours > 0) {
            return String.format("%d godzin %d minut %d sekund", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minut %d sekund", minutes, seconds);
        } else {
            return String.format("%d sekund", seconds);
        }
    }
}
