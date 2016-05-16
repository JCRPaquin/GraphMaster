package io.github.magisun.graphmaster.graph;

import edu.uci.ics.jung.graph.DelegateForest;
import org.junit.Test;

import java.io.*;

/**
 * Tests for the GridForestSerializer class.
 */
public class GridForestSerializerTest {

    @Test
    public void roundTripTest() throws IOException, ClassNotFoundException {
        GridForestSerializer wrapper = new GridForestSerializer();
        DelegateForest<Grid, Transition> forest = wrapper.getForest();

        Grid original = new Grid(3,3);
        Grid first = new Grid(original, MoveType.LEFT);

        Transition t1 = new Transition(original, first);

        forest.addVertex(original);
        forest.addVertex(first);
        forest.addEdge(t1, original, first);

        File tmp = File.createTempFile("test-file", ".tmp");
        ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(tmp));

        out.writeObject(wrapper);
        out.flush();
        out.close();

        ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(tmp));

        GridForestSerializer newForest = (GridForestSerializer) in.readObject();
        in.close();
    }
}
