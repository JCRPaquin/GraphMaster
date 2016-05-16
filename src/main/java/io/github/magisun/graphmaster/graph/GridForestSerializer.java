package io.github.magisun.graphmaster.graph;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import java.io.*;
import java.util.*;

/**
 * A serialization wrapper for Grid forests.
 */
public class GridForestSerializer implements Serializable {

    private DelegateForest<Grid, Transition> forest;

    public GridForestSerializer() {
        forest = new DelegateForest<>(new DirectedSparseGraph<>());
    }

    public GridForestSerializer(DirectedGraph<Grid, Transition> graph) {
        forest = new DelegateForest<>(graph);
    }

    public DelegateForest<Grid, Transition> getForest() {
        return forest;
    }

    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        if(forest.getVertices().isEmpty()) {
            throw new IOException("Cannot write empty graph.");
        }

        HashMap<Grid, Integer> indexMap = new HashMap<>();
        int index = 0;
        ArrayList<Grid> roots = new ArrayList<>(),
                nonRoots = new ArrayList<>();
        for(Grid g : forest.getVertices()) {
            if(g.isDerived()) {
                nonRoots.add(g);
            } else {
                roots.add(g);
            }
            indexMap.put(g, index++);
        }

        if(roots.isEmpty()) {
            throw new IOException("No root Grids found in the graph.");
        }
        Grid root = roots.get(0);
        stream.writeInt(root.getWidth());
        stream.writeInt(root.getHeight());
        stream.writeInt(forest.getVertices().size());
        stream.writeInt(roots.size());

        for(int i = 0; i < nonRoots.size(); i++) {
            Grid child = nonRoots.get(i);
            stream.writeInt(indexMap.get(child));
            stream.writeInt(indexMap.get(child.getParent()));
            stream.writeInt(child.getExecutedMove().ordinal());
        }

        for(int i = 0; i < roots.size(); i++) {
            stream.writeInt(indexMap.get(roots.get(i)));
            roots.get(i).writeToStream(stream);
        }
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        forest = new DelegateForest<>(new DirectedSparseGraph<>());

        int width = stream.readInt(),
                height = stream.readInt();

        int totalVertices = stream.readInt();
        int roots = stream.readInt();

        Set<Integer> unresolved = new HashSet<>();
        Map<Integer, ArrayList<UnfinishedPair>> unfinished = new HashMap<>();
        Grid[] grids = new Grid[totalVertices];

        for(int i = 0; i < (totalVertices - roots); i++) {
            int childID = stream.readInt();
            int parentID = stream.readInt();
            MoveType moveType = MoveType.values()[stream.readInt()];

            if(!unfinished.containsKey(parentID)) {
                unfinished.put(parentID, new ArrayList<>());
            }

            unfinished.get(parentID).add(new UnfinishedPair(childID, moveType));
            unresolved.add(childID);
        }

        for(int i = 0; i < roots; i++) {
            int index = stream.readInt();
            grids[index] = readGrid(stream, width, height);
            forest.addVertex(grids[index]);

            LinkedList<Integer> toFinish = new LinkedList<>();
            toFinish.add(index);
            while(!toFinish.isEmpty()) {
                int next = toFinish.remove();
                Grid parent = grids[next];

                if(!unfinished.containsKey(next)) continue;

                for(UnfinishedPair pair : unfinished.get(next)) {
                    Grid g = new Grid(grids[next], pair.moveType);
                    forest.addVertex(g);
                    forest.addEdge(new Transition(parent, g), parent, g);

                    grids[pair.finalID] = g;
                    unresolved.remove(pair.finalID);
                    toFinish.add(pair.finalID);
                }
            }
        }

        if(!unresolved.isEmpty()) {
            throw new IOException("Graph description was somehow incomplete.");
        }
    }

    private Grid readGrid(ObjectInputStream stream, int width, int height)
            throws IOException {
        int[][] data = new int[width][height];

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                data[x][y] = stream.readInt();
            }
        }

        return new Grid(data);
    }

    private void readObjectNoData()
            throws ObjectStreamException {
        throw new ObjectStreamException("Something is broken.") {};
    }

    private class UnfinishedPair {
        public final Integer finalID;
        public final MoveType moveType;

        public UnfinishedPair(Integer finalID, MoveType moveType) {
            this.finalID = finalID;
            this.moveType = moveType;
        }
    }
}
