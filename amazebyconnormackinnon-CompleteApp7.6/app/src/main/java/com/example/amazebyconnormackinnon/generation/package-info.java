/**
 * This package contains classes used in the generation and representation of a maze.
 * <p>
 * For the maze representation, contributing classes are: BSPNode, BSPLeaf, BSPBranch,
 * Distance, Floorplan, MazeContainer, Wall, and Wallboard.
 * The MazeContainer class is a wrapper that represents an overall maze, other classes
 * provide ingredients that are accessible through MazeContainer methods.
 * </p>
 * <p>
 * For the maze generation, clients interact with a Factory and provide it with an
 * Order that the Factory then delivers. The MazeFactory is the class that implements
 * the factory, the DefaultOrder is a class that implements an Order. The generation
 * is performed in a background thread that such that the interaction with the factory
 * is synchronous but triggers an asynchronous call to the actual generation which will
 * eventually call the deliver method that acts as a callback method. The order carries
 * not just the input specification on what kind of maze is wanted but after deliver is
 * called also a reference to the resulting generated maze.
 * </p>
 */
package com.example.amazebyconnormackinnon.generation;