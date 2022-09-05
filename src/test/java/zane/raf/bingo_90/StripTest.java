package zane.raf.bingo_90;

import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StripTest implements Common {
    @Test
    void testGenerateStrip() {
        IntStream
            .range(0, PERFORMANCE_TEST_NUMBER_OF_LOOPS * 10)
            .parallel()
            .forEach(i -> {
                final var pool = Pool.create();

                final var strip = Strip.of(pool, new Random());

                strip
                    .tickets()
                    .forEach(this::verifyTicketIsValid);

                assertEquals(0, pool.values().stream().mapToInt(LinkedList::size).sum());
            });
    }

    @Test
    void testGenerateStripExecutionTime() {
        // Warmup
        final long warmupStart = System.currentTimeMillis();

        for (int i = 0; i < PERFORMANCE_TEST_NUMBER_OF_LOOPS; i++) { Strip.create(); }

        final var warmupElapsed = System.currentTimeMillis() - warmupStart;

        assertTrue(warmupElapsed < 1000, "Took too long: " + warmupElapsed + "ms");

        // Probably JIT'ed
        final long start = System.currentTimeMillis();

        for (int i = 0; i < PERFORMANCE_TEST_NUMBER_OF_LOOPS; i++) { Strip.create(); }

        final var elapsed = System.currentTimeMillis()- start;

        assertTrue(elapsed < 1000, "Took too long: " + elapsed + "ms");
    }
}