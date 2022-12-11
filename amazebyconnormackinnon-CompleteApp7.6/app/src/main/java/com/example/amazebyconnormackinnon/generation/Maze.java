/**
 * 
 */
package com.example.amazebyconnormackinnon.generation;


/**
 * A Maze encapsulates all relevant information about a maze 
 * that can be explored in a game. 
 * A maze has finite dimensions width and height.
 * It has a floor plan that represents where walls are
 * with discrete integer locations in width and height range.
 * It has information about the distance to the exit position
 * from any location inside the maze.
 * It carries information about the starting position.
 * 
 * @author pk
 *
 */
public interface Maze {
	/**
	 * Set the width of the maze.
	 * @param width is greater or equal zero.
	 */
	void setWidth(int width);
	/**
	 * Set the height of the maze.
	 * @param height is greater or equal zero.
	 */
	void setHeight(int height);
	/**
	 * Get the height of the maze.
	 * @return the height 
	 */
	int getHeight();
	/**
	 * Get the width of the maze.
	 * @return the width 
	 */
	int getWidth();
	
	/**
	 * Gets the floorplan which describes where wallboards are in the current maze.
	 * @return the floorplan
	 */
	Floorplan getFloorplan();

	/**
	 * Sets the floorplan which describe where wallboards are in the current maze.
	 * @param floorplan the floorplan to set
	 */
	void setFloorplan(Floorplan floorplan);

	/**
	 * Gets a distance object for this maze to describe 
	 * for each position how many steps it is towards the exit.
	 * @return the mazedists
	 */
	Distance getMazedists();

	/**
	 * Sets the distance values towards the exit for this maze.
	 * Note that the dimensions of the distance matrix needs to match 
	 * with the cells.
	 * @param mazedists the distances to set
	 */
	void setMazedists(Distance mazedists) ;

	/**
	 * Gets access to a tree of nodes for segments of wallboards which is
	 * used for drawing the currently visible part.
	 * @return the rootnode
	 */
	BSPNode getRootnode();

	/**
	 * Sets the tree of nodes for segments of wallboards.
	 * @param rootnode the rootnode to set
	 */
	void setRootnode(BSPNode rootnode);
	/**
	 * Tells if (x,y) coordinate is within range.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return true if {@code 0 <= x < width, 0 <= y < height}
	 */
	boolean isValidPosition(int x, int y);
	/**
	 * Tells if the given position is inside a room.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return true if (x,y) position resides in an area marked as a room, false otherwise
	 */
	public boolean isInRoom(int x, int y);
	/**
	 * Tells how many steps it is from the given (x,y) coordinate
	 * to the exit position. 
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return the length of path to the exit
	 */
	public int getDistanceToExit(int x, int y);
	/**
     * Calculates a distance to exit as a percentage. 
     * 1.0 is for the starting position as this is the maximal
     * distance possible. 
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
     * @return a value between 0.0 and 1.0, the smaller the closer
     */
    public float getPercentageForDistanceToExit(int x, int y);
	/**
	 * Tells if one faces a wallboard at position (x,y) looking into the 
	 * given direction. Note that the cardinal direction is absolute
	 * and not relative to the current direction such as right or left.  
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @param dir is the direction in terms of North, East, South, West
	 * @return true if (x,y) is valid and there is a wallboard in the given direction, false otherwise
	 */
	public boolean hasWall(int x, int y, CardinalDirection dir) ;
    /**
     * Checks if the given position and direction faces a dead end
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
     * @param cd is a direction
     * @return true if at the given position there is
     * a wall to the left, right and front, false otherwise
     */
    public boolean isFacingDeadEnd(int x, int y, CardinalDirection cd);
	/**
	 * Provides coordinates of a position adjacent to the given (x,y)
	 * position that has a distance to the exit that is less than
	 * the distance for the given (x,y) position and there is also no 
	 * wall between the given position and the adjacent one. 
	 * For a maze that has a path from the any given position to
	 * the exit, any position other than the exit position itself
	 * must have an adjacent position that is closer. So with the 
	 * exception of the exit position, existence of such a neighbor
	 * is guaranteed for any position. However, the neighbor need
	 * not be unique. One can imanine situations where more than 
	 * one neighbor would have the same distance to the exit and 
	 * this method would have to pick one.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return int array of length 2 with (x',y') coordinates for neighbor if exists, null otherwise
	 */
	public int[] getNeighborCloserToExit(int x, int y);
	/**
	 * Provides coordinates (x,y) of the starting position for this maze.
	 * Maze generation algorithms are expected to use the position
	 * that is the farthest away from the exit as the starting position, 
	 * i.e., the one with the maximum distance.
	 * @return the starting position
	 */	
	public int[] getStartingPosition();
	/**
	 * Sets coordinates (x,y) of the starting position for this maze.
	 * Maze generation algorithms are expected to use the position
	 * that is the farthest away from the exit as the starting position, 
	 * i.e., the one with the maximum distance.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 */
	public void setStartingPosition(int x, int y);
	/**
	 * Provides coordinates (x,y) of the exit position for this maze.
	 * This position is still inside the maze but at the very cell
	 * that has the an opening at one of its sides that leads to the
	 * outside of the maze. This position is called exit position.
	 * It is unique and its distance to the exit has a value of 1. 
	 * @return the exit position
	 */	
	public int[] getExitPosition();
}
