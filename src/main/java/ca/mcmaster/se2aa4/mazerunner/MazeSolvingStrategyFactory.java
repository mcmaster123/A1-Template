package ca.mcmaster.se2aa4.mazerunner;

/**
 * A Factory class that instantiates MazeSolvingStrategy objects.
 * 
 * Extend or modify this class to handle additional strategies
 * (e.g., "left-hand rule," BFS/DFS, etc.).
 */
public class MazeSolvingStrategyFactory {

    /**
     * Returns an instance of MazeSolvingStrategy based on the given name.
     *
     * @param strategyName A string indicating which strategy to instantiate.
     * @return A MazeSolvingStrategy instance, defaulting to RightHandRule if
     *         the name is unrecognized.
     */
    public static MazeSolvingStrategy getStrategy(String strategyName) {
        if ("right-hand".equalsIgnoreCase(strategyName)) {
            return new RightHandRuleStrategy();
        }
        // If more strategies exist, check them here...

        // Fallback to the right-hand rule if none match
        return new RightHandRuleStrategy();
    }
}
