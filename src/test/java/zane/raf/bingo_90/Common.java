package zane.raf.bingo_90;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public interface Common {
    default void verifyTicket(final Ticket ticket) {
        verifyRow(ticket.row1());
        verifyRow(ticket.row2());
        verifyRow(ticket.row3());

        assertTrue(Collections.max(ticket.getFreqOfBlanksPerColumn().keySet()) < 3, ticket.toString());

        // TODO: verify order of items per column -> ASC
    }

    default void verifyRow(final List<Integer> row) {
        if (row.size() != 9) throw new RuntimeException("Invalid size (!= 9): " + row);

        final var c = row
            .stream()
            .filter(Objects::isNull)
            .count();

        if (c != 4) throw new RuntimeException("Invalid number of blanks: " + row);
    }
}
