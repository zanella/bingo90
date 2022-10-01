package zane.raf.bingo_90;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static zane.raf.bingo_90.Ticket.COLUMN_INDEXES;

public interface Common {
    int PERFORMANCE_TEST_NUMBER_OF_LOOPS = 10_000;

    default void verifyTicketIsValid(final Ticket ticket) {
        verifyTicketRows(ticket);

        verifyTicketIsBalanced(ticket);

        verifyColumnsOrdering(ticket);
    }

    default void verifyTicketIsBalanced(final Ticket ticket) {
        assertTrue(Collections.max(ticket.getFreqOfBlanksPerColumn().keySet()) < 3, ticket.toString());
    }

    default void verifyColumnsOrdering(final Ticket ticket) {
        COLUMN_INDEXES
            .forEach(columnIndex -> {
                final var l = Stream
                    .of(ticket.row1().get(columnIndex), ticket.row2().get(columnIndex), ticket.row3().get(columnIndex))
                    .filter(Objects::nonNull)
                    .toList();

                if (l.size() > 1) {
                    for (int i = 0; i < l.size() - 1; i++) {
                        if (l.get(i) >= l.get(i + 1)) {
                            throw new RuntimeException("Invalid column [" + columnIndex + "]: " + ticket);
                        }
                    }
                }
            });
    }

    default void verifyTicketRows(final Ticket ticket) {
        verifyRow(ticket.row1());
        verifyRow(ticket.row2());
        verifyRow(ticket.row3());
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
