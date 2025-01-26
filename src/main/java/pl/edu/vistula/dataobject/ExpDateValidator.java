package pl.edu.vistula.dataobject;

public class ExpDateValidator {

    public static void validateTicketExpiration(String expirationText, String randomTicketType) throws Exception {
        String[] parts = expirationText.split(" ");
        int hours = 0, minutes = 0, seconds = 0;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("godzin")) {
                hours = Integer.parseInt(parts[i-1]);
            } else if (parts[i].equals("minut")) {
                minutes = Integer.parseInt(parts[i-1]);
            } else if (parts[i].equals("sekund")) {
                seconds = Integer.parseInt(parts[i-1]);
            }
        }

        long expectedDurationSeconds = TicketType.valueOf(randomTicketType.toUpperCase()).getExpirationTime() - 5;
        long expectedHours = expectedDurationSeconds / 3600;
        long expectedMinutes = (expectedDurationSeconds % 3600) / 60;
        long expectedSeconds = expectedDurationSeconds % 60;

        int tolerance = 5;

        long actualSeconds = hours * 3600 + minutes * 60 + seconds;
        long expectedSecondsTotal = expectedHours * 3600 + expectedMinutes * 60 + expectedSeconds;

        long difference = Math.abs(actualSeconds - expectedSecondsTotal);

        if (difference > tolerance) {
            throw new Exception("The time difference is too large: " + difference + " seconds");
        }
    }
}

