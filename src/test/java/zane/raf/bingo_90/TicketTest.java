package zane.raf.bingo_90;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.*;
import static zane.raf.bingo_90.Ticket.fillRow;

class TicketTest implements Common {

    @Test
    void testFillRow() {
        verifyRow( fillRow(Pool.create()) );
    }

    @Test
    void testGetFreqOfBlanksPerColumn() {
        final var pool = Pool.create();

        final Supplier<ArrayList<Integer>> f = () -> IntStream
            .range(0, 9)
            .mapToObj(pool::get)
            .map(LinkedList::pop)
            .collect(toCollection(ArrayList::new));

        final var unbalancedTicket = new Ticket(f.get(), f.get(), f.get());

        unbalancedTicket.row1().set(0, null);

        unbalancedTicket.row1().set(1, null);
        unbalancedTicket.row2().set(1, null);

        unbalancedTicket.row1().set(2, null);
        unbalancedTicket.row2().set(2, null);
        unbalancedTicket.row3().set(2, null);

        final var expectedFreqMap = Map.of(
            0, Set.of(3, 4, 5, 6, 7, 8),
            1, Set.of(0),
            2, Set.of(1),
            3, Set.of(2)
        );

        assertEquals(expectedFreqMap, unbalancedTicket.getFreqOfBlanksPerColumn(), unbalancedTicket.toString());
    }

    @Test
    void testBalanceTicket() {
        final var pool = Pool.create();

        final Supplier<ArrayList<Integer>> f = () -> IntStream
            .range(0, 9)
            .mapToObj(idx -> ((idx == 2) || (idx == 4) || (idx == 7) || (idx == 8)) ? null : pool.get(idx).pop())
            .collect(toCollection(ArrayList::new));

        final var unbalancedTicket = new Ticket(f.get(), f.get(), f.get());

        assertEquals(3, Collections.max(unbalancedTicket.getFreqOfBlanksPerColumn().keySet()));

        verifyTicket( unbalancedTicket.balanceTicket(pool, new Random()) );
    }
}