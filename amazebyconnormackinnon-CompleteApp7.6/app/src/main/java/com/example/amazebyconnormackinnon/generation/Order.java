package com.example.amazebyconnormackinnon.generation;


/**
 * An order describes functionality needed to order a maze from
 * the maze factory. It allows for asynchronous production 
 * with a mechanism to deliver a MazeConfiguration.
 * 
 * @author Peter Kemper
 *
 */
public interface Order {
	/**
	 * Gives the required skill level, range of values 0,1,2,...,15.
	 * @return the skill level or size of maze to be generated in response to an order
	 */
	int getSkillLevel() ;
	/** 
	 * Gives the requested builder algorithm, possible values 
	 * are listed in the Builder enum type.
	 * @return the builder algorithm that is expected to be used for building the maze
	 */
	Builder getBuilder() ;
	/**
	 * Lists all maze generation algorithms that are supported
	 * by the maze factory (Eller needs to be implemented for P2)
	 *
	 */
	enum Builder {DFS, Prim, Kruskal, Eller, Boruvka};
	/**
	 * Describes if the ordered maze should be perfect, i.e. there are 
	 * no loops and no isolated areas, which also implies that 
	 * there are no rooms as rooms can imply loops
	 * @return true if a perfect maze is wanted, false otherwise
	 */
	boolean isPerfect() ;
	/**
	 * Gives the seed that is used for the random number generator
	 * used during the maze generation.
	 * @return the current setting for the seed value of the random number generator
	 */
	int getSeed();
	/**
	 * Delivers the produced maze. 
	 * This method is called by the factory to provide the 
	 * resulting maze as a MazeConfiguration.
	 * It is a call back function that is called some time
	 * later in response to a client's call of the order method.
	 * @param mazeConfig is the maze that is delivered in response to an order
	 */
	void deliver(Maze mazeConfig) ;
	/**
	 * Provides an update on the progress being made on 
	 * the maze production. This method is called occasionally
	 * during production, there is no guarantee on particular values.
	 * Percentage will be delivered in monotonously increasing order,
	 * the last call is with a value of 100 after delivery of product.
	 * @param percentage of job completion
	 */
	void updateProgress(int percentage) ;
}
