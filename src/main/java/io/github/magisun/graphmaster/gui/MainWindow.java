package io.github.magisun.graphmaster.gui;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.picking.ClosestShapePickSupport;
import io.github.magisun.graphmaster.graph.Grid;
import io.github.magisun.graphmaster.graph.Transition;
import io.github.magisun.graphmaster.gui.control.EditNodesPlugin;
import io.github.magisun.graphmaster.gui.transformers.GridIconTransformer;
import io.github.magisun.graphmaster.gui.transformers.GridShapeTransformer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.concurrent.Semaphore;

/**
 * The main window for the application.
 *
 * Only one object of this class should exist at any time during
 * the application's lifecycle.
 */
public class MainWindow extends JFrame {

    private static final String TITLE = "Graph Master";
    private static MainWindow SINGLETON;

    private DirectedSparseGraph<Grid, Transition> graph;

    private InfoPanel info;
    private ControlPanel control;
    private VisualizationViewer<Grid, Transition> viewer;
    private SatelliteVisualizationViewer<Grid, Transition> satellite;

    private MainWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(800, 650);
        setMinimumSize(getSize());

        setTitle(TITLE);
        setBackground(Color.WHITE);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        Container contentPane = this.getContentPane();

        info = new InfoPanel();
        contentPane.add(info);
        layout.putConstraint(SpringLayout.EAST, info,
                -10, SpringLayout.EAST, contentPane);
        layout.putConstraint(SpringLayout.WEST, info,
                10, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.SOUTH, info,
                -10, SpringLayout.SOUTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, info,
                -160, SpringLayout.SOUTH, contentPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentPane.add(splitPane);
        layout.putConstraint(SpringLayout.NORTH, splitPane,
                10, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.EAST, splitPane,
                0, SpringLayout.EAST, info);
        layout.putConstraint(SpringLayout.WEST, splitPane,
                -300, SpringLayout.EAST, splitPane);
        layout.putConstraint(SpringLayout.SOUTH, splitPane,
                -10, SpringLayout.NORTH, info);

        graph = new DirectedSparseGraph<>();
        Forest<Grid, Transition> forest = new DelegateForest<>(graph);
        TreeLayout<Grid, Transition> treeLayout = new TreeLayout<>(forest, 100, 100);
        viewer = new VisualizationViewer<>(treeLayout);
        viewer.setPickSupport(new ClosestShapePickSupport<>(viewer));
        viewer.getRenderContext()
                .setVertexIconTransformer(GridIconTransformer.SINGLETON);
        viewer.getRenderContext()
                .setVertexShapeTransformer(GridShapeTransformer.SINGLETON);
        DefaultModalGraphMouse<Grid, Transition> mouse = new DefaultModalGraphMouse<>();
        mouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        mouse.add(new EditNodesPlugin());
        viewer.setGraphMouse(mouse);

        contentPane.add(viewer);
        layout.putConstraint(SpringLayout.WEST, viewer,
                0, SpringLayout.WEST, info);
        layout.putConstraint(SpringLayout.EAST, viewer,
                -10, SpringLayout.WEST, splitPane);
        layout.putConstraint(SpringLayout.SOUTH, viewer,
                0, SpringLayout.SOUTH, splitPane);
        layout.putConstraint(SpringLayout.NORTH, viewer,
                0, SpringLayout.NORTH, splitPane);
        viewer.setBorder(new LineBorder(Color.BLACK));

        satellite = new SatelliteVisualizationViewer<>(viewer, new Dimension(0,0));
        splitPane.setTopComponent(satellite);

        control = new ControlPanel();
        splitPane.setBottomComponent(control);

        pack();
        setVisible(true);
    }

    /**
     * Initializes, if necessary, and returns the singleton instance
     * of MainWindow.
     *
     * If window initialization is necessary, this method will block
     * until window construction is complete.
     *
     * @return the singleton instance of MainWindow
     */
    public static MainWindow getWindow() {
        if(SINGLETON == null) {
            if(SwingUtilities.isEventDispatchThread()) {
                SINGLETON = new MainWindow();
            } else {
                // Block until window is constructed
                final Semaphore constructionLock = new Semaphore(1);
                constructionLock.acquireUninterruptibly();

                SwingUtilities.invokeLater(() -> {
                    SINGLETON = new MainWindow();
                    constructionLock.release();
                });

                constructionLock.acquireUninterruptibly();
            }
        }

        return SINGLETON;
    }

    public DirectedSparseGraph<Grid, Transition> getGraph() {
        return graph;
    }

    public void setGraph(DirectedSparseGraph<Grid, Transition> newGraph) {
        graph = newGraph;
        TreeLayout<Grid, Transition> treeLayout;
        if(!newGraph.getVertices().isEmpty()) {
            Grid g = newGraph.getVertices().iterator().next();
            int gridWidth = g.getIcon().getIconWidth(),
                    gridHeight = g.getIcon().getIconHeight();

            Forest<Grid, Transition> forest = new DelegateForest<>(graph);
            treeLayout = new TreeLayout<>(forest, gridWidth + 50, gridHeight + 50);
        } else {
            Forest<Grid, Transition> forest = new DelegateForest<>(graph);
            treeLayout = new TreeLayout<>(forest);
        }
        viewer.setGraphLayout(treeLayout);
    }

    public void recalculateLayout() {
        setGraph(graph);
    }
}
