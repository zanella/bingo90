package zane.raf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
    private static final int NUMBER_OF_REPETITIONS = 1_000_000;

    @Test
    void testFillRow() {
        for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) {
            final var pool = getPool();

            for (int j = 0; j < 18; j++) { verifyRow( fillRow(pool) ); }
        }
    }

    @Test
    void testBalanceTicket() {
        for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) {
            final var pool = getPool();

            final Supplier<ArrayList<Integer>> f = () -> IntStream
                .range(0, 9)
                .mapToObj(idx -> ((idx == 2) || (idx == 4) || (idx == 7) || (idx == 8)) ? null : pool.get(idx).pop())
                .collect(toCollection(ArrayList::new));

            final var unbalancedTicket = new Bingo90Generator.Ticket(f.get(), f.get(), f.get());

            final var ticket = balanceTicket(pool, unbalancedTicket);

            verifyRow(ticket.row1());
            verifyRow(ticket.row2());
            verifyRow(ticket.row3());

            assertTrue(Collections.max(getFreqOfBlanksPerColumn(ticket).keySet()) < 3);
        }
    }

    @Test
    void testGenerateStrip() {
        IntStream
            .range(0, NUMBER_OF_REPETITIONS)
            .parallel()
            .forEach(i -> {
                final var pool = getPool();

                final var strip = generateStrip(pool);

                Arrays.asList(strip.t1(), strip.t2(), strip.t3(), strip.t4(), strip.t5(), strip.t6()).forEach(ticket -> {
                    verifyRow(ticket.row1());
                    verifyRow(ticket.row2());
                    verifyRow(ticket.row3());
                });

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

    private void verifyRow(final List<Integer> row) {
        if (row.size() != 9) throw new RuntimeException("Invalid size (!= 9): " + row);

        final var c = row
            .stream()
            .filter(Objects::isNull)
            .count();

        if (c > 4) throw new RuntimeException("Invalid number of blanks: " + row);
    }
}
