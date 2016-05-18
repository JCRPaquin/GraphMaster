package io.github.magisun.graphmaster.gui.control;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;
import io.github.magisun.graphmaster.graph.Grid;
import io.github.magisun.graphmaster.graph.Transition;
import io.github.magisun.graphmaster.gui.AddNodeDialog;
import io.github.magisun.graphmaster.gui.AddOriginNodeDialog;
import io.github.magisun.graphmaster.gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A plugin that allows you to edit the graph.
 */
public class EditNodesPlugin implements GraphMousePlugin, MouseListener {
    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public void setModifiers(int i) {

    }

    @Override
    public boolean checkModifiers(MouseEvent mouseEvent) {
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    // TODO: Extract this into a picker support class and do things properly.
    @Override
    public void mousePressed(MouseEvent e) {
        if(!SwingUtilities.isRightMouseButton(e)) return;

        Point down = e.getPoint();
        VisualizationViewer<Grid, Transition> vv =
                (VisualizationViewer<Grid, Transition>)e.getSource();
        GraphElementAccessor<Grid, Transition> pickSupport = vv.getPickSupport();

        Layout<Grid, Transition> layout = vv.getGraphLayout();

        Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer()
                .inverseTransform(Layer.LAYOUT, down);

        // Add node to empty graph
        if(layout.getGraph().getVertices().isEmpty()) {
            addNode(vv, null);
            return;
        }

        double minDist = Double.MAX_VALUE;
        Grid closest = null;
        while(true) {
            try {
                for(Grid g : layout.getGraph().getVertices()) {
                    Point2D gPos = layout.apply(g);
                    double dist = gPos.distance(graphDown);
                    if(dist < minDist) {
                        minDist = dist;
                        closest = g;
                    }
                }
                break;
            } catch(Exception ex) {}
        }

        Point2D location = layout.apply(closest);
        Rectangle2D bounds =
                (Rectangle2D) vv.getRenderContext().getVertexShapeTransformer().apply(closest);
        if(Math.abs(graphDown.getX() - location.getX()) > bounds.getWidth()/2 ||
                Math.abs(graphDown.getY() - location.getY()) > bounds.getHeight()/2) {
            return;
        }


        if((e.getModifiersEx() &
                (InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) > 0) {
            deleteNode(vv, closest);
        } else {
            addNode(vv, closest);
        }
    }

    private void addNode(VisualizationViewer<Grid, Transition> viewer, Grid target) {
        JDialog dialog;

        if(target != null) {
            dialog = new AddNodeDialog(target, viewer.getGraphLayout().getGraph());
        } else {
            dialog = new AddOriginNodeDialog(new Function<Grid, Void>() {
                @Override
                public Void apply(Grid grid) {
                    viewer.getGraphLayout().getGraph().addVertex(grid);
                    MainWindow.getWindow().recalculateLayout();
                    return null;
                }
            });
        }

        dialog.setLocationRelativeTo(MainWindow.getWindow());
        dialog.setVisible(true);
        dialog.requestFocus();
        dialog.toFront();
    }

    private void deleteNode(VisualizationViewer<Grid, Transition> viewer, Grid target) {
        System.out.println("Removing vertex " + target.toString());
        viewer.getGraphLayout().getGraph().removeVertex(target);
        MainWindow.getWindow().recalculateLayout();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}
}
