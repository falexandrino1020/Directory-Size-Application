package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    // A lone file just reports its own size.
    @Test
    void fileReturnsItsOwnSize() {
        Main.FileNode file = new Main.FileNode("a.txt", 42);
        assertEquals(42, file.getSize());
    }

    // An empty folder weighs nothing.
    @Test
    void emptyDirectoryHasZeroSize() {
        Main.DirectoryNode dir = new Main.DirectoryNode("empty", null);
        assertEquals(0, dir.getSize());
    }

    // A folder with two files = their sum. (One level deep.)
    @Test
    void directorySizeSumsDirectFiles() {
        Main.DirectoryNode dir = new Main.DirectoryNode("d", null);
        dir.add(new Main.FileNode("a", 10));
        dir.add(new Main.FileNode("b", 20));
        assertEquals(30, dir.getSize());
    }

    // The important one: a file nested inside a subfolder still counts.
    // This is what proves the recursion actually recurses.
    @Test
    void directorySizeIsRecursive() {
        Main.DirectoryNode root = new Main.DirectoryNode("root", null);
        root.add(new Main.FileNode("top", 5));
        Main.DirectoryNode sub = new Main.DirectoryNode("sub", root);
        sub.add(new Main.FileNode("deep", 15));
        root.add(sub);
        assertEquals(20, root.getSize());
    }

    // The seed tree should total the 1030 we computed by hand.
    @Test
    void seedDataTotalSizeIsCorrect() {
        Main.DirectoryNode root = Main.buildSeedData();
        assertEquals(1030, root.getSize());
    }

    // Two entries with the same name in one folder should be rejected.
    @Test
    void duplicateNameRejected() {
        Main.DirectoryNode dir = new Main.DirectoryNode("d", null);
        dir.add(new Main.FileNode("x", 1));
        assertThrows(IllegalArgumentException.class,
                () -> dir.add(new Main.FileNode("x", 2)));
    }

    // Negative file sizes are nonsense and should be refused.
    @Test
    void negativeFileSizeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Main.FileNode("bad", -1));
    }

    // Navigate into a folder, then check size — exercises the Shell.
    @Test
    void cdIntoSubdirectoryThenSize() {
        Main.Shell shell = new Main.Shell(Main.buildSeedData());
        shell.execute("cd projects");
        assertEquals("380 bytes", shell.execute("size"));
    }

    // "cd .." returns to the parent.
    @Test
    void cdDotDotGoesToParent() {
        Main.Shell shell = new Main.Shell(Main.buildSeedData());
        shell.execute("cd docs");
        shell.execute("cd ..");
        assertEquals("root", shell.getCurrent().getName());
    }

    // cd into something that doesn't exist gives a clear error.
    @Test
    void cdNonexistentReturnsError() {
        Main.Shell shell = new Main.Shell(Main.buildSeedData());
        assertTrue(shell.execute("cd nope").startsWith("cd: no such directory"));
    }
}


