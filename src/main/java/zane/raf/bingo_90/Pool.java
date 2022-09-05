package zane.raf.bingo_90;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

abstract class Pool {
    private static final List<Integer> FIRST_COLUMN_SPACE = IntStream.rangeClosed(1, 9).boxed().toList();
    private static final List<Integer> SECOND_COLUMN_SPACE = IntStream.rangeClosed(10, 19).boxed().toList();
    private static final List<Integer> THIRD_COLUMN_SPACE = IntStream.rangeClosed(20, 29).boxed().toList();
    private static final List<Integer> FOURTH_COLUMN_SPACE = IntStream.rangeClosed(30, 39).boxed().toList();
    private static final List<Integer> FIFTH_COLUMN_SPACE = IntStream.rangeClosed(40, 49).boxed().toList();
    private static final List<Integer> SIXTH_COLUMN_SPACE = IntStream.rangeClosed(50, 59).boxed().toList();
    private static final List<Integer> SEVENTH_COLUMN_SPACE = IntStream.rangeClosed(60, 69).boxed().toList();
    private static final List<Integer> EIGHTH_COLUMN_SPACE = IntStream.rangeClosed(70, 79).boxed().toList();
    private static final List<Integer> NINTH_COLUMN_SPACE = IntStream.rangeClosed(80, 90).boxed().toList();

    /**
     *
     * @param space The list to be shuffled
     * @return A shuffled copy of the received list
     */
    static LinkedList<Integer> shuffleList(final List<Integer> space) {
        final var l = new LinkedList<>(space);

        Collections.shuffle(l);

        return l;
    }

    /**
     * @return A map, consisting of: column index -> shuffled list of numbers, valid for said column
     */
    static Map<Integer, LinkedList<Integer>> create() {
        return Map.of(
            0, shuffleList(FIRST_COLUMN_SPACE),     1, shuffleList(SECOND_COLUMN_SPACE),
            2, shuffleList(THIRD_COLUMN_SPACE),     3, shuffleList(FOURTH_COLUMN_SPACE),
            4, shuffleList(FIFTH_COLUMN_SPACE),     5, shuffleList(SIXTH_COLUMN_SPACE),
            6, shuffleList(SEVENTH_COLUMN_SPACE),   7, shuffleList(EIGHTH_COLUMN_SPACE),
            8, shuffleList(NINTH_COLUMN_SPACE)
        );
    }
}