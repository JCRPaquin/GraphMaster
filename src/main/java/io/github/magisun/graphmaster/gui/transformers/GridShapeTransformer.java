package io.github.magisun.graphmaster.gui.transformers;

import com.google.common.base.Function;
import io.github.magisun.graphmaster.graph.Grid;

import javax.swing.*;
import java.awt.*;

/**
 * A transformer functor object that converts Grids to Shapes.
 */
public class GridShapeTransformer implements Function<Grid, Shape> {

    public static final GridShapeTransformer SINGLETON = new GridShapeTransformer();

    private GridShapeTransformer() {}

    @Override
    public Shape apply(Grid grid) {
        Icon icon = grid.getIcon();
        return new Rectangle(icon.getIconWidth(), icon.getIconHeight());
    }
}
