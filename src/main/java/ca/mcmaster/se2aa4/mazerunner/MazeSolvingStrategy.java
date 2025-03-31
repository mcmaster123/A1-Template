package ca.mcmaster.se2aa4.mazerunner;

/**
 * Strategy interface for various maze-solving algorithms.
 * 
 * Each strategy must implement a method to solve the maze, returning a
 * raw sequence of moves (e.g., "F F R") or null if no solution.
 */
public interface MazeSolvingStrategy {

    /**
     * Attempts to solve the maze and returns a raw path (e.g., "F F R L F").
     *
     * @param maze      The 2D character array representing the maze layout.
     * @param entryRow  The row index of the left-edge entry point.
     * @param entryCol  The column index of the left-edge entry point.
     * @param exitRow   The row index of the right-edge exit point.
     * @param exitCol   The column index of the right-edge exit point.
     * @return A raw path string if solved, or null if unsolvable or if an
     *         infinite loop is detected.
     */
    String solve(char[][] maze, int entryRow, int entryCol, int exitRow, int exitCol);
}
