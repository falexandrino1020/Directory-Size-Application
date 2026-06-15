# Directory Size Application

# 

A command-line app that simulates a file system and calculates directory sizes

recursively. Supports `cd`, `ls`, and `size` on an in-memory directory tree.

# 

# Approach and Design

# 

The file system is modeled as a \*\*tree\*\*. Every item is a `FileSystemNode` with

a name and a size:



\- `FileNode` — a file; its size is fixed.

\- `DirectoryNode` — a folder that holds other nodes (files or directories).



A directory's size is the sum of its children's sizes. Because a child can

itself be a directory, `getSize()` is \*\*recursive\*\*: a file returns its own

size (the base case), and a directory adds up its children, recursing into any

subfolders. Since every branch ends in files, the recursion always terminates.



A separate `Shell` class interprets the commands and tracks the current

directory. Keeping it apart from the tree model means the logic can be tested

by passing in command strings, without simulating keyboard input.

# 

# Key Files and Folders

# 

\-`src/main/java/org/example/Main.java` — the whole application:

&#x20; - `FileSystemNode`, `FileNode`, `DirectoryNode` — the tree model

&#x20; - `Shell` — the command interpreter

&#x20; - `buildSeedData()` — builds the sample directory tree

&#x20; - `main()` — the interactive command loop

\- `src/test/java/org/example/MainTest.java` — the JUnit 5 tests



# Seed Data

# 

On startup the app loads a sample tree totaling \*\*1030 bytes\*\*:



root/

&#x20;   -readme.txt        (100)

&#x20;   -docs/

&#x20;       -resume.pdf    (500)

&#x20;       -notes.txt     (50)

&#x20;   -projects/

&#x20;       -app/

&#x20;           -Main.java (200)

&#x20;           -Util.java (150)

&#x20;       -todo.md       (30) 



# How to Run



Requires \*\*JDK 21\*\*.



```bash

git clone https://github.com/falexandrino1020/Directory-Size-Application.git

cd Directory-Size-Application

javac -d out src/main/java/org/example/Main.java

java -cp out org.example.Main

```



Or open the folder in IntelliJ and click the green \*\*Run\*\* arrow next to `main`.



Then type commands at the prompt:



```

ls        list the current directory

cd <dir>  change directory (cd .. to go up, cd / for root)

size      total size of the current directory (recursive)

pwd       print the current path

help      list commands

exit      quit

```

# 

# \## How to Test and Verify

# 

In IntelliJ, right-click `MainTest.java` and select \*\*Run\*\*. Ten JUnit 5 tests

verify the recursive size calculation, input validation, and shell navigation.



You can also verify by hand against the seed data:



\- `size` at the root prints `1030 bytes`

\- `cd projects` then `size` prints `380 bytes`

\- `cd app` then `size` prints `350 bytes`



