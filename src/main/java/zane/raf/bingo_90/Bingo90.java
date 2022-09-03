package zane.raf.bingo_90;

import java.util.Random;

public final class Bingo90 {

    /**
     * Generates a valid strip, consisting of 6 tickets
     *
     * @return a valid {@code Strip}
     */
    public static Strip generateStrip() {
        return Strip.of(Pool.create(), new Random());
    }

    ///////////////////////////////////////////////////////////////////////////

    private Bingo90() {}
}
