package zane.raf.bingo_90;

import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static zane.raf.bingo_90.Bingo90.generateStrip;

class StripTest implements Common {
    @Test
    void testGenerateStrip() {
        IntStream
            .range(0, 100_000)
            .parallel()
            .forEach(i -> {
                final var pool = Pool.create();

                final var strip = Strip.of(pool, new Random());

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
}