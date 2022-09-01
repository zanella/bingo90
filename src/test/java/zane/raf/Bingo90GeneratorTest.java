package zane.raf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static zane.raf.Bingo90Generator.balanceTicket;
import static zane.raf.Bingo90Generator.fillRow;
import static zane.raf.Bingo90Generator.generateStrip;
import static zane.raf.Bingo90Generator.getFreqOfBlanksPerColumn;
import static zane.raf.Bingo90Generator.getPool;

class Bingo90GeneratorTest {
    private static final int NUMBER_OF_REPETITIONS = 100_000;

    @Test
    void testFillRow() throws Exception {
        for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) { verifyRow( fillRow(getPool()) ); }
    }

    @Test
    void testBalanceTicket() throws Exception {
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
        long start = System.currentTimeMillis();

        //for (int i = 0; i < 10000; i++) {

        generateStrip();

        System.out.println("elapsed: " + (System.currentTimeMillis() - start) + "ms");
    }

    ////////////////////////////////////////////////

    private void verifyRow(final List<Integer> row) throws Exception {
        if (row.size() != 9) throw new Exception("Invalid size (!= 9): " + row);

        final var c = row
            .stream()
            .filter(Objects::isNull)
            .count();

        if (c > 4) throw new Exception("Invalid number of blanks: " + row);
    }
}
