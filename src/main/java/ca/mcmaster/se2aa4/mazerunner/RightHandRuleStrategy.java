package ca.mcmaster.se2aa4.mazerunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Concrete Strategy implementing the classic "Right-Hand Rule" for maze solving.
 * 
 * The solver keeps its right hand on a wall as it navigates the maze.
 */
public class RightHandRuleStrategy implements MazeSolvingStrategy {

    private static final Logger logger = LogManager.getLogger(RightHandRuleStrategy.class);

    // We consider ' ' (space) as open, '#' as wall
    private static final char OPEN = ' ';

    @Override
    public String solve(char[][] maze, int entryRow, int entryCol, int exitRow, int exitCol) {
        if (maze == null) {
            logger.error("Maze is null; cannot solve with Right-Hand Rule.");
            return null;
        }

        // Directions in order: Right, Down, Left, Up (clockwise)
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        int dir = 0; // start facing "right"
        int row = entryRow, col = entryCol;

        StringBuilder path = new StringBuilder();
        boolean[][][] visited = new boolean[maze.length][maze[0].length][4];

        // Step limit to avoid infinite loops: 2 * (area of the maze)
        int stepLimit = maze.length * maze[0].length * 2;
        int steps = 0;

        while (row != exitRow || col != exitCol) {
            if (steps++ > stepLimit) {
                logger.error("Infinite loop detected during maze solving (RightHandRule).");
                return null;
            }

            visited[row][col][dir] = true;

            // Attempt turning right
            int rightDir = (dir + 1) % 4;
            int rRow = row + directions[rightDir][0];
            int rCol = col + directions[rightDir][1];

            if (isOpen(maze, rRow, rCol) && !visited[rRow][rCol][rightDir]) {
                dir = rightDir;
                row = rRow;
                col = rCol;
                path.append("R F ");
                continue;
            }

            // Attempt moving forward
            int fRow = row + directions[dir][0];
            int fCol = col + directions[dir][1];
            if (isOpen(maze, fRow, fCol) && !visited[fRow][fCol][dir]) {
                row = fRow;
                col = fCol;
                path.append("F ");
                continue;
            }

            // If forward is blocked, turn left
            dir = (dir + 3) % 4;
            path.append("L ");
        }

        // Return the raw path string (un-factorized)
        return path.toString().trim();
    }

    /**
     * Checks if the specified cell is open and within maze bounds.
     */
    private boolean isOpen(char[][] maze, int row, int col) {
        return row >= 0 && row < maze.length
            && col >= 0 && col < maze[0].length
            && maze[row][col] == OPEN;
    }
}
