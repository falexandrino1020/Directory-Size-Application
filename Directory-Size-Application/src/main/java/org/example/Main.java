package org.example;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    //Part A: The tree

    // Both files and directories are "nodes" in the tree, and both
    // have a name and a size. So we make an abstract base class that
    // captures what they share.
    abstract static class FileSystemNode {
        private final String name;

        protected FileSystemNode(String name) {
            // A node with no name is a bug, so reject it early.
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            this.name = name;
        }

        public String getName() {
            return name;
        }

        // This is the key method. We DECLARE it here but don't define it.
        // Files and directories each answer "what's your size?" differently,
        // so each subclass gets its own version.
        public abstract long getSize();

        public abstract boolean isDirectory();
    }

    // A file is the simple case: it just knows its own size.
    static class FileNode extends FileSystemNode {
        private final long size;

        public FileNode(String name, long size) {
            super(name); // run the name-checking constructor
            if (size < 0) {
                throw new IllegalArgumentException("Size cannot be negative");
            }
            this.size = size;
        }

        // A file's size is just... its size. The recursion stops here.
        // This is the "base case" — the bottom of the tree.
        @Override
        public long getSize() {
            return size;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }
    }

    // A directory holds other nodes.
    static class DirectoryNode extends FileSystemNode {
        // A link back to the folder that contains this one. The root's
        // parent is null. We need this so "cd .." can walk back up.
        private final DirectoryNode parent;

        // The contents, keyed by name. LinkedHashMap keeps things in the
        // order we added them, this is important for using "ls".
        private final Map<String, FileSystemNode> children = new LinkedHashMap<>();

        public DirectoryNode(String name, DirectoryNode parent) {
            super(name);
            this.parent = parent;
        }

        public DirectoryNode getParent() {
            return parent;
        }

        // Add a file or subdirectory into this directory.
        public void add(FileSystemNode node) {
            // Don't allow two things with the same name in one folder.
            if (children.containsKey(node.getName())) {
                throw new IllegalArgumentException(
                        "An entry named '" + node.getName() + "' already exists");
            }
            children.put(node.getName(), node);
        }

        // Look up one entry by name. Returns null if it's not here.
        public FileSystemNode getChild(String name) {
            return children.get(name);
        }

        public Map<String, FileSystemNode> getChildren() {
            return children;
        }


        // A directory's size = the sum of all its children's sizes.
        // We loop over each child and ask IT for its size.
        //   - If the child is a file, getSize() returns a number (base case).
        //   - If the child is a directory, getSize() runs THIS SAME METHOD
        //     again on the smaller subtree (the recursive case).
        // The recursion naturally bottoms out at files, so it always finishes.
        @Override
        public long getSize() {
            long total = 0;
            for (FileSystemNode child : children.values()) {
                total += child.getSize();
            }
            return total;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }
    }

    // Part B: Shell
    static class Shell {
        private final DirectoryNode root;   // never changes; the top of the tree
        private DirectoryNode current;       // "where we are" — changes as we cd around

        public Shell(DirectoryNode root) {
            this.root = root;
            this.current = root; // we start at the top
        }

        public DirectoryNode getCurrent() {
            return current;
        }

        // Takes a raw line like "cd docs" and routes it to the right handler.
        public String execute(String line) {
            if (line == null || line.isBlank()) {
                return "";
            }
            // Split into at most 2 pieces: the command, and the rest (the argument).
            // "cd docs"  -> ["cd", "docs"]
            String[] parts = line.trim().split("\\s+", 2);
            String command = parts[0];
            String arg = parts.length > 1 ? parts[1] : "";

            // A switch picks the handler based on the command word.
            return switch (command) {
                case "cd"   -> cd(arg);
                case "ls"   -> ls();
                case "size" -> size();
                case "pwd"  -> pwd();
                case "help" -> help();
                default     -> "Unknown command: " + command + " (type 'help')";
            };
        }

        // cd: change which directory we're "in".
        private String cd(String arg) {
            if (arg.isEmpty() || arg.equals("/")) {
                current = root;           // "cd /" jumps to the top
                return "";
            }
            if (arg.equals("..")) {       // "cd .." goes up one level
                if (current.getParent() != null) {
                    current = current.getParent();
                }
                return "";
            }
            // Otherwise, look for a child folder with that name.
            FileSystemNode target = current.getChild(arg);
            if (target == null) {
                return "cd: no such directory: " + arg;
            }
            if (!target.isDirectory()) {  // can't cd into a file
                return "cd: not a directory: " + arg;
            }
            current = (DirectoryNode) target;
            return "";
        }

        // ls: list what's in the current directory.
        private String ls() {
            if (current.getChildren().isEmpty()) {
                return "(empty)";
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, FileSystemNode> entry : current.getChildren().entrySet()) {
                FileSystemNode node = entry.getValue();
                if (node.isDirectory()) {
                    // mark folders with a trailing slash
                    sb.append(node.getName()).append("/\n");
                } else {
                    sb.append(node.getName()).append("  ").append(node.getSize()).append(" bytes\n");
                }
            }
            return sb.toString().stripTrailing(); // drop the last newline
        }

        // size: the recursive total of the current directory. One call does it all.
        private String size() {
            return current.getSize() + " bytes";
        }

        // pwd: build the path string by walking from current up to root.
        private String pwd() {
            StringBuilder sb = new StringBuilder();
            DirectoryNode node = current;
            while (node != null && node.getParent() != null) {
                sb.insert(0, "/" + node.getName()); // prepend each name
                node = node.getParent();
            }
            return sb.length() == 0 ? "/" : sb.toString();
        }

        private String help() {
            return """
                   Available commands:
                     cd <dir>   change directory ('cd ..' to go up, 'cd /' for root)
                     ls         list current directory contents
                     size       total size of current directory (recursive)
                     pwd        print current directory path
                     help       show this message
                     exit       quit""";
        }
    }

    //Part C: Seed Data
    static DirectoryNode buildSeedData() {
        DirectoryNode root = new DirectoryNode("root", null);

        root.add(new FileNode("readme.txt", 100));

        DirectoryNode docs = new DirectoryNode("docs", root);
        docs.add(new FileNode("resume.pdf", 500));
        docs.add(new FileNode("notes.txt", 50));
        root.add(docs);

        DirectoryNode projects = new DirectoryNode("projects", root);
        DirectoryNode app = new DirectoryNode("app", projects);
        app.add(new FileNode("Main.java", 200));
        app.add(new FileNode("Util.java", 150));
        projects.add(app);
        projects.add(new FileNode("todo.md", 30));
        root.add(projects);

        //100 + 500 + 50 + 200 + 150 + 30 = 1030 bytes total.
        return root;
    }

    //Part D: Main
    public static void main(String[] args) {
        Shell shell = new Shell(buildSeedData());

        System.out.println("Directory Size Calculator. Type 'help' for commands, 'exit' to quit.");

        // try-with-resources: the Scanner is closed automatically at the end.
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                // Print a prompt showing where we are, like "/docs $ "
                System.out.print(shell.execute("pwd") + " $ ");
                if (!scanner.hasNextLine()) {
                    break; // input ended (e.g. you piped a file in)
                }
                String line = scanner.nextLine().trim();
                if (line.equals("exit")) {
                    break;
                }
                String output = shell.execute(line);
                if (!output.isEmpty()) {
                    System.out.println(output);
                }
            }
        }
        System.out.println("Goodbye.");
    }
}