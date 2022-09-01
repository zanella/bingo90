package zane.raf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.graalvm.collections.Pair;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

public final class Bingo90Generator {
    private static final List<Integer> FIRST_COLUMN_SPACE = IntStream.rangeClosed(1, 9).boxed().toList();
    private static final List<Integer> SECOND_COLUMN_SPACE = IntStream.rangeClosed(10, 19).boxed().toList();
    private static final List<Integer> THIRD_COLUMN_SPACE = IntStream.rangeClosed(20, 29).boxed().toList();
    private static final List<Integer> FOURTH_COLUMN_SPACE = IntStream.rangeClosed(30, 39).boxed().toList();
    private static final List<Integer> FIFTH_COLUMN_SPACE = IntStream.rangeClosed(40, 49).boxed().toList();
    private static final List<Integer> SIXTH_COLUMN_SPACE = IntStream.rangeClosed(50, 59).boxed().toList();
    private static final List<Integer> SEVENTH_COLUMN_SPACE = IntStream.rangeClosed(60, 69).boxed().toList();
    private static final List<Integer> EIGHTH_COLUMN_SPACE = IntStream.rangeClosed(70, 79).boxed().toList();
    private static final List<Integer> NINTH_COLUMN_SPACE = IntStream.rangeClosed(80, 90).boxed().toList();

    private static final Random random = new Random();

    private Bingo90Generator() {}

    record Ticket(ArrayList<Integer> row1, ArrayList<Integer> row2, ArrayList<Integer> row3) {}

    record Strip(Ticket t1, Ticket t2, Ticket t3, Ticket t4, Ticket t5, Ticket t6)  {}

    private static LinkedList<Integer> shuffleList(List<Integer> space) {
        final var l = new LinkedList<>(space);

        Collections.shuffle(l);

        return l;
    }

    /**
     *
     * @param pool Available numbers
     * @return Returns a "valid" row: 5 numbers + 4 blank spaces
     */
    static ArrayList<Integer> fillRow(final Map<Integer, LinkedList<Integer>> pool) {
        final var row = new ArrayList<Integer>(9);

        var numberOfBlanks = 0;

        for (int i = 0; i < 9; i++) {
            final var l = pool.get(i);

            final var v = l.isEmpty() ? null : l.pop();

            if (Objects.isNull(v)) numberOfBlanks++;

            row.add(v);
        }

        while (numberOfBlanks < 4) {
            final var idx = random.nextInt(0, 9);

            if ( Objects.nonNull(row.get(idx)) ) {
                pool.get(idx).add( row.get(idx) );

                row.set(idx, null);

                numberOfBlanks++;
            }
        }

        return row;
    }

    private static ArrayList<Integer> getRandomRow(final Ticket ticket) {
        final var rowIdx = random.nextInt(1, 4);

        return switch (rowIdx) {
            case 1 -> ticket.row1;
            case 2 -> ticket.row2;
            case 3 -> ticket.row3;
            default -> throw new IllegalStateException("Unexpected value: " + rowIdx);
        };
    }

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

    static Ticket balanceTicket(final Map<Integer, LinkedList<Integer>> pool, final Ticket ticket) {
        final var freqOfBlanksPerColumn = getFreqOfBlanksPerColumn(ticket);

        /*    3      2       1      0  | 3 -> Has to gain a number
         * [null], [null], [null], [A] | 2 -> Cannot lose a number
         * [null], [null],  [Y],   [B] | 1, 0 -> Can lose a number and keep balance
         * [null],  [X],    [Z],   [C] | */
        if (freqOfBlanksPerColumn.containsKey(3)) {
            /* System.out.println("Invalid ticket BEFORE: ");
            System.out.println(ticket.row1);
            System.out.println(ticket.row2);
            System.out.println(ticket.row3);
            System.out.println("\nfreqOfBlanksPerColumn: " + freqOfBlanksPerColumn); */

            freqOfBlanksPerColumn
                .get(3)
                .stream()
                .sorted(Comparator.reverseOrder()) // Try to fill the last column first
                .forEach(invalidColumnIdx -> {
                    final var unmodifiableColumns = new HashSet<Integer>();
                    unmodifiableColumns.addAll(freqOfBlanksPerColumn.getOrDefault(3, emptySet()));
                    unmodifiableColumns.addAll(freqOfBlanksPerColumn.getOrDefault(2, emptySet()));

                    final var row = getRandomRow(ticket);

                    // Insert a new value: [1, null, 3] -> [1, X, 3]
                    row.set(invalidColumnIdx, pool.get(invalidColumnIdx).pop());

                    // Pick column to poke a blank
                    int columnToModify = unmodifiableColumns.stream().findFirst().orElseThrow();

                    while ( unmodifiableColumns.contains(columnToModify) ) { columnToModify = random.nextInt(0, 9); }

                    // Return to pool
                    pool.get(columnToModify).add( row.get(columnToModify) );
                    //pool.get(columnToModify).add( rowToModify.get(columnToModify) );

                    // Add a blank space: [1, X, null]
                    row.set(columnToModify, null);
                    //rowToModify.set(columnToModify, null);

                    // Update frequency map
                    for (int freqOfBlanks = 1; freqOfBlanks >= 0; freqOfBlanks--) {
                        final var s = freqOfBlanksPerColumn.getOrDefault(freqOfBlanks, emptySet());

                        if (s.contains(columnToModify)) {
                            s.remove(columnToModify);

                            final var incFreqSet = freqOfBlanksPerColumn.getOrDefault(freqOfBlanks + 1, new HashSet<>());

                            incFreqSet.add(columnToModify);

                            freqOfBlanksPerColumn.put(freqOfBlanks + 1, incFreqSet);
                        }
                    }
                });

            /* System.out.println("\n\nInvalid ticket AFTER: ");
            System.out.println(ticket.row1);
            System.out.println(ticket.row2);
            System.out.println(ticket.row3);
            System.out.println("\nfreqOfBlanksPerColumn: " + freqOfBlanksPerColumn); */
        }

        return ticket;
    }

    static Map<Integer, LinkedList<Integer>> getPool() {
        return Map.of(
            0, shuffleList(FIRST_COLUMN_SPACE),     1, shuffleList(SECOND_COLUMN_SPACE),
            2, shuffleList(THIRD_COLUMN_SPACE),     3, shuffleList(FOURTH_COLUMN_SPACE),
            4, shuffleList(FIFTH_COLUMN_SPACE),     5, shuffleList(SIXTH_COLUMN_SPACE),
            6, shuffleList(SEVENTH_COLUMN_SPACE),   7, shuffleList(EIGHTH_COLUMN_SPACE),
            8, shuffleList(NINTH_COLUMN_SPACE)
        );
    }

    public static Strip generateStrip() {
        final var m = getPool();

        final var ticket = balanceTicket(m, new Ticket(fillRow(m), fillRow(m), fillRow(m)) );

        random.nextInt(1, 4);

        return null;
    }
}
