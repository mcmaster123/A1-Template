package ca.mcmaster.se2aa4.mazerunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Command-line option setup
        CommandLine cmd = parseCommandLineArgs(args);
        if (cmd == null) return;

        String mazeFile = cmd.getOptionValue("i");

        // Load maze and initialize the solver
        MazeSolver solver = new MazeSolver(mazeFile);
        if (!solver.loadMaze()) {
            System.err.println("Failed to load maze from file: " + mazeFile);
            return;
        }

        // Display maze
        solver.displayMaze();

        // Solve the maze
        String factorizedPath = solver.solveMazeWithRightHandRule();
        if (factorizedPath == null) {
            System.err.println("Failed to solve maze: Entry or exit not found.");
        } else {
            System.out.println("Factorized Path: " + factorizedPath);
        }
    }

    /**
     * Parses the command-line arguments using Apache Commons CLI.
     */
    private static CommandLine parseCommandLineArgs(String[] args) {
        Options options = new Options();
        Option inputOption = Option.builder("i")
            .longOpt("input")
            .hasArg()
            .desc("Maze file path")
            .required(true)
            .build();
        options.addOption(inputOption);

        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.err.println("Invalid command-line arguments: " + e.getMessage());
            new HelpFormatter().printHelp("MazeRunner", options);
            return null;
        }
    }
}

/**
 * Encapsulates all logic for loading, displaying, and solving a maze.
 */
class MazeSolver {

    private static final Logger logger = LogManager.getLogger(MazeSolver.class);
    private static final char WALL = '#';
    private static final char OPEN = ' ';

    private final String filePath;
    private char[][] maze;
    private int entryRow, entryCol, exitRow, exitCol;

    public MazeSolver(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads the maze from the file into a 2D character array.
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

        maze = lines.toArray(new char[0][]);
        if (!findEntryAndExit()) {
            logger.error("Maze does not contain a valid entry or exit.");
            return false;
        }
        return true;
    }

    /**
     * Displays the maze layout.
     */
    public void displayMaze() {
        logger.info("Maze Layout:");
        for (char[] row : maze) {
            logger.info(new String(row));
        }
    }

    /**
     * Solves the maze using the Right-Hand Rule and returns the factorized path.
     */
    public String solveMazeWithRightHandRule() {
        if (maze == null) return null;

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
        int dir = 0; // Start facing right
        int row = entryRow, col = entryCol;

        StringBuilder path = new StringBuilder();
        boolean[][][] visited = new boolean[maze.length][maze[0].length][4];
        int stepLimit = maze.length * maze[0].length * 2;
        int steps = 0;

        while (row != exitRow || col != exitCol) {
            if (steps++ > stepLimit) {
                logger.error("Infinite loop detected during maze solving.");
                return null;
            }

            visited[row][col][dir] = true;

            // Try turning right first
            int rightDir = (dir + 1) % 4;
            int rRow = row + directions[rightDir][0];
            int rCol = col + directions[rightDir][1];

            if (isOpen(rRow, rCol) && !visited[rRow][rCol][rightDir]) {
                dir = rightDir;
                row = rRow;
                col = rCol;
                path.append("R F ");
                continue;
            }

            // Try moving forward
            int fRow = row + directions[dir][0];
            int fCol = col + directions[dir][1];

            if (isOpen(fRow, fCol) && !visited[fRow][fCol][dir]) {
                row = fRow;
                col = fCol;
                path.append("F ");
                continue;
            }

            // Turn left if forward is blocked
            dir = (dir + 3) % 4;
            path.append("L ");
        }

        return factorizePath(path.toString().trim());
    }

    /**
     * Factorizes the path by compressing repeated moves.
     */
    private String factorizePath(String path) {
        if (path.isEmpty()) return path;

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

        factorized.append(count > 1 ? count + prevMove : prevMove);
        return factorized.toString();
    }

    /**
     * Identifies the entry (leftmost open space) and exit (rightmost open space).
     */
    private boolean findEntryAndExit() {
        entryRow = exitRow = -1;
        entryCol = 0;
        exitCol = maze[0].length - 1;

        for (int i = 0; i < maze.length; i++) {
            if (maze[i][0] == OPEN && entryRow == -1) entryRow = i;
            if (maze[i][exitCol] == OPEN && exitRow == -1) exitRow = i;
        }

        return entryRow != -1 && exitRow != -1;
    }

    /**
     * Checks if a cell is open and within bounds.
     */
    private boolean isOpen(int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col] == OPEN;
    }
}
