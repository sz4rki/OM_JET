package pl.edu.vistula.dataobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TicketType {
    ULGOWY(3600),
    NORMALNY(3600),
    DZIENNY(86400),
    MIESIECZNY(2678400);

    private final long expirationTime;
}
