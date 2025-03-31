package ca.mcmaster.se2aa4.mazerunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MazeSolver handles the loading of a maze from a file, 
 * displaying it, and orchestrating the solve via a MazeSolvingStrategy.
 *
 * It also factorizes the resulting path to compress repeated moves.
 */
public class MazeSolver {

    private static final Logger logger = LogManager.getLogger(MazeSolver.class);
    private static final char OPEN = ' ';

    private final String filePath;
    private char[][] maze;
    private int entryRow, entryCol;
    private int exitRow, exitCol;

    // Current Maze-Solving Strategy
    private MazeSolvingStrategy strategy;

    public MazeSolver(String filePath) {
        this.filePath = filePath;
        // By default, we choose the "right-hand" approach from the factory
        this.strategy = MazeSolvingStrategyFactory.getStrategy("right-hand");
    }

    /**
     * A bridging method so older tests referencing solveMazeWithRightHandRule() won't break.
     * Internally delegates to solveMaze().
     *
     * Marked @Deprecated because we prefer solveMaze() in the new design.
     */
    @Deprecated
    public String solveMazeWithRightHandRule() {
        // If you want to force the right-hand strategy specifically:
        // setStrategy(MazeSolvingStrategyFactory.getStrategy("right-hand"));
        // return solveMaze();
        //
        // But by default, it's already right-hand, so:
        return solveMaze();
    }

    /**
     * Another bridging method so older reflection-based tests won't fail.
     * In the new design, "isOpen" is in RightHandRuleStrategy, but we replicate it here
     * for backward compatibility.
     */
    @SuppressWarnings("unused") // used by reflection in old tests
    private boolean isOpen(int row, int col) {
        if (maze == null) return false;
        return row >= 0 && row < maze.length
            && col >= 0 && col < maze[0].length
            && maze[row][col] == OPEN;
    }

    /**
     * Loads the maze from a text file into a 2D char array.
     */
    public boolean loadMaze() {
        List<char[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.toCharArray());
            }
        } catch (IOException e) {
            logger.error("Error reading maze file: {}", e.getMessage());
            return false;
        }

        if (lines.isEmpty()) {
            logger.error("Maze file is empty or unreadable.");
            return false;
        }

        maze = lines.toArray(new char[0][]);
        if (!findEntryAndExit()) {
            logger.error("Maze does not contain a valid entry or exit.");
            return false;
        }
        return true;
    }

    /**
     * Displays the maze in logs for debugging or informational purposes.
     */
    public void displayMaze() {
        if (maze == null) {
            logger.info("No maze loaded; cannot display.");
            return;
        }
        logger.info("Maze Layout:");
        for (char[] row : maze) {
            logger.info(new String(row));
        }
    }

    /**
     * Solves the loaded maze using the current MazeSolvingStrategy, 
     * then factorizes repeated moves (e.g., "F F F" => "3F").
     *
     * @return A factorized path string if the maze is solvable, or null otherwise.
     */
    public String solveMaze() {
        if (maze == null) {
            logger.error("No maze loaded; cannot solve.");
            return null;
        }
        // 1) Get the raw path from the strategy
        String rawPath = strategy.solve(maze, entryRow, entryCol, exitRow, exitCol);
        if (rawPath == null || rawPath.isEmpty()) {
            // No path or infinite loop
            return null;
        }
        // 2) Factorize repeated tokens
        return factorizePath(rawPath);
    }

    /**
     * Allows switching the maze-solving strategy at runtime.
     */
    public void setStrategy(MazeSolvingStrategy newStrategy) {
        this.strategy = newStrategy;
    }

    /**
     * Identifies the leftmost open cell (entry) and rightmost open cell (exit).
     */
    private boolean findEntryAndExit() {
        entryRow = exitRow = -1;
        entryCol = 0;
        if (maze.length == 0 || maze[0].length == 0) return false;

        exitCol = maze[0].length - 1;

        for (int i = 0; i < maze.length; i++) {
            // Left edge
            if (maze[i][0] == OPEN && entryRow == -1) {
                entryRow = i;
            }
            // Right edge
            if (maze[i][exitCol] == OPEN && exitRow == -1) {
                exitRow = i;
            }
        }
        return (entryRow != -1 && exitRow != -1);
    }

    /**
     * Compresses consecutive repeats in a raw path string:
     *  e.g., "F F F R R F" => "3F 2R F"
     */
    private String factorizePath(String path) {
        if (path == null || path.isEmpty()) return path;

        String[] moves = path.split(" ");
        StringBuilder factorized = new StringBuilder();
        String prevMove = moves[0];
        int count = 1;

        for (int i = 1; i < moves.length; i++) {
            if (moves[i].equals(prevMove)) {
                count++;
            } else {
                factorized.append(count > 1 ? count + prevMove : prevMove).append(" ");
                prevMove = moves[i];
                count = 1;
            }
        }
        // Append final group
        factorized.append(count > 1 ? count + prevMove : prevMove);

        return factorized.toString();
    }
}
