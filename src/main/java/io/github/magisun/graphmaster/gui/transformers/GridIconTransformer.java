package io.github.magisun.graphmaster.gui.transformers;

import com.google.common.base.Function;
import io.github.magisun.graphmaster.graph.Grid;

import javax.swing.*;

/**
 * A transformer functor object that converts Grids to Icons.
 */
public class GridIconTransformer implements Function<Grid, Icon> {

    public static final GridIconTransformer SINGLETON = new GridIconTransformer();

    private GridIconTransformer() {}

    @Override
    public Icon apply(Grid grid) {
        return grid.getIcon();
    }
}
