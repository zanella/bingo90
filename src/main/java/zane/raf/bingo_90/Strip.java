package zane.raf.bingo_90;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

record Strip(List<Ticket> tickets) {
    static Strip of(final Map<Integer, LinkedList<Integer>> pool, final Random random) {
        final var l = IntStream
            .range(0, 6)
            .mapToObj(i -> Ticket.of(pool, random))
            .toList();

        return new Strip(l);
    }

    @Override
    public String toString() {
        return "Strip{\n"
            + tickets.stream().map(Ticket::toString).collect(Collectors.joining())
            + "\n}";
    }
}
