# GraphMaster
A small program to demonstrate and evaluate graph traversal algorithms with variations of the _15 Puzzle_.

Controls:
- *Add a node:* Right click on a pre-existing node.
- *Remove a node:* Hold the control key down while right clicking a node. NOTE: Removing a node removes all of its children.
- *Change the puzzle size/initial grid:* Remove the root node at the top of the tree and right click anywhere on the blank canvas.

Current features:
- Displays a hierarchical graph of moves from state to state
- Can add nodes to the graph (right click on a node)
- Can remove nodes from the graph (CTRL + right click on a node)
- Can add initial nodes to the graph (remove all nodes and then right click on the display area)

What's broken:
- Saving/loading
- Scaling (disabled because there were issues with mouse click translations)

What's missing:
- Batch grading
- Automated algorithms
