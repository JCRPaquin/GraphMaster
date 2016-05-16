package io.github.magisun.graphmaster.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tile grid puzzle.
 */
public class Grid {

    /**
     * Space size in pixels
     */
    public static final int SPACE_SIZE = 20;

    private Dimension gridSize;
    private int emptyX, emptyY;
    private Icon icon;

    private int[][] grid;

    private Grid parent;
    private MoveType executedMove;

    private int cachedHash;

    /**
     * Constructs a 1x1 grid with one empty space.
     */
    public Grid() {
        gridSize = new Dimension(1, 1);
        grid = new int[1][1];
        emptyX = 0;
        emptyY = 0;
        grid[0][0] = -1;

        executedMove = MoveType.NONE;

        calculateHash();
    }

    /**
     * Constructs a grid with an empty space at
     * the lower left-hand corner.
     *
     * @param width width of the puzzle
     * @param height height of the puzzle
     */
    public Grid(int width, int height) {
        gridSize = new Dimension(width, height);
        grid = new int[width][height];
        emptyX = width - 1;
        emptyY = height - 1;

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                grid[x][y] = y*width + x + 1;
            }
        }
        grid[emptyX][emptyY] = -1;

        executedMove = MoveType.NONE;

        calculateHash();
    }

    /**
     * Copy constructor.
     *
     * @param other the grid to copy
     */
    public Grid(Grid other) {
        emptyX = other.emptyX;
        emptyY = other.emptyY;

        gridSize = other.gridSize;
        parent = other;
        executedMove = MoveType.NONE;

        cachedHash = other.cachedHash;
    }

    /**
     * Constructs a derivative grid given a particular move direction.
     *
     * @param other the grid to copy initially
     * @param move the move to play out on the copied grid
     */
    public Grid(Grid other, MoveType move) {
        this(other);

        executedMove = move;

        followMove(move);
    }

    /**
     * Constructs a grid from a given data set.
     *
     * @param data the grid's data set
     */
    public Grid(int[][] data) {
        gridSize = new Dimension(data.length, data[0].length);

        grid = new int[gridSize.width][gridSize.height];
        for(int x = 0; x < gridSize.width; x++) {
            for(int y = 0; y < gridSize.height; y++) {
                if(data[x][y] == -1) {
                    emptyX = x;
                    emptyY = y;
                }
                grid[x][y] = data[x][y];
            }
        }

        executedMove = MoveType.NONE;

        calculateHash();
    }

    /**
     * Lazily retrieves the board icon.
     *
     * @return the board icon
     */
    public Icon getIcon() {
        if(icon == null) {
            makeIcon();
        }

        return icon;
    }

    private Point getSlotImageIndex(int x, int y) {
        return new Point(x*SPACE_SIZE+x+1, y*SPACE_SIZE+y+1);
    }

    private void makeIcon() {
        int width = gridSize.width*(SPACE_SIZE+1) + 1;
        int height = gridSize.height*(SPACE_SIZE+1) + 1;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        if(grid != null) {
            makeBaseIcon(image);
        } else {
            makeDerivedIcon(image);
        }

        icon = new ImageIcon(image);
    }

    private void makeDerivedIcon(BufferedImage image) {
        ImageIcon oldIcon = (ImageIcon) parent.getIcon();
        BufferedImage oldImage = (BufferedImage) oldIcon.getImage();

        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(oldImage, null, null);

        Point oldEmptySlot = getSlotImageIndex(parent.emptyX, parent.emptyY);
        Point newEmptySlot = getSlotImageIndex(emptyX, emptyY);

        BufferedImage subImage = oldImage.getSubimage(newEmptySlot.x, newEmptySlot.y,
                SPACE_SIZE, SPACE_SIZE);
        g2d.drawImage(subImage, oldEmptySlot.x, oldEmptySlot.y, null);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(newEmptySlot.x, newEmptySlot.y,
                SPACE_SIZE, SPACE_SIZE);
    }

    // Renders the board icon
    private void makeBaseIcon(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();

        g2d.setBackground(Color.WHITE);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth()-1, image.getHeight()-1);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, image.getWidth()-1, image.getHeight()-1);

        // Vertical lines
        for(int i = 1; i < gridSize.width; i++) {
            g2d.drawLine(i*SPACE_SIZE+i, 0, i*SPACE_SIZE+i, image.getHeight()-1);
        }

        // Horizontal lines
        for(int i = 1; i < gridSize.height; i++) {
            g2d.drawLine(0, i*SPACE_SIZE+i, image.getWidth()-1, i*SPACE_SIZE+i);
        }

        // Numbers
        for(int x = 0; x < gridSize.width; x++) {
            for(int y = 0; y < gridSize.height; y++) {
                if(grid[x][y] < 0) {
                    continue;
                }

                String toDraw = Integer.toString(grid[x][y]);
                Point gridCorner = getSlotImageIndex(x, y);

                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D bounds = fm.getStringBounds(toDraw, g2d);
                g2d.drawString(toDraw,
                        gridCorner.x + (int)((SPACE_SIZE - bounds.getWidth())/2),
                        gridCorner.y + (int)((SPACE_SIZE - bounds.getHeight())/2)
                                + fm.getAscent());
            }
        }
    }

    /**
     * @return the x position of the empty space
     */
    public int getEmptyX() {
        return emptyX;
    }

    /**
     * @return the y position of the empty space
     */
    public int getEmptyY() {
        return emptyY;
    }

    /**
     * @return the width of the grid
     */
    public int getWidth() {
        return grid.length;
    }

    /**
     * @return the height of the grid
     */
    public int getHeight() {
        return grid[0].length;
    }

    /**
     * @return whether or not this grid is derived from another grid
     */
    public boolean isDerived() {
        return parent != null;
    }

    /**
     * @return the parent grid of this grid
     */
    public Grid getParent() {
        return parent;
    }

    /**
     * @return the move executed on the parent grid to get to this grid
     */
    public MoveType getExecutedMove() {
        return executedMove;
    }

    private void followMove(MoveType move) {
        switch (move) {
            case UP:
                moveTo(emptyX, emptyY-1);
                break;
            case DOWN:
                moveTo(emptyX, emptyY+1);
                break;
            case LEFT:
                moveTo(emptyX-1, emptyY);
                break;
            case RIGHT:
                moveTo(emptyX+1, emptyY);
        }
    }

    /**
     * Relocates the empty space in the puzzle to (x,y) if (x,y) is a valid
     * position.
     *
     * @param x a valid x position
     * @param y a valid y position
     * @throws IllegalArgumentException if (x,y) is not on the board
     * @return true on successful move, false otherwise
     */
    private boolean moveTo(int x, int y) {
        if(x < 0 || x >= gridSize.width) {
            throw new IllegalArgumentException("x index out of bounds.");
        } else if(y < 0 || y >= gridSize.height) {
            throw new IllegalArgumentException("y index out of bounds.");
        }

        if(Math.abs(emptyX-x) + Math.abs(emptyY-y) != 1) {
            return false;
        }

        if(grid != null) {
            grid[emptyX][emptyY] = grid[x][y];
            grid[x][y] = -1;
        }
        emptyX = x;
        emptyY = y;

        calculateHash();

        return true;
    }

    // Calculate the hash for this grid
    private void calculateHash() {
        int result = 0;
        if(grid != null) {
            result += Arrays.deepHashCode(grid);
        } else {
            result += parent.hashCode();
        }
        result = 31 * result + emptyX;
        result = 31 * result + emptyY;
        cachedHash = result;
    }

    private int[][] getFullGrid() {
        if(grid != null) {
            return grid;
        } else if(parent.grid != null && executedMove == MoveType.NONE) {
            grid = parent.grid;
            return grid;
        }

        int[][] base = parent.getFullGrid();
        int[][] copy = new int[gridSize.width][gridSize.height];

        for(int x = 0; x < gridSize.width; x++) {
            for(int y = 0; y < gridSize.height; y++) {
                copy[x][y] = base[x][y];
            }
        }

        copy[parent.emptyX][parent.emptyY] = base[emptyX][emptyY];
        copy[emptyX][emptyY] = -1;

        grid = copy;
        return grid;
    }

    private boolean isValid(int x, int y) {
        return (x >= 0 && x < gridSize.width) &&
                (y >= 0 && y < gridSize.height);
    }

    private MoveType offsetsToMoveType(int xOffset, int yOffset) {
        if(xOffset == 1) {
            return MoveType.RIGHT;
        } else if(xOffset == -1) {
            return MoveType.LEFT;
        }

        if(yOffset == 1) {
            return MoveType.DOWN;
        } else if(yOffset == -1) {
            return MoveType.UP;
        }

        return MoveType.NONE;
    }

    public Map<Integer, MoveType> getPotentialMoves() {
        int[][] grid = getFullGrid();
        HashMap<Integer, MoveType> potentialMoves = new HashMap<>();

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if(x == 0 && y == 0) continue;
                if(x != 0 && y != 0) continue;
                if(!isValid(emptyX+x, emptyY+y)) continue;

                potentialMoves.put(grid[emptyX+x][emptyY+y],
                        offsetsToMoveType(x, y));
            }
        }

        return potentialMoves;
    }

    private Point getOriginalSlot(int n) {
        return new Point((n-1)%gridSize.width,
                (n-1)/gridSize.width);
    }

    /**
     * Calculates the score of this Grid by accumulating the
     * taxicab distances of all slots from their original position.
     *
     * @return the score of this grid
     */
    public int getScore() {
        int score = 0;
        getFullGrid();

        for(int x = 0; x < gridSize.width; x++) {
            for(int y = 0; y < gridSize.height; y++) {
                if(x == emptyX && y == emptyY) continue;

                Point original = getOriginalSlot(grid[x][y]);
                score += Math.abs(x-original.x) + Math.abs(y-original.y);
            }
        }

        return score;
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    /**
     * Performs a full grid check to determine Grid equality.
     *
     * @param other the second grid to compare with
     * @return whether the two grids are equal to each other
     */
    public boolean isSimilar(Grid other) {
        if(!other.gridSize.equals(gridSize)) {
            return false;
        } else if(other.emptyX != emptyX || other.emptyY != emptyY) {
            return false;
        }

        other.getFullGrid();
        getFullGrid();

        for(int x = 0; x < gridSize.width; x++) {
            for(int y = 0; y < gridSize.height; y++) {
                if(other.grid[x][y] != grid[x][y]) {
                    return false;
                }
            }
        }

        return true;
    }

    void writeToStream(ObjectOutputStream stream)
            throws IOException {
        for(int x = 0; x < gridSize.width; x++) {
            for(int y = 0; y < gridSize.height; y++) {
                stream.writeInt(grid[x][y]);
            }
        }
    }
}
