package zane.raf.bingo_90;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoolTest implements Common {

    @Test
    void testShuffleList() {
        final var l = Arrays.asList(1, 2, 3, 4, 5);

        final var shuffledL = Pool.shuffleList(new ArrayList<>(l));

        assertNotEquals(l, shuffledL);
    }

    @Test
    void testCreate() {
        final var pool = Pool.create();

        assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8), pool.keySet());
        assertEquals(9, pool.get(0).size());
        IntStream.rangeClosed(1, 7).forEach(idx -> assertEquals(10, pool.get(idx).size()));
        assertEquals(11, pool.get(8).size());

        final var entirePool = pool
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        IntStream.rangeClosed(1, 90).forEach(number -> assertTrue(entirePool.contains(number)));
    }

    @Test
    void testCreatePerformance() {
        // warmup
        for (int i = 0; i < PERFORMANCE_TEST_NUMBER_OF_LOOPS; i++) { Pool.create(); }

        final long start = System.currentTimeMillis();

        for (int i = 0; i < PERFORMANCE_TEST_NUMBER_OF_LOOPS; i++) { Pool.create(); }

        final var elapsed = System.currentTimeMillis() - start;

        System.out.println("elapsed: " + elapsed);

        assertTrue(elapsed < 100, "Took too long: " + elapsed + "ms");
    }
}
