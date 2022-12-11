package com.example.amazebyconnormackinnon.generation;

/**
 * This class has the responsibility to provide the distance
 * for each cell to the exit of a maze.
 * It represents this information as a matrix, a 2D integer array.
 * Its dimensions and positioning of (x,y) coordinates matches
 * with the same notion in the Cells.java class
 * that represents the walls for the maze.
 *  
 * All methods assume that given (x,y) coordinates are
 * with its legal range {@code[0,width-1],[0,height-1]}
 * such that no additional parameter checks are performed.
 * This is reasonable as this class is used only internally
 * to the package.
 * 
 * The expected lifecycle for an instance of this class is:
 * a) initialization either by providing a distance matrix
 * with valid distance values or by providing a cells
 * object and calling for {@link #computeDistances(Floorplan) computeDistances}
 * to derive valid distance values, before
 * b) obtaining information on distance values
 * for specific positions as well as information about 
 * the exit or starting position for the maze.
 * 
 * This code is refactored code from MazeBuilder.java by 
 * Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class Distance {
	/**
	 * The width of the distance matrix, same as width of maze and cells
	 */
	private int width;
	/**
	 * The height of the distance matrix, same as height of maze and cells
	 */
	private int height; 
	/**
	 * A matrix of dimension (width x height) with 
	 * distance values to the exit of a maze.
	 * The use of indices between Distance and Cells internal 2D matrices
	 * is consistent such that (i,j) in dists refers to the same 
	 * position in cells.
	 * Warning: class does not enforce encapsulation
	 * on dists. There are ways to set this reference,
	 * obtain it and manipulate its content. 
	 */
	private int[][] dists; 
	/** 
	 * The exit position has a distance of 1
	 * which is also the minimum of all values in dists.
	 * Array of length 2, with coordinates (x,y) of exit position.
	 */
	private int[] exitPosition;
	/**
	 * The start position has a maximum distance
	 * from the exit position, which is also
	 * the maximum of all vaues in dists.
	 * Array of length 2, with coordinates (x,y) of starting position.
	 * The field is initially null.
	 * Can be calculated only after distance values 
	 * have been computed or distance matrix has been given
	 * to this instance.
	 */
	private int[] startPosition;
	// Memo: the start position is also stored in the MazeContainer, supports a set method
	// this could lead to inconsistent settings between MazeContainer and Distance object at runtime.
	// TODO: avoid redundancy of start position, remove startPosition from Distance, replease with getPositionWithMaxDistance
	
	/**
	 * Constructor
	 * @param w is the width of the maze in the horizontal direction
	 * @param h is the height of the maze in the vertical direction
	 */
	public Distance(int w, int h) {
		// note: method call below sets width, height, dists,
		// exitPosition and startPosition
		setAllDistanceValues(new int[w][h]);
	}
	/**
	 * Constructor that directly uses the given reference.
	 * Warning: the given parameter is shared, not copied. 
	 * This breaks encapsulation and can have side effects.
	 * This constructor is currently only used in the
	 * MazeFileReader to hand a 2D array
	 * that is parsed from a file to a Distance object. 
	 * @param distances is a non-null matrix of appropriate width x height dimension
	 */
	public Distance(int[][] distances) {
		// note: method call below sets width, height, dists,
		// exitPosition and startPosition
		setAllDistanceValues(distances);
	}
	/**
	 * Sets the internal attribute to the given parameter value.
	 * Can be used to provide precomputed distance values to an object
	 * or to use an existing array and have its values computed
	 * by a subsequent call to {@link #computeDistances(Floorplan)} computeDistances.
	 * Warning, the array is not copied but directly shared with the environment
	 * that provides it! This can have undesired side effects. Handle with care!
	 * @param distances is an array with distance values to the exit, can not be null
	 */
	public void setAllDistanceValues(int[][] distances) {
		// side constraint: dists must be of dimension width x height
		width = distances.length;
		height = distances[0].length;
		this.dists = distances;
		// reset exit and start positions
		// note: lazy evaluation in the sense that positions are
		// computed on demand and then cached on corresponding
		// fields exitPosition and startPosition
		exitPosition = null;
		startPosition = null;
	}
	/**
	 * Gets access to a width x height array of distances. 
	 * Warning, this exposes the internal attribute
	 * and is intended for read access only. 
	 * Do not modify entries of the returned array.
	 * @return array with distance values
	 */
	public int[][] getAllDistanceValues() {
		return dists;
	}
	/**
	 * Gets the distance value for the given (x,y) position
	 * @param x is the x coordinate, {@code 0 <= x < width}
	 * @param y is the y coordinate, {@code 0 <= y < height}
	 * @return the distance value for the given (x,y) position
	 */
	public int getDistanceValue(int x, int y) {
		return dists[x][y] ;
	}
	/**
	 * Compute distances for given cells object of a maze.
	 * The method determines an exit position for the maze
	 * as well as a starting position. It computes a distance
	 * value for each position (x,y) that represents the number
	 * of steps through intermediate cells to reach the exit with
	 * the understanding that one can not cross walls and can 
	 * only to go an adjacent position if there is no wallboard in 
	 * between. The exit position is returned for convenience.
	 * This method provides the main responsibility of the class, 
	 * namely to determine the distance to the exit position. 
	 * Once called, client classes can use {@link #getDistanceValue(int, int)
	 * getDistanceValue()}, {@link #getExitPosition() getExitPosition()}, 
	 * and {@link #getStartPosition() getStartPosition()}.
	 * @param cells with information on wallboards for a maze, can not be null, read only
	 * @return exit position somewhere on the  border
	 */
	public int[] computeDistances(Floorplan cells) {
		// constraint: cells must be of size (width x height)
		
		// Step 1: find a suitable exit position
		// Heuristic:
		// compute temporary distances for a starting point 
		// (x,y) = (width/2,height/2) 
		// which is located in the center of the maze
		computeDists(cells, width/2, height/2);
		// figure out which position is the farthest on the border 
		// to find a candidate for the exit position
		exitPosition = getPositionWithMaxDistanceOnBorder();
		// Step 2: compute distances with regards to the exit position
		computeDists(cells, exitPosition[0], exitPosition[1]);
		return exitPosition ;
	}

	/**
	 * Gets the starting position.
	 * Assumes that computeDistances() was called before.
	 * @return starting position somewhere within maze
	 */
	public int[] getStartPosition() {
		if (null == startPosition)
			startPosition = getPositionWithMaxDistance() ;
		return startPosition ;
	}
	/**
	 * Gets maximum distance present in maze
	 * Assumes that computeDistances() was called before
	 * @return the maximum distance
	 */
	public int getMaxDistance() {
		// the maximum distance is at the starting position
		int[] start = getStartPosition();
		return getDistanceValue(start[0], start[1]);
	}
	/**
	 * Gets minimum distance present in maze
	 * Assumes that computeDistances() was called before
	 * @return the minimum distance which should be 1
	 * and is located at the exit position
	 */
	public int getMinDistance() {
		// the maximum distance is at the exit position
		int[] exit = getExitPosition();
		return getDistanceValue(exit[0], exit[1]);
	}
	/**
	 * Gets the exit position.
	 * Assumes that computeDistances() was called before.
	 * @return exit position somewhere on the border
	 */
	public int[] getExitPosition() {
		if (null == exitPosition)
			exitPosition = getPositionWithMinDistance() ;
		return exitPosition ;
	}
	/**
	 * Determines if given position is the exit position.
	 * @param x is the x coordinate, {@code 0 <= x < width}
	 * @param y is the y coordinate, {@code 0 <= y < height}
	 * @return true if (x,y) is the exit position, falls otherwise
	 */
	public boolean isExitPosition(int x, int y){
		if (null == exitPosition)
			exitPosition = getPositionWithMinDistance() ;
		return ((x == exitPosition[0]) && (y == exitPosition[1])) ;
	}
	//////////////////////////////////////////////////////////////////////////
	//////////////// private, internal methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	/**
	 * Finds the most remote point in the maze somewhere on the border. 
	 * Requires that distances have been computed beforehand.
	 * @return array of length 2 encodes position 
	 * {@code (x,y)=(array[0],array[1])}
	 */
	private int[] getPositionWithMaxDistanceOnBorder() {
		// return result in an array of length 2
		int[] result = new int[2] ;
		int remoteDist = 0;
		for (int x = 0; x < width; x++) {
			remoteDist = keepMaxDistance(x, 0, remoteDist, result);
			remoteDist = keepMaxDistance(x, height-1, remoteDist, result);
		}
		for (int y = 0; y < height; y++) {
			remoteDist = keepMaxDistance(0, y, remoteDist, result);
			remoteDist = keepMaxDistance(width-1, y, remoteDist, result);
		}
		return result;
	}

	/**
	 * Get the position of the entry with the highest value.
	 * @return array of length 2 encodes position
	 * {@code (x,y)=(array[0],array[1])}
	 */
	private int[] getPositionWithMaxDistance() {
		int d = 0;
		int[] result = new int[2] ;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				d = keepMaxDistance(x, y, d, result);
			}
		}
		//maxDistance = d ; // memorize maximal distance for other purposes
		return result ;
	}
	/**
	 * For a given candidate position (x,y), the method checks if it
	 * exceeds the given threshold for the distance value. If so
	 * it updates the result coordinates accordingly and returns 
	 * the new threshold value.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param d the current maximum distance as a threshold for comparison
	 * @param result the (x,y) coordinates of the position to memorize
	 * @return the new maximum distance after the comparison
	 */
	private int keepMaxDistance(int x, int y, int d, int[] result) {
		if (dists[x][y] > d) {
			result[0] = x;
			result[1] = y;
			d = dists[x][y];
		}
		return d;
	}
	/**
	 * Get the position of the entry with the smallest value. 
	 * If the values of the distance matrix are accurate,
	 * the result is the exit position.
	 * @return position with smallest distance
	 */
	private int[] getPositionWithMinDistance() {
		int d = INFINITY ;
		int[] result = new int[2] ;
		for (int x = 0; x != width; x++)
			for (int y = 0; y != height; y++) {
				if (dists[x][y] < d) {
					result[0] = x;
					result[1] = y;
					d = dists[x][y];
				}
			}
		return result ;
	}
	
	/**
	 * Define a readable constant to express that we have not 
	 * found a finite distance value for some (x,y) position 
	 * in matrix dists.
	 */
	static final int INFINITY = Integer.MAX_VALUE; 

	/**
	 * Computes distances to the given position (ax,ay) for all cells in array dists.
	 * @param cells with information on wallboards for a maze, can not be null
	 * @param ax, position, x coordinate
	 * @param ay, position, y coordinate
	 */
	private void computeDists(Floorplan cells, int ax, int ay) {
		//int x, y;
		// initialize the distance array with a value for infinity 
		setAllDistanceValues(INFINITY) ;
		// set the final distance at the exit position
		dists[ax][ay] = 1;
		// distribute values from this position to get started
		pushDistanceValuesDFS2(cells, ax, ay);
		int toDoCounter = countInfinity();
		int progress; // used to recognize fixpoint, no progress, then stop
		// go over this array as long as we can find something to do
		// MEMO: there are likely to be much smarter ways to distribute distances in a breadth first manner...
		// why not push identified cells with infinite distance on a "work to do" heap
		// TODO: limit iterations to upperbound which is #cells == width * height
		// in each iteration at least one cell should benefit from neighbor
		// in each iteration at least one cell should receive its final value
		// TODO: do a final checkup for enclosed areas left over with Max Int
		do {
			// check all entries in the distance array
			for (int x = 0; x != width; x++) {
				for (int y = 0; y != height; y++) 
				{
					if (dists[x][y] == INFINITY) {
						continue;
					}
					// if the distance is not infinite, 
					// let's see if the cell has a neighbor that we can update and 
					// perform a depth first search on.
					pushDistanceValuesDFS2(cells, x, y);
				}
			}
			progress = toDoCounter - countInfinity();
			toDoCounter -= progress; // update to current value
		} while (0 < progress);
		if (0 < countInfinity())
			System.out.println("Distance: ERROR: All positions should have a finite distance to the exit, counted: " + countInfinity() );
		assert (0 == countInfinity()) : 
			"All positions should have a finite distance to the exit";
		// the computation is not good enough to have truly minimum values
		saturateDistances(cells);
	}
	/**
	 * Follows a path as far as possible to push small distance values
	 * into the maze
	 * @param cells needed for checking wallboards, read only
	 * @param sx starting position
	 * @param sy starting position
	 */
	private void pushDistanceValuesDFS2(Floorplan cells, int sx, int sy) {
		while (true) {
			CardinalDirection nextn = updateNeighborDistancesAndDirection(cells, sx, sy);
			if (nextn == null)
				break; // exit the loop if we cannot find another cell to proceed with
			// update coordinates for next cell
			int[] dir = nextn.getDxDyDirection();
			sx += dir[0];
			sy += dir[1];
			// follow the nextn node on a depth-first-search path
		}
	}

	
	/**
	 * Sets all values in dists to given value
	 * @param value
	 */
	private void setAllDistanceValues(int value) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				dists[x][y] = value ;
			}
		}
	}
	/**
	 * Counts how often infinity values is present in matrix
	 * @return the number of cells with an infinite distance to the exit
	 */
	private int countInfinity() {
		int result = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (dists[x][y] == INFINITY)
					result++;
			}
		}
		return result;
	}
	/**
	 * Updates distance values for adjacent cells that are reachable
	 * if value can be reduced to current distance plus 1. 
	 * @param cells with information on wallboards for a maze, can not be null, read only
	 * @param currentX x coordinate of current position
	 * @param currentY y coordinate of current position
	 * @return direction for a neighbor that has been updated or null if there is none
	 */
	private CardinalDirection updateNeighborDistancesAndDirection(Floorplan cells, int currentX, int currentY) {
		CardinalDirection result = null; // returns null by default
		int nextDistance = dists[currentX][currentY] + 1; // distance of a neighbor
		int[] dir;
		int nextX;
		int nextY;
		// check all four directions, update distance as needed
		for (CardinalDirection cd: CardinalDirection.values()) {
			// check for reachable neighbors
			if (cells.hasNoWall(currentX, currentY, cd)) {
				// check if neighbor is on board
				dir = cd.getDxDyDirection();
				nextX = currentX+dir[0];
				nextY = currentY+dir[1];
				// check if cell at (nextX,nextY) is within bounds
				if ((0 <= nextX && nextX < width) && (0 <= nextY && nextY < height)) {
					// check if neigbor's distance needs update
					if (dists[nextX][nextY] > nextDistance) {
						// update the neighbor's distance value
						// mark that cell as the next one
						dists[nextX][nextY] = nextDistance;
						result = cd;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Fix-point iteration on distance matrix.
	 * @param  cells with information on wallboards for a maze, can not be null, read only
	 */
	private void saturateDistances(Floorplan cells) {
		boolean progress = false; 
		//int count = 0;
		int[] dir = null;
		CardinalDirection nextn;
		do {
			progress = false; // reset termination criterion
			// go through all positions
			// check if we can update a neighbor to 
			// a shorter distance, if so we made progress
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					nextn = updateNeighborDistancesAndDirection(cells, x, y);
					if (null != nextn) {
						progress = true; // something changed
						// follow neighbor on a depth-first-search path
						dir = nextn.getDxDyDirection();
						pushDistanceValuesDFS2(cells, x+dir[0], y+dir[1]);		
					}
				}
			}
			//count++;
		} while (progress);
		//System.out.println("Distance.saturate: #iterations needed " + count);
	}
}