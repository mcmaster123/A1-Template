package ca.mcmaster.se2aa4.mazerunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Apache Commons CLI imports
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        // Create an Options object for CLI parsing
        Options options = new Options();

        // Define the -i option (required) for the maze file
        Option inputOption = new Option("i", "input", true, "Path to the maze file");
        inputOption.setRequired(true);
        options.addOption(inputOption);

        // Prepare parser and help formatter
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        // Parse command line
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Error parsing command-line arguments: {}", e.getMessage());
            formatter.printHelp("MazeRunner", options);
            return; // Exit the program
        }

        // Retrieve the maze file
        String mazeFile = cmd.getOptionValue("i");

        // Log the start of the program
        logger.info("** Starting Maze Runner **");
        
        // Log which file we are reading
        logger.info("**** Reading the maze from file: {}", mazeFile);

        // Read the file
        try (BufferedReader reader = new BufferedReader(new FileReader(mazeFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Build output for the line
                StringBuilder sb = new StringBuilder();
                for (int idx = 0; idx < line.length(); idx++) {
                    char c = line.charAt(idx);
                    if (c == '#') {
                        sb.append("WALL ");
                    } else if (c == ' ') {
                        sb.append("PASS ");
                    }
                }
                // Log the processed line
                logger.info(sb.toString());
            }
        } catch (IOException e) {
            logger.error("/!\\ An error has occurred while reading the file: {}", e.getMessage());
        }

        // Compute path (placeholder)
        logger.info("**** Computing path");
        logger.info("PATH NOT COMPUTED");

        // End of MazeRunner
        logger.info("** End of MazeRunner **");
    }
}
