package io.github.magisun.graphmaster.gui;

import com.sun.istack.internal.NotNull;
import edu.uci.ics.jung.graph.Graph;
import io.github.magisun.graphmaster.graph.Grid;
import io.github.magisun.graphmaster.graph.MoveType;
import io.github.magisun.graphmaster.graph.Transition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

import static javax.swing.SpringLayout.*;

public class AddNodeDialog extends JDialog {

    private static final int DISPLAY_SIZE = 100;
    private static final int SPACING = 10;

    private JPanel contentPane;
    private JPanel selectionPane;
    private JButton buttonCancel;

    public AddNodeDialog(Grid parent, Graph<Grid, Transition> graph) {
        super();
        setModal(true);

        contentPane = new JPanel();
        setContentPane(contentPane);

        SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);

        Map<Integer, MoveType> moves = parent.getPotentialMoves();

        selectionPane = new JPanel();
        buildSelectionPane(moves);
        contentPane.add(selectionPane);
        layout.putConstraint(WEST, selectionPane, 15, WEST, contentPane);
        layout.putConstraint(NORTH, selectionPane, 15, NORTH, contentPane);
        layout.putConstraint(EAST, selectionPane, DISPLAY_SIZE*moves.size() + SPACING*(moves.size()-1), WEST, selectionPane);
        layout.putConstraint(SOUTH, selectionPane, DISPLAY_SIZE, NORTH, selectionPane);
        layout.putConstraint(EAST, contentPane, 15, EAST, selectionPane);

        buttonCancel = new JButton("Cancel");
        buildCancelButton();
        contentPane.add(buttonCancel);
        layout.putConstraint(NORTH, buttonCancel, 15, SOUTH, selectionPane);
        layout.putConstraint(SOUTH, buttonCancel, 25, NORTH, buttonCancel);
        layout.putConstraint(SOUTH, contentPane, 15, SOUTH, buttonCancel);
        layout.putConstraint(EAST, buttonCancel, 0, EAST, selectionPane);
        layout.putConstraint(WEST, buttonCancel, -100, EAST, buttonCancel);

        pack();
    }

    private void onCancel() {
        dispose();
    }

    private void buildCancelButton() {
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   onCancel();
                                               }
                                           },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void buildSelectionPane(Map<Integer, MoveType> possibleMoves) {

    }

    private class OptionPanel extends JButton {

        private Grid option;

        public OptionPanel(@NotNull Grid option) {
            this.option = option;
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();



            g2d.dispose();
        }
    }
}
