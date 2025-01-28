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

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Command-line argument handling
        Options options = new Options();
        Option inputOption = new Option("i", "input", true, "Maze file path");
        inputOption.setRequired(true);
        options.addOption(inputOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Invalid command-line arguments: {}", e.getMessage());
            new HelpFormatter().printHelp("MazeRunner", options);
            return;
        }

        String mazeFile = cmd.getOptionValue("i");

        logger.info("Starting Maze Runner");
        logger.info("Reading maze file: {}", mazeFile);

        // Read the maze from the file
        char[][] maze = readMaze(mazeFile);
        if (maze == null) {
            logger.error("Failed to load maze.");
            return;
        }

        // Print maze layout for debugging
        displayMaze(maze);

        // Find the entry and exit points
        int[] entryExit = locateEntryExit(maze);
        if (entryExit == null) {
            logger.error("Entry/exit not found in the maze.");
            return;
        }

        logger.info("Entry at: Row {} Column {}", entryExit[0], entryExit[1]);
        logger.info("Exit at: Row {} Column {}", entryExit[2], entryExit[3]);

        logger.info("Computing path...");
        logger.info("Path computation not implemented yet.");

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
     * Prints the maze to the console.
     */
    private static void displayMaze(char[][] maze) {
        logger.info("Maze Layout:");
        for (char[] row : maze) {
            logger.info(new String(row));
        }
    }

    /**
     * Finds the first open space (' ') in the leftmost and rightmost columns
     * to determine the entry and exit points.
     */
    private static int[] locateEntryExit(char[][] maze) {
        int rows = maze.length;
        int cols = maze[0].length;
        int entryRow = -1, exitRow = -1;

        for (int i = 0; i < rows; i++) {
            if (maze[i][0] == ' ') {
                entryRow = i;
            }
            if (maze[i][cols - 1] == ' ') {
                exitRow = i;
            }
            if (entryRow != -1 && exitRow != -1) {
                break;
            }
        }

        return (entryRow != -1 && exitRow != -1) ? new int[]{entryRow, 0, exitRow, cols - 1} : null;
    }
}
