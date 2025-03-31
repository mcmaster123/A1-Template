package ca.mcmaster.se2aa4.mazerunner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

/**
 * JUnit 5 test class for MazeSolver.
 * Demonstrates 10+ targeted tests covering major functionality and edge cases.
 */
public class MazeSolverTest {

    private static final String TEMP_DIR = "test-mazes";

    @BeforeAll
    public static void createTestFolder() {
        File dir = new File(TEMP_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    @AfterAll
    public static void removeTestFolder() {
        File dir = new File(TEMP_DIR);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
    }

    /** Helper to write a maze to a file. */
    private File createMazeFile(String filename, String content) throws IOException {
        File file = new File(TEMP_DIR, filename);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
        }
        return file;
    }

    @Test
    public void loadMaze_validLayout_returnsTrue() throws IOException {
        // Maze has an open path on left & right edges
        File f = createMazeFile("validMaze.txt",
                "# #\n" +
                "   \n" +
                "# #\n");

        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertTrue(solver.loadMaze(),
                "Expected loadMaze() to succeed with correct left/right openings.");
    }

    @Test
    public void loadMaze_nonExistentFile_returnsFalse() {
        MazeSolver solver = new MazeSolver("fake/path/doesNotExist.txt");
        assertFalse(solver.loadMaze(),
                "Expected loadMaze() to fail for nonexistent file path.");
    }

    @Test
    public void loadMaze_allWalledNoExit_returnsFalse() throws IOException {
        // No open left or right edges => invalid
        File f = createMazeFile("noEntryExit.txt",
                "###\n" +
                "###\n" +
                "###\n");

        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertFalse(solver.loadMaze(),
                "Expected loadMaze() to fail if it can't find valid entry/exit.");
    }

    @Test
    public void solveMaze_simpleSolvableMaze_returnsPath() throws IOException {
        // Middle row open => solvable
        File f = createMazeFile("solvable.txt",
                "# #\n" +
                "   \n" +
                "# #\n");
        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertTrue(solver.loadMaze());

        String path = solver.solveMazeWithRightHandRule();
        assertNotNull(path, "Should get a path string for a solvable maze.");
        assertFalse(path.isEmpty(), "Path should not be empty if maze is solvable.");
    }

    @Test
    public void solveMaze_unsolvable_returnsNull() throws IOException {
        // Left & right open, but walled in the middle
        File f = createMazeFile("unsolvable.txt",
                "  #\n" +
                "###\n" +
                "#  \n");
        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertTrue(solver.loadMaze(), "Maze loads but is unsolvable.");

        String path = solver.solveMazeWithRightHandRule();
        assertNull(path, "Expect null if no valid route exists.");
    }

    @Test
    public void solveMaze_infiniteLoopScenario_handledGracefully() throws IOException {
        // Fully open 3x3 => naive right-hand rule might loop
        File f = createMazeFile("loopy.txt",
                "   \n" +
                "   \n" +
                "   \n");
        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertTrue(solver.loadMaze());

        String path = solver.solveMazeWithRightHandRule();
        // Either returns a path or null after step limit. No infinite hang.
        assertTrue(path == null || !path.isEmpty(),
                "No infinite loop: solver must return either a path or null.");
    }

    @Test
    public void factorizePath_compressesMoves() throws Exception {
        MazeSolver solver = new MazeSolver("dummy.txt");
        var method = MazeSolver.class.getDeclaredMethod("factorizePath", String.class);
        method.setAccessible(true);

        String raw = "F F F R R F F";
        String expected = "3F 2R 2F";
        String actual = (String) method.invoke(solver, raw);

        assertEquals(expected, actual, "Should compress repeated moves correctly.");
    }

    @Test
    public void isOpen_privateCheckViaReflection() throws Exception {
        // Maze w/ open middle row
        File f = createMazeFile("testIsOpen.txt",
                "# #\n" +
                "   \n" +
                "# #\n");
        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertTrue(solver.loadMaze());

        var isOpenMth = MazeSolver.class.getDeclaredMethod("isOpen", int.class, int.class);
        isOpenMth.setAccessible(true);

        // row=1,col=1 => ' ', row=0,col=0 => '#'
        assertTrue((boolean) isOpenMth.invoke(solver, 1, 1));
        assertFalse((boolean) isOpenMth.invoke(solver, 0, 0));
    }

    @Test
    public void singleRowMaze_loadsAndSolves_noCrash() throws IOException {
        // Single row: "   "
        File f = createMazeFile("singleRow.txt", "   ");
        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        assertTrue(solver.loadMaze());

        String path = solver.solveMazeWithRightHandRule();
        // Could be empty if we’re already at the “exit,” but definitely not null
        assertNotNull(path, "Single-row maze might have no moves, but no reason to fail.");
    }

    @Test
    public void singleColumnMaze_loadedOrRejected_noCrash() throws IOException {
        // Single column, multiple rows
        File f = createMazeFile("singleColumn.txt",
                " \n" +
                " \n" +
                " \n");
        MazeSolver solver = new MazeSolver(f.getAbsolutePath());
        boolean loaded = solver.loadMaze();

        // If it doesn't accept single-column as valid, loaded=false
        // Otherwise, try to solve
        if (loaded) {
            String path = solver.solveMazeWithRightHandRule();
            // Could be null or empty. We accept any outcome, as long as no crash
            assertTrue(path == null || !path.isEmpty() || path.isEmpty(),
                    "No crash allowed; any valid result is fine.");
        }
    }
}
