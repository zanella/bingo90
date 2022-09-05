package zane.raf.bingo_90;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

record Strip(List<Ticket> tickets) {
    static Strip create() {
        return Strip.of(Pool.create(), new Random());
    }

    static Strip of(final Map<Integer, LinkedList<Integer>> pool, final Random random) {
        final var l = new LinkedList<Ticket>();

        for (int i = 0; i < 6; i++) { l.add( Ticket.of(pool, random) ); }

        return new Strip(l);
    }

    @Override
    public String toString() {
        return "Strip{\n"
            + tickets.stream().map(Ticket::toString).collect(Collectors.joining())
            + "\n}";
    }
}
