package io.github.magisun.graphmaster.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the Transition class.
 */
public class TransitionTest {

    @Test
    public void test() {
        Grid initial = new Grid(90, 90);
        Grid first = new Grid(initial, MoveType.UP);
        Grid second = new Grid(first, MoveType.LEFT);
        Grid third = new Grid(second, MoveType.RIGHT);

        Transition t1 = new Transition(initial, first);
        assertEquals(MoveType.UP, t1.getMoveType());

        Transition t2 = new Transition(first, second);
        assertEquals(MoveType.LEFT, t2.getMoveType());

        Transition t3 = new Transition(second, third);
        assertEquals(MoveType.RIGHT, t3.getMoveType());

        Transition t4 = new Transition(third, initial);
        assertEquals(MoveType.DOWN, t4.getMoveType());
    }
}
