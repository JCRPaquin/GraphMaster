package io.github.magisun.graphmaster.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the Grid class.
 */
public class GridTest {

    @Test
    public void baseGridMoves() {
        Grid moo = new Grid(3, 3);
        assertEquals(moo.getPotentialMoves().keySet().size(), 2);
    }

    @Test
    public void translatedGridMoves() {
        Grid initial = new Grid(3, 3);
        Grid next = new Grid(initial, MoveType.LEFT);
        Grid second = new Grid(next, MoveType.UP);
        Grid corner = new Grid(next, MoveType.LEFT);

        assertEquals(4, second.getPotentialMoves().keySet().size());
        assertEquals(3, next.getPotentialMoves().keySet().size());
        assertEquals(2, corner.getPotentialMoves().keySet().size());
    }

    @Test
    public void scoringTest() {
        Grid initial = new Grid(3, 3);
        Grid first = new Grid(initial, MoveType.LEFT);
        Grid second = new Grid(first, MoveType.LEFT);
        Grid third = new Grid(second, MoveType.UP);
        Grid fourth = new Grid(third, MoveType.DOWN);

        assertEquals(0, initial.getScore());
        assertEquals(1, first.getScore());
        assertEquals(2, second.getScore());
        assertEquals(3, third.getScore());
        assertEquals(2, fourth.getScore());
    }
}
