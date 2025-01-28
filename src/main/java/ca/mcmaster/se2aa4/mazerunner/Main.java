package ca.mcmaster.se2aa4.mazerunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final char WALL = '#';
    private static final char OPEN = ' ';

    public static void main(String[] args) {
        // Set up command-line options
        Options options = new Options();
        Option inputOption = Option.builder("i")
            .longOpt("input")
            .hasArg()
            .desc("Maze file path")
            .required(true)
            .build();
        options.addOption(inputOption);

        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            logger.error("Invalid command-line arguments: {}", e.getMessage());
            new HelpFormatter().printHelp("MazeRunner", options);
            return;
        }

        String mazeFile = cmd.getOptionValue("i");

        logger.info("Starting Maze Runner");
        logger.info("Reading maze file: {}", mazeFile);

        char[][] maze = readMaze(mazeFile);
        if (maze == null) {
            logger.error("Failed to load maze.");
            return;
        }

        displayMaze(maze);

        int[] entryExit = locateEntryExit(maze);
        if (entryExit == null) {
            logger.error("Entry/exit not found in the maze.");
            return;
        }

        logger.info("Entry at: Row {} Column {}", entryExit[0], entryExit[1]);
        logger.info("Exit at: Row {} Column {}", entryExit[2], entryExit[3]);

        String path = findPath(maze, entryExit[0], entryExit[1], entryExit[2], entryExit[3]);

        logger.info("Factorized Path: {}", factorizePath(path));

        logger.info("Maze Runner completed.");
    }

    /**
     * Reads the maze from a file and returns it as a 2D character array.
     */
    private static char[][] readMaze(String filename) {
        List<char[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.toCharArray());
            }
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage());
            return null;
        }
        return lines.toArray(new char[0][]);
    }

    /**
     * Displays the maze layout for debugging.
     */
    private static void displayMaze(char[][] maze) {
        logger.info("Maze Layout:");
        for (char[] row : maze) {
            logger.info(new String(row));
        }
    }

    /**
     * Identifies the entry and exit points in the maze.
     * The entry is the first open space (' ') in the leftmost column.
     * The exit is the first open space (' ') in the rightmost column.
     */
    private static int[] locateEntryExit(char[][] maze) {
        int rows = maze.length;
        int cols = maze[0].length;
        int entryRow = -1, exitRow = -1;

        for (int i = 0; i < rows; i++) {
            if (maze[i][0] == OPEN) entryRow = i;
            if (maze[i][cols - 1] == OPEN) exitRow = i;
            if (entryRow != -1 && exitRow != -1) break;
        }

        return (entryRow != -1 && exitRow != -1) ? new int[]{entryRow, 0, exitRow, cols - 1} : null;
    }

    /**
     * Implements the Right-Hand Rule to find a path through the maze.
     */
    private static String findPath(char[][] maze, int startRow, int startCol, int exitRow, int exitCol) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
        int dir = 0; // Start facing right
        int row = startRow, col = startCol;

        StringBuilder path = new StringBuilder();
        Set<String> visited = new HashSet<>();

        int stepLimit = maze.length * maze[0].length * 2; // Prevents infinite looping (Failsafe)
        int stepCount = 0;

        while (row != exitRow || col != exitCol) {
            if (stepCount++ > stepLimit) {
                logger.error("ERROR: Infinite loop detected! Terminating pathfinding.");
                return "ERROR: Infinite loop detected";
            }

            String pos = row + "," + col + "," + dir;
            if (visited.contains(pos)) {
                logger.warn("Loop detected at ({}, {}) facing direction {}", row, col, dir);
            }
            visited.add(pos);

            // Try turning right first
            int rightDir = (dir + 1) % 4;
            int rRow = row + directions[rightDir][0];
            int rCol = col + directions[rightDir][1];

            if (isOpen(maze, rRow, rCol)) {
                dir = rightDir;
                row = rRow;
                col = rCol;
                path.append("R F ");
                continue;
            }

            // If right is blocked, try moving forward
            int fRow = row + directions[dir][0];
            int fCol = col + directions[dir][1];

            if (isOpen(maze, fRow, fCol)) {
                row = fRow;
                col = fCol;
                path.append("F ");
                continue;
            }

            // If forward is blocked, turn left
            dir = (dir + 3) % 4;
            path.append("L ");
        }

        return path.toString().trim();
    }

    /**
     * Checks if a cell in the maze is open and within bounds.
     */
    private static boolean isOpen(char[][] maze, int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col] == OPEN;
    }

    /**
     * Factorizes the path by compressing repeated moves.
     * Example: "F F F L L R R" → "3F 2L 2R"
     */
    private static String factorizePath(String path) {
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
}
