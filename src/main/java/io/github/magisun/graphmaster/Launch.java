package io.github.magisun.graphmaster;

import io.github.magisun.graphmaster.graph.Grid;
import io.github.magisun.graphmaster.graph.MoveType;
import io.github.magisun.graphmaster.graph.Transition;
import io.github.magisun.graphmaster.gui.MainWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * The GraphMaster entrypoint:
 * - Initiates GUI construction
 * - Opens static resources
 * - Prepares scripting engines
 */
public class Launch {

    /**
     * The main program entrypoint.
     *
     * @param args Program arguments
     */
    public static void main(String[] args) {
        MainWindow w = MainWindow.getWindow();


        Grid g = new Grid(3, 3);
        w.getGraph().addVertex(g);

        Grid g2 = new Grid(g, MoveType.UP);
        g2.setLensColor(new Color(100, 0, 100, 100));
        w.getGraph().addVertex(g2);

        Transition t = new Transition(g, g2);
        w.getGraph().addEdge(t, g, g2);

        Grid g3 = new Grid(g, MoveType.LEFT);
        w.getGraph().addVertex(g3);

        Transition t2 = new Transition(g, g3);
        w.getGraph().addEdge(t2, g, g3);

        Grid g4 = new Grid(g3, MoveType.UP);
        Grid g5 = new Grid(g4, MoveType.LEFT);
        w.getGraph().addVertex(g4);
        w.getGraph().addVertex(g5);

        Transition t3 = new Transition(g3, g4);
        w.getGraph().addEdge(t3, g3, g4);

        Transition t4 = new Transition(g4, g5);
        w.getGraph().addEdge(t4, g4, g5);

        Grid test = new Grid(g4, MoveType.LEFT);
        System.out.println(w.getGraph().containsVertex(test));

        w.recalculateLayout();


        System.out.println("Window constructed: " + w.toString());

        /*
        try {
            ImageIcon iico = (ImageIcon) g2.getIcon();
            BufferedImage img = (BufferedImage) iico.getImage();
            File output = new File("testGrid.png");
            ImageIO.write(img, "png", output);

            System.out.println("Wrote test grid image to: " + output.getAbsolutePath());
        } catch(Exception ex) {
            System.err.println("Failed to write test png file.");
            ex.printStackTrace();
        }
        */
    }
}
