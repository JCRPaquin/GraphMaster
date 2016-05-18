package io.github.magisun.graphmaster.gui;

import com.google.common.base.Function;
import io.github.magisun.graphmaster.graph.Grid;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import static javax.swing.SpringLayout.*;

/**
 * A dialog for adding the initial node in a graph.
 */
public class AddOriginNodeDialog extends JDialog {

    private static final int MAX_SIZE = 15;

    final private Function<Grid, Void> callback;

    private JPanel contentPane;
    private JButton buttonCancel, buttonOK;
    private JLabel widthLabel, heightLabel;
    private JSpinner widthSpinner, heightSpinner;
    private JScrollPane scrollPane;
    private JTable fieldsTable;
    private GridTableModel currentModel;

    public AddOriginNodeDialog(Function<Grid, Void> callback) {
        super();
        setModal(true);
        setTitle("Add origin node");

        this.callback = callback;

        contentPane = new JPanel();
        setContentPane(contentPane);

        {
            widthLabel = new JLabel("Width: ");
            contentPane.add(widthLabel);

            widthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_SIZE, 1));
            contentPane.add(widthSpinner);

            heightLabel = new JLabel("Height: ");
            contentPane.add(heightLabel);

            heightSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_SIZE, 1));
            contentPane.add(heightSpinner);
        }

        currentModel = new GridTableModel(1, 1);
        fieldsTable = new JTable(currentModel);
        fieldsTable.setShowGrid(true);
        fieldsTable.setRowSorter(null);
        fieldsTable.setTableHeader(null);
        fieldsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane = new JScrollPane(fieldsTable);
        contentPane.add(scrollPane);

        buttonCancel = new JButton("Cancel");
        contentPane.add(buttonCancel);

        buttonOK = new JButton("OK");
        contentPane.add(buttonOK);

        connectSpinners();
        setupButtons();

        constructLayout();
    }

    private void connectSpinners() {
        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                currentModel = new GridTableModel(
                        (Integer) widthSpinner.getValue(),
                        (Integer) heightSpinner.getValue());
                fieldsTable.setModel(currentModel);
            }
        };

        connectSpinner(widthSpinner, listener);
        connectSpinner(heightSpinner, listener);
    }

    private void connectSpinner(JSpinner spinner, ChangeListener listener) {
        JComponent comp = spinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        spinner.addChangeListener(listener);
    }

    private void setupButtons() {
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
                                               @Override
                                               public void actionPerformed(ActionEvent e) {
                                                   onCancel();
                                               }
                                           },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        callback.apply(currentModel.getGrid());

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void constructLayout() {
        SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);

        layout.putConstraint(WEST, widthLabel, 15, WEST, contentPane);
        layout.putConstraint(NORTH, widthLabel, 15, NORTH, contentPane);
        layout.putConstraint(WEST, widthSpinner, 10, EAST, widthLabel);
        layout.putConstraint(NORTH, widthSpinner, 0, NORTH, widthLabel);
        layout.putConstraint(SOUTH, widthSpinner, 25, NORTH, widthSpinner);
        layout.putConstraint(SOUTH, widthLabel, 0, SOUTH, widthSpinner);

        layout.putConstraint(NORTH, heightLabel, 5, SOUTH, widthLabel);
        layout.putConstraint(WEST, heightLabel, 0, WEST, widthLabel);
        layout.putConstraint(WEST, heightSpinner, 10, EAST, heightLabel);
        layout.putConstraint(NORTH, heightSpinner, 0, NORTH, heightLabel);
        layout.putConstraint(SOUTH, heightSpinner, 25, NORTH, heightSpinner);
        layout.putConstraint(SOUTH, heightLabel, 0, SOUTH, heightSpinner);

        layout.putConstraint(NORTH, scrollPane, 10, SOUTH, heightLabel);
        layout.putConstraint(WEST, scrollPane, 0, WEST, heightLabel);
        layout.putConstraint(EAST, contentPane, 15, EAST, scrollPane);
        scrollPane.setPreferredSize(new Dimension(300, 300));

        layout.putConstraint(EAST, buttonCancel, 0, EAST, scrollPane);
        layout.putConstraint(NORTH, buttonCancel, 10, SOUTH, scrollPane);
        layout.putConstraint(EAST, buttonOK, -10, WEST, buttonCancel);
        layout.putConstraint(NORTH, buttonOK, 0, NORTH, buttonCancel);
        layout.putConstraint(SOUTH, contentPane, 15, SOUTH, buttonCancel);

        pack();
    }

    private class GridTableModel extends AbstractTableModel {

        final int width, height, max;
        HashMap<Integer, Point> tileLocations;
        int[][] data;

        public GridTableModel(int width, int height) {
            this.width = width;
            this.height = height;
            this.max = width * height - 1;
            this.data = new int[width][height];
            this.tileLocations = new HashMap<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    data[x][y] = y * width + x + 1;
                    tileLocations.put(data[x][y], new Point(x, y));
                }
            }

            data[width - 1][height - 1] = -1;
            tileLocations.put(-1, new Point(width - 1, height - 1));
            tileLocations.remove(max + 1);
        }

        public Grid getGrid() {
            return new Grid(data);
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return true;
        }

        @Override
        public int getRowCount() {
            return height;
        }

        @Override
        public int getColumnCount() {
            return width;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (data[col][row] == -1) {
                return "";
            }

            return data[col][row];
        }

        @Override
        public void setValueAt(Object o, int row, int col) {
            if (!(o instanceof Integer || o instanceof String)) {
                return;
            }

            int newVal;
            if (o instanceof String) {
                String str = (String) o;

                if (str.trim().equals("")) {
                    newVal = -1;
                } else {
                    try {
                        newVal = Integer.valueOf(str.trim());
                    } catch (Exception ex) {
                        return;
                    }
                }
            } else {
                newVal = (Integer) o;
            }

            if (newVal < -1 || newVal > max) {
                return;
            }

            if (data[col][row] == newVal) {
                return;
            }

            Point swapFrom = tileLocations.get(newVal);
            tileLocations.put(newVal, tileLocations.get(data[col][row]));
            tileLocations.put(data[col][row], swapFrom);
            data[swapFrom.x][swapFrom.y] = data[col][row];
            data[col][row] = newVal;

            fireTableDataChanged();
            fireTableCellUpdated(col, row);
            fireTableCellUpdated(swapFrom.x, swapFrom.y);
        }
    }
}
