package zane.raf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static zane.raf.Bingo90Generator.balanceTicket;
import static zane.raf.Bingo90Generator.fillRow;
import static zane.raf.Bingo90Generator.generateStrip;
import static zane.raf.Bingo90Generator.getFreqOfBlanksPerColumn;
import static zane.raf.Bingo90Generator.getPool;

class Bingo90GeneratorTest {
    @Test
    void testFillRow() {
        verifyRow( fillRow(getPool()) );
    }

    @Test
    void testBalanceTicket() {
        final var pool = getPool();

        final Supplier<ArrayList<Integer>> f = () -> IntStream
            .range(0, 9)
            .mapToObj(idx -> ((idx == 2) || (idx == 4) || (idx == 7) || (idx == 8)) ? null : pool.get(idx).pop())
            .collect(toCollection(ArrayList::new));

        final var unbalancedTicket = new Bingo90Generator.Ticket(f.get(), f.get(), f.get());

        assertEquals(3, Collections.max(getFreqOfBlanksPerColumn(unbalancedTicket).keySet()));

        verifyTicket( balanceTicket(pool, new Random(), unbalancedTicket) );
    }

    @Test
    void testGenerateStrip() {
        IntStream
            .range(0, 100_000)
            .parallel()
            .forEach(i -> {
                final var pool = getPool();

                final var strip = generateStrip(pool, new Random());

                strip
                    .tickets()
                    .forEach(this::verifyTicket);

                // System.out.println(strip);

                assertEquals(0, pool.values().stream().mapToInt(LinkedList::size).sum());
            });
    }

    @Test
    void testGenerateStripExecutionTime() {
        final long start = System.currentTimeMillis();

        for (int i = 0; i < 10_000; i++) { generateStrip(); }

        final var elapsed = System.currentTimeMillis()- start;

        assertTrue(elapsed < 1000, "Took too long: " + elapsed + "ms");
    }

    ////////////////////////////////////////////////

    private void verifyTicket(final Bingo90Generator.Ticket ticket) {
        verifyRow(ticket.row1());
        verifyRow(ticket.row2());
        verifyRow(ticket.row3());

        assertTrue(Collections.max(getFreqOfBlanksPerColumn(ticket).keySet()) < 3, ticket.toString());

        // TODO: verify order of items per column -> ASC
    }

    private void verifyRow(final List<Integer> row) {
        if (row.size() != 9) throw new RuntimeException("Invalid size (!= 9): " + row);

        final var c = row
            .stream()
            .filter(Objects::isNull)
            .count();

        if (c != 4) throw new RuntimeException("Invalid number of blanks: " + row);
    }
}
