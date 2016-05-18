package io.github.magisun.graphmaster.gui;

import edu.uci.ics.jung.graph.Graph;
import io.github.magisun.graphmaster.graph.Grid;
import io.github.magisun.graphmaster.graph.MoveType;
import io.github.magisun.graphmaster.graph.Transition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static javax.swing.SpringLayout.*;

public class AddNodeDialog extends JDialog {

    private static final int DISPLAY_SIZE = 100;
    private static final int SPACING = 10;

    private Grid parent;
    private Graph<Grid, Transition> graph;

    private JPanel contentPane;
    private JPanel selectionPane;
    private JButton buttonCancel;

    public AddNodeDialog(Grid parent, Graph<Grid, Transition> graph) {
        super();
        setModal(true);
        setTitle("Add node");

        this.parent = parent;
        this.graph = graph;

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
        SpringLayout layout = new SpringLayout();
        selectionPane.setLayout(layout);

        Component last = selectionPane;
        int labelHeight = 0;
        for(Map.Entry<Integer, MoveType> entry : possibleMoves.entrySet()) {
            Grid option = new Grid(parent, entry.getValue());
            if(graph.containsVertex(option)) {
                option.setLensColor(new Color(255, 0, 0, 150));
            }
            OptionPanel panel = new OptionPanel(option);
            panel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if(!graph.containsVertex(option)) {
                        graph.addVertex(option);
                        graph.addEdge(new Transition(parent, option), parent, option);
                        MainWindow.getWindow().recalculateLayout();
                        onCancel();
                    }
                }
            });

            selectionPane.add(panel);
            if(last == selectionPane)
                layout.putConstraint(WEST, panel, 0, WEST, last);
            else
                layout.putConstraint(WEST, panel, SPACING, EAST, last);

            JLabel scoreLabel = new JLabel("Score: " + option.getScore());
            selectionPane.add(scoreLabel);
            if(scoreLabel.getPreferredSize().height > labelHeight) {
                labelHeight = scoreLabel.getPreferredSize().height;
            }

            layout.putConstraint(WEST, scoreLabel, 0, WEST, panel);
            layout.putConstraint(NORTH, scoreLabel, 5, SOUTH, panel);

            last = panel;
        }

        selectionPane.setSize(DISPLAY_SIZE*possibleMoves.size() + SPACING*(possibleMoves.size()-1),
                DISPLAY_SIZE + 5 + labelHeight);
        selectionPane.setPreferredSize(selectionPane.getSize());
    }

    private class OptionPanel extends JButton {

        private Grid option;
        private BufferedImage buffer;

        public OptionPanel(Grid option) {
            this.option = option;

            setSize(100, 100);
            setPreferredSize(getSize());

            makeBuffer();
        }

        private void makeBuffer() {
            buffer = new BufferedImage(DISPLAY_SIZE, DISPLAY_SIZE, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = buffer.createGraphics();

            ImageIcon icon = (ImageIcon) option.getIcon();
            BufferedImage image = (BufferedImage) icon.getImage();

            Point emptyPos = option.getSlotImageIndex(option.getEmptyX(), option.getEmptyY());
            emptyPos.translate(Grid.SPACE_SIZE/2, Grid.SPACE_SIZE/2);
            Point bufferTopLeft = new Point(emptyPos.x-DISPLAY_SIZE/2, emptyPos.y-DISPLAY_SIZE/2);
            Point bufferBottomRight = new Point(emptyPos.x+DISPLAY_SIZE/2, emptyPos.y+DISPLAY_SIZE/2);
            Rectangle imageBounds = new Rectangle(DISPLAY_SIZE, DISPLAY_SIZE);

            if(bufferTopLeft.x < 0) {
                imageBounds.x -= bufferTopLeft.x;
                bufferTopLeft.x = 0;
            }
            if(bufferTopLeft.y < 0) {
                imageBounds.y -= bufferTopLeft.y;
                bufferTopLeft.y = 0;
            }
            if(bufferBottomRight.y >= image.getHeight()) {
                int dy = bufferBottomRight.y - image.getHeight();
                imageBounds.height -= dy;
                bufferBottomRight.y -= dy;
            }
            if(bufferBottomRight.x >= image.getWidth()) {
                int dx = bufferBottomRight.x - image.getWidth();
                imageBounds.width -= dx;
                bufferBottomRight.x -= dx;
            }

            BufferedImage subImage = image.getSubimage(bufferTopLeft.x, bufferTopLeft.y,
                    bufferBottomRight.x - bufferTopLeft.x, bufferBottomRight.y - bufferTopLeft.y);
            g2d.drawImage(subImage, imageBounds.x, imageBounds.y, this);

            g2d.dispose();
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.drawImage(buffer, 0, 0, this);

            g2d.dispose();
        }
    }
}
