package ca.mcmaster.se2aa4.mazerunner;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class for the Maze Runner application.
 * 
 * - Parses command-line arguments using Apache Commons CLI.
 * - Creates a MazeSolver to load and solve a maze file.
 * - Prints the resulting factorized path or an error message.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        CommandLine cmd = parseCommandLineArgs(args);
        if (cmd == null) {
            return; // invalid arguments or help displayed
        }

        String mazeFile = cmd.getOptionValue("i");
        MazeSolver solver = new MazeSolver(mazeFile);

        // Load the maze
        if (!solver.loadMaze()) {
            System.err.println("Failed to load maze from file: " + mazeFile);
            return;
        }

        // Display the maze layout
        solver.displayMaze();

        // Solve using the default (right-hand) strategy
        String factorizedPath = solver.solveMaze();
        if (factorizedPath == null) {
            System.err.println("Failed to solve maze: No valid path found or infinite loop detected.");
        } else {
            System.out.println("Factorized Path: " + factorizedPath);
        }
    }

    /**
     * Parses command-line arguments: requires -i <mazeFilePath>.
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
