package zane.raf.bingo_90;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.graalvm.collections.Pair;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

record Ticket(ArrayList<Integer> row1, ArrayList<Integer> row2, ArrayList<Integer> row3) {
    private static final List<Integer> NULLED_ROW = Arrays.asList(null, null, null, null, null, null, null, null, null);

    static final List<Integer> COLUMN_INDEXES = IntStream.range(0, 9).boxed().toList();

    /**
     * @param pool Available numbers
     * @param random RNG
     * @return A valid {@code Ticket}
     */
    static Ticket of(final Map<Integer, LinkedList<Integer>> pool, final Random random) {
        return new Ticket(fillRow(pool), fillRow(pool), fillRow(pool))
            .balanceTicket(pool, random)
            .sortColumns();
    }

    /**
     *
     * @param pool Available numbers
     * @return Returns a "valid" row: 5 numbers + 4 blank spaces
     */
    static ArrayList<Integer> fillRow(final Map<Integer, LinkedList<Integer>> pool) {
        final var row = new ArrayList<>(NULLED_ROW);

        final var columnsToFill = new LinkedList<>(COLUMN_INDEXES);
        Collections.shuffle(columnsToFill);

        final var minPoolSize = new AtomicInteger(Integer.MAX_VALUE);
        for (int i = 0; i < 9; i++) { minPoolSize.set( Math.min(minPoolSize.get(), pool.get(i).size()) ); }

        if (minPoolSize.get() < 8) { // If not the first 2 tickets, give preference to the unused pools
            pool
                .entrySet()
                .stream()
                .filter(e -> ((e.getValue().size() - minPoolSize.get()) >= 1) )
                .sorted((x, y) -> Integer.compare(y.getValue().size(), x.getValue().size())) // inverted, larger pools first
                .forEach(e -> columnsToFill.addFirst(e.getKey()));
        }

        new LinkedHashSet<>(columnsToFill) // gets rid of repeated columns
            .stream()
            .filter(idx -> !pool.get(idx).isEmpty())
            .limit(5) // The row is already filled with nulls, only numbers are needed, (9 - 4) -> 5
            .forEach(columnIdx -> row.set(columnIdx, pool.get(columnIdx).pop()));

        return row;
    }

    /**
     * @return A map consisting of "number of times a blank happens in the column" (frequency) -> column's index
     */
    Map<Integer, Set<Integer>> getFreqOfBlanksPerColumn() {
        return COLUMN_INDEXES
            .stream()
            .map(columnIndex -> {
                final var blanks = (Objects.isNull(row1.get(columnIndex)) ? 1 : 0)
                    + (Objects.isNull(row2.get(columnIndex)) ? 1 : 0)
                    + (Objects.isNull(row3.get(columnIndex)) ? 1 : 0);

                return Pair.create(columnIndex, blanks);
            })
            .collect(
                groupingBy(Pair::getRight,
                    mapping(Pair::getLeft, toCollection(HashSet::new))));
    }

    /**
     *
     * @param random RNG
     * @return a random row
     */
    private ArrayList<Integer> getRandomRow(final Random random) {
        final var rowIdx = random.nextInt(1, 4);

        return switch (rowIdx) {
            case 1 -> row1;
            case 2 -> row2;
            case 3 -> row3;
            default -> throw new IllegalStateException("Unexpected value: " + rowIdx);
        };
    }

    /**
     * Checks that there are no invalid columns (w/ 3 blanks), and if there are modify the ticket to prevent it.
     *
     * @param pool available numbers for filling the ticket
     * @param random RNG
     * @return this, only returned to enable a fluent API
     */
    Ticket balanceTicket(final Map<Integer, LinkedList<Integer>> pool, final Random random) {
        final var freqOfBlanksPerColumn = getFreqOfBlanksPerColumn();

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
                    final var row = getRandomRow(random);

                    // Insert a new value: [1, null, 3] -> [1, X, 3]
                    row.set(invalidColumnIdx, pool.get(invalidColumnIdx).pop());

                    // Pick column to poke a blank
                    int columnToModify = unmodifiableColumns.stream().findFirst().orElseThrow();

                    // If the random column is one of not-to-be-modified or the row is already null at this index
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

        return this;
    }

    /**
     * Reorders the column's items so that they are shown on an ascending order
     *
     * @return this, only returned to enable a fluent API
     */
    Ticket sortColumns() {
        COLUMN_INDEXES
            .forEach(columnIndex -> {
                final var rows = Arrays.asList(row1, row2, row3);

                final var columnValues = rows
                    .stream()
                    .map(row -> row.get(columnIndex))
                    .filter(Objects::nonNull) // NULL values are not reordered
                    .sorted()
                    .collect(toCollection(LinkedList::new));

                for (int i = 0; !columnValues.isEmpty(); i++) {
                    final var row = rows.get(i);

                    if (Objects.nonNull(row.get(columnIndex))) {
                        row.set(columnIndex, columnValues.removeFirst());
                    }
                }
            });

        return this;
    }

    @Override
    public String toString() {
        return "Ticket{" + "\nrow1=" + row1 + "\nrow2=" + row2 + "\nrow3=" + row3 + "}\n";
    }
}
