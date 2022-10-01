package zane.raf.bingo_90;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

        verifyTicketRows(unbalancedTicket);
        assertEquals(3, Collections.max(unbalancedTicket.getFreqOfBlanksPerColumn().keySet()));

        final var balancedTicket = unbalancedTicket.balanceTicket(pool, new Random());

        verifyTicketIsBalanced(balancedTicket);
    }

    @Test
    void testSortColumns() {
        final var pool = Pool.create();
        final var random = new Random();

        final var ticket =  new Ticket(fillRow(pool), fillRow(pool), fillRow(pool))
            .balanceTicket(pool, random);

        ticket.row1().set(0, 9);
        ticket.row2().set(0, null);
        ticket.row3().set(0, 7);

        verifyColumnsOrdering(ticket.sortColumns());

        assertEquals(7, ticket.row1().get(0));
        assertNull(ticket.row2().get(0));
        assertEquals(9, ticket.row3().get(0));
    }
}
