package zane.raf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.graalvm.collections.Pair;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

public final class Bingo90Generator {

    /**
     * Generates a valid strip, consisting of 6 tickets
     *
     * @return a valid {@code Strip}
     */
    public static Strip generateStrip() {
        return generateStrip(getPool(), new Random());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final List<Integer> FIRST_COLUMN_SPACE = IntStream.rangeClosed(1, 9).boxed().toList();
    private static final List<Integer> SECOND_COLUMN_SPACE = IntStream.rangeClosed(10, 19).boxed().toList();
    private static final List<Integer> THIRD_COLUMN_SPACE = IntStream.rangeClosed(20, 29).boxed().toList();
    private static final List<Integer> FOURTH_COLUMN_SPACE = IntStream.rangeClosed(30, 39).boxed().toList();
    private static final List<Integer> FIFTH_COLUMN_SPACE = IntStream.rangeClosed(40, 49).boxed().toList();
    private static final List<Integer> SIXTH_COLUMN_SPACE = IntStream.rangeClosed(50, 59).boxed().toList();
    private static final List<Integer> SEVENTH_COLUMN_SPACE = IntStream.rangeClosed(60, 69).boxed().toList();
    private static final List<Integer> EIGHTH_COLUMN_SPACE = IntStream.rangeClosed(70, 79).boxed().toList();
    private static final List<Integer> NINTH_COLUMN_SPACE = IntStream.rangeClosed(80, 90).boxed().toList();

    private Bingo90Generator() {}

    record Ticket(ArrayList<Integer> row1, ArrayList<Integer> row2, ArrayList<Integer> row3) {
        @Override
        public String toString() {
            return "Ticket{" + "\nrow1=" + row1 + "\nrow2=" + row2 + "\nrow3=" + row3 + "}\n";
        }
    }

    record Strip(List<Ticket> tickets) {
        @Override
        public String toString() {
            return "Strip{\n"
                + tickets.stream().map(Ticket::toString).collect(Collectors.joining())
                + "\n}";
        }
    }

    /**
     *
     * @param space The list to be shuffled
     * @return A shuffled copy of the received list
     */
    private static LinkedList<Integer> shuffleList(final List<Integer> space) {
        final var l = new LinkedList<>(space);

        Collections.shuffle(l);

        return l;
    }

    private static final List<Integer> NULLED_ROW = Arrays.asList(null, null, null, null, null, null, null, null, null);

    private static final List<Integer> COLUMN_INDEXES = IntStream.range(0, 9).boxed().toList();

    /**
     *
     * @param pool Available numbers
     * @return Returns a "valid" row: 5 numbers + 4 blank spaces
     */
    static ArrayList<Integer> fillRow(final Map<Integer, LinkedList<Integer>> pool) {
        final var row = new ArrayList<>(NULLED_ROW);

        final var columnsToFill = new LinkedList<>(COLUMN_INDEXES);
        Collections.shuffle(columnsToFill);

        final var minPoolSize = pool.values().stream().mapToInt(LinkedList::size).min().orElseThrow();

        pool
            .entrySet()
            .stream()
            .filter(e -> ((e.getValue().size() - minPoolSize) >= 1) )
            .sorted((x, y) -> Integer.compare(y.getValue().size(), x.getValue().size())) // inverted, larger pools first
            .forEach(e -> columnsToFill.addFirst(e.getKey()));

        new LinkedHashSet<>(columnsToFill) // gets rid of repeated columns
            .stream()
            .filter(idx -> !pool.get(idx).isEmpty())
            .limit(5)
            .forEach(idx -> row.set(idx, pool.get(idx).pop()));

        return row;
    }

    /**
     *
     * @param random RNG
     * @param ticket {@code Ticket} to get a random row from
     * @return a random row
     */
    private static ArrayList<Integer> getRandomRow(final Random random, final Ticket ticket) {
        final var rowIdx = random.nextInt(1, 4);

        return switch (rowIdx) {
            case 1 -> ticket.row1;
            case 2 -> ticket.row2;
            case 3 -> ticket.row3;
            default -> throw new IllegalStateException("Unexpected value: " + rowIdx);
        };
    }

    /**
     *
     * @param ticket .
     * @return A map consisting of "number of times a blank happens in the column" (frequency) -> column's index
     */
    static Map<Integer, Set<Integer>> getFreqOfBlanksPerColumn(final Ticket ticket) {
        return IntStream
            .range(0, 9)
            .mapToObj(idx -> {
                final var blanks = (Objects.isNull(ticket.row1.get(idx)) ? 1 : 0)
                    + (Objects.isNull(ticket.row2.get(idx)) ? 1 : 0)
                    + (Objects.isNull(ticket.row3.get(idx)) ? 1 : 0);

                return Pair.create(idx, blanks);
            })
            .collect(
                groupingBy(Pair::getRight,
                    mapping(Pair::getLeft, toCollection(HashSet::new))));
    }

    /**
     * Checks that there are no invalid columns (w/ 3 blanks), and if there are modify the ticket to prevent it.
     * Reorders the column's items so that they are show on an ascending order
     *
     * @param pool available numbers for filling the ticket
     * @param random RNG
     * @param ticket {@code Ticket}
     * @return the ticket received, only returned to enable a fluent API
     */
    static Ticket balanceTicket(final Map<Integer, LinkedList<Integer>> pool, final Random random, final Ticket ticket) {
        final var freqOfBlanksPerColumn = getFreqOfBlanksPerColumn(ticket);

        /*    3      2       1      0  | 3 -> Has to gain a number
         * [null], [null], [null], [A] | 2 -> Cannot lose a number
         * [null], [null],  [Y],   [B] | 1, 0 -> Can lose a number and keep balance
         * [null],  [X],    [Z],   [C] | */
        if (freqOfBlanksPerColumn.containsKey(3)) {
            final var unmodifiableColumns = new HashSet<Integer>();
            unmodifiableColumns.addAll(freqOfBlanksPerColumn.getOrDefault(3, emptySet()));
            unmodifiableColumns.addAll(freqOfBlanksPerColumn.getOrDefault(2, emptySet()));

            freqOfBlanksPerColumn
                .get(3)
                .stream()
                .sorted(Comparator.reverseOrder()) // Try to fill the last column first
                .forEach(invalidColumnIdx -> {
                    final var row = getRandomRow(random, ticket);

                    // Insert a new value: [1, null, 3] -> [1, X, 3]
                    row.set(invalidColumnIdx, pool.get(invalidColumnIdx).pop());

                    // Pick column to poke a blank
                    int columnToModify = unmodifiableColumns.stream().findFirst().orElseThrow();

                    // If the random column is one of not-to-be-modified or the row is already null on it
                    while (unmodifiableColumns.contains(columnToModify) || Objects.isNull( row.get(columnToModify) )) {
                        columnToModify = random.nextInt(0, 9);
                    }

                    // Return to pool
                    pool.get(columnToModify).add( row.get(columnToModify) );

                    // Add a blank space: columnRand = 2 -> [1, X, null]
                    row.set(columnToModify, null);

                    // Update frequency map of usable columns
                    for (int freqOfBlanks = 1; freqOfBlanks >= 0; freqOfBlanks--) {
                        final var s = freqOfBlanksPerColumn.getOrDefault(freqOfBlanks, emptySet());

                        if (s.contains(columnToModify)) {
                            s.remove(columnToModify);

                            final var newFreq = freqOfBlanks + 1;

                            final var incFreqSet = freqOfBlanksPerColumn.getOrDefault(newFreq, new HashSet<>());

                            incFreqSet.add(columnToModify);

                            freqOfBlanksPerColumn.put(newFreq, incFreqSet);

                            if (newFreq == 2) { unmodifiableColumns.add(columnToModify); }
                        }
                    }
                });
        }

        // TODO: order columns

        return ticket;
    }

    /**
     *
     * @return A map, consisting of: column index -> shuffled list of numbers, valid for said column
     */
    static Map<Integer, LinkedList<Integer>> getPool() {
        return Map.of(
            0, shuffleList(FIRST_COLUMN_SPACE),     1, shuffleList(SECOND_COLUMN_SPACE),
            2, shuffleList(THIRD_COLUMN_SPACE),     3, shuffleList(FOURTH_COLUMN_SPACE),
            4, shuffleList(FIFTH_COLUMN_SPACE),     5, shuffleList(SIXTH_COLUMN_SPACE),
            6, shuffleList(SEVENTH_COLUMN_SPACE),   7, shuffleList(EIGHTH_COLUMN_SPACE),
            8, shuffleList(NINTH_COLUMN_SPACE)
        );
    }

    /**
     * Generates a valid strip
     * @param pool the pool from which the numbers to fill the tickets will be acquired
     * @return a valid {@code Strip}
     */
    static Strip generateStrip(final Map<Integer, LinkedList<Integer>> pool, final Random random) {
        final var l = IntStream
            .range(0, 6)
            .mapToObj(i -> new Ticket(fillRow(pool), fillRow(pool), fillRow(pool)))
            .map(ticket -> balanceTicket(pool, random, ticket))
            .toList();

        return new Strip(l);
    }
}
