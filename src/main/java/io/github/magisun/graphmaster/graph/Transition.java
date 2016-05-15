package io.github.magisun.graphmaster.graph;

/**
 * Represents a transition between two grids.
 */
public class Transition {

    private final Grid origin, destination;
    private MoveType type;

    public Transition(Grid g1, Grid g2) {
        if(Math.abs(g1.getEmptyX()-g2.getEmptyX()) +
                Math.abs(g1.getEmptyY()-g2.getEmptyY()) > 1) {
            throw new IllegalArgumentException(
                    "Grid transitions must involve one or zero moves.");
        }

        origin = g1;
        destination = g2;

        if(g1.getEmptyX() == g2.getEmptyX() &&
                g1.getEmptyY() == g2.getEmptyY()) {
            type = MoveType.NONE;
        } else if(g1.getEmptyX() < g2.getEmptyX()) {
            type = MoveType.RIGHT;
        } else if(g1.getEmptyX() > g2.getEmptyX()) {
            type = MoveType.LEFT;
        } else if(g1.getEmptyY() < g2.getEmptyY()) {
            type = MoveType.DOWN;
        } else {
            type = MoveType.UP;
        }
    }

    public Grid getOrigin() {
        return origin;
    }

    public Grid getDestination() {
        return destination;
    }

    public MoveType getMoveType() {
        return type;
    }
}
