/**
 * 
 */
package com.example.amazebyconnormackinnon.generation;

import com.example.amazebyconnormackinnon.gui.Constants;

/**
 * Class encapsulates access to all information that constitutes a maze.
 * 
 * A maze is considered a rectangular arrangement of positions called
 * cells such that each cell is on a position (x,y) with 
 * {@code 0 <= x < width} and {@code 0 <= y < height} of the maze.
 * For each cell, one can tell where wallboards are in any of the
 * CardinalDirections (north, south, east, west), how many steps
 * it is to the cell that is at the exit of the maze, and what is the 
 * starting position for the game. The starting position is a cell that
 * is farthest away from the exit position. A maze is assumed to have
 * only a single exit and sufficient walls to make it interesting to play.
 * A MazeContainer also holds information in a BSP tree for drawing visible
 * walls.
 * 
 * A MazeContainer is generated and initialized in the generation of a maze
 * and not expected to change when the user plays the game because
 * walls are not expected to change during the game (only their visibility).
 * In particular, the current location and the current direction of the
 * user while playing the game are not stored in this class.
 * 
 * @author Peter Kemper
 *
 */
public class MazeContainer implements Maze {
	// properties of the current maze
	private int width; // width of maze
	// range: Constants.SKILL_X[0] <= width && width <= Constants.SKILL_X[last])
	private int height; // height of maze
	// range: Constants.SKILL_Y[0] <= width && width <= Constants.SKILL_Y[last])
	// (width,height) must be consistent with corresponding settings in mazecells and mazedists
	private Floorplan floorplan ; // maze as a matrix of cells which keep track of the location of wallboards
	private Distance mazedists ; // a matrix with distance values for each cell towards the exit
	// mazecells and mazedists should be consistent with respect to their
	// starting position, such that the starting position has the maximum distance value of all positions
	// exit position, such that the exit position in mazecells has the minimum distance value
	// and is also the exit position in mazedists
	
	private BSPNode rootnode ; // a binary tree type search data structure to quickly locate a subset of segments
	// a segment is a continuous sequence of wallboards in vertical or horizontal direction
	// a subset of segments need to be quickly identified for drawing
	// the BSP tree partitions the set of all segments and provides a binary search tree for the partitions
	
	private int[] start ; // the starting position (x,y) for the game
	// this position should be in in range: 0 <= x < width, 0 <= y < height
	// it should be consistent with mazedists.getStartPosition()
	// MEMO: redundant with Distance class that also tracks starting position
	// TODO: avoid redundant representation of starting position MazeContainer and Distance
		
	// to be fully initialized, we need all other fields being set consistently
	private boolean fullyInitialized;
	
	/**
	 * Default constructor leaves object in an state that requires a series of set methods
	 * to be called for proper initialization.
	 * These are {@link #setWidth(int) setWidth},
	 * {@link #setHeight(int) setHeight}, {@link #setFloorplan(Floorplan) setMazecells}, 
	 * {@link #setMazedists(Distance) setMazedists}, 
	 * {@link #setRootnode(BSPNode) setRootnode}, and 
	 * {@link #setStartingPosition(int, int) setStartingPosition}
	 */
	public MazeContainer() {
		// can not set up object to a meaningful start
		fullyInitialized = false;
	}
	/**
	 * Constructor with values for all attributes, delivers a fully operational maze
	 * @param width is the width of the maze, must conform with Constants.SKILL_X values
	 * @param height is the height of the maze, must conform with Constants.SKILL_Y values
	 * @param floorplan the floorplan to set, must not be null
	 * @param mazedists the mazedists to set, must be not null
	 * @param root the rootnode to set, must be not null
	 * @param startingPositionX is on the horizontal axis, {@code 0 <= x < width}
	 * @param startingPositionY is on the vertical axis, {@code 0 <= y < height}
	 * 
	 */
	public MazeContainer(int width, int height, Floorplan floorplan, Distance mazedists, BSPNode root, int startingPositionX, int startingPositionY) {
		setHeight(height);
		setWidth(width);
		setFloorplan(floorplan);
		setMazedists(mazedists);
		setRootnode(root);
		setStartingPosition(startingPositionX, startingPositionY);
		assert(isOperational()) : 
			"constructor for operational maze received faulty input!";
	}
	/**
	 * @param width is the width of the maze, must conform with Constants.SKILL_X values
	 */
	public void setWidth(int width) {
		assert(Constants.SKILL_X[0] <= width && 
				width <= Constants.SKILL_X[Constants.SKILL_X.length-1]);
		this.width = width;
	}
	/**
	 * @return the width of the maze, such that for any (x,y), {@code 0 <= x < width}
	 */
	public int getWidth() {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return width;
	}
	/**
	 * @param height is the height of the maze, must conform with Constants.SKILL_Y values
	 */
	public void setHeight(int height) {
		// check if value is in range of values
		assert(Constants.SKILL_Y[0] <= height && 
				height <= Constants.SKILL_Y[Constants.SKILL_Y.length-1]);	
		this.height = height;
	}
	/**
	 * @return the height of the maze, such that for any (x,y), {@code 0 <= y < height}
	 */
	public int getHeight() {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return height;
	}

	/**
	 * Gives the cells.
	 * Warning, returns direct access to internal field.
	 * @return the floorplan
	 */
	public Floorplan getFloorplan() {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return floorplan;
	}

	/**
	 * @param floorplan the floorplan to set, can not be null
	 */
	public void setFloorplan(Floorplan floorplan) {
		this.floorplan = floorplan;
	}

	/**
	 * Gives the distance. 
	 * Warning, returns direct access to internal field.
	 * @return the mazedists
	 */
	public Distance getMazedists() {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return mazedists;
	}

	/**
	 * Sets the distance.
	 * @param mazedists the mazedists to set, must be not null
	 */
	public void setMazedists(Distance mazedists) {
		this.mazedists = mazedists;
	}

	/**
	 * Gives the rootnode for the tree of BSPnodes.
	 * Warning, returns direct access to internal field.
	 * @return the rootnode
	 */
	public BSPNode getRootnode() {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return rootnode;
	}

	/**
	 * Sets the root for the tree of BSPnodes
	 * @param rootnode the rootnode to set
	 */
	public void setRootnode(BSPNode rootnode) {
		this.rootnode = rootnode;
	}
	/**
	 * Tells if the given position is inside a room.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return true if (x,y) position resides in an area marked as a room, false otherwise
	 */
	public boolean isInRoom(int x, int y) {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return floorplan.isInRoom(x, y);
	}
	/**
	 * Tells if given (x,y) position is valid, i.e. within legal range of values
	 * @param x is on the horizontal axis, {@code 0 <= x < width} 
	 * @param y is on the vertical axis, {@code 0 <= y < height} 
	 * @return true if {@code 0 <= x < width} and {@code 0 <= y < height}, false otherwise
	 */
	public boolean isValidPosition(int x, int y) {
		return ((0 <= x && x < width) && (0 <= y && y < height));
	}
	/**
	 * Gives the number of steps or moves needed to get to the exit.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return number of steps to exit
	 */
	public int getDistanceToExit(int x, int y) {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return mazedists.getDistanceValue(x, y) ;
	}
	/**
     * Calculates a distance to exit as a percentage. 
     * 1.0 is for the starting position as this is the maximal
     * distance possible. 
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
     * @return a value between 0.0 and 1.0, the smaller the closer
     */
    public float getPercentageForDistanceToExit(int x, int y) {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
    	return getDistanceToExit(x, y) / 
    			((float) mazedists.getMaxDistance());
    }
	/**
	 * Tells if at position (x,y) and looking into given direction faces a wallboard.
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return true if there is a wallboard, false otherwise
	 */
	public boolean hasWall(int x, int y, CardinalDirection dir) {
		assert(isOperational()) : 
			"accessing content before object is fully initialized!";
		return floorplan.hasWall(x, y, dir) ;
	}
    /**
     * Checks if the given position and direction faces a dead end
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
     * @param cd is a direction
     * @return true if at the given position there is
     * a wall to the left, right and front, false otherwise
     */
    public boolean isFacingDeadEnd(int x, int y, CardinalDirection cd) {
    	return (isValidPosition(x,y) &&
    			hasWall(x, y, cd) &&
    			hasWall(x, y, cd.oppositeDirection().rotateClockwise()) &&
    			hasWall(x, y, cd.rotateClockwise()));
    }
	/**
	 * Gives a (x',y') neighbor for given (x,y) that is closer to exit
	 * if it exists. 
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 * @return array with neighbor coordinates if neighbor exists, null otherwise
	 */
	public int[] getNeighborCloserToExit(int x, int y) {
		assert isValidPosition(x,y) : "Invalid position";
		// corner case, (x,y) is exit position
		if (floorplan.isExitPosition(x, y))
			return null;
		// find best candidate
		int dnext = getDistanceToExit(x, y) ;
		int[] result = new int[2] ;
		int[] dir;
		for (CardinalDirection cd: CardinalDirection.values()) {
			if (hasWall(x, y, cd)) 
				continue; // there is a wallboard in the way
			// no wallboard, let's check the distance
			dir = cd.getDxDyDirection();
			int dn = getDistanceToExit(x+dir[0], y+dir[1]);
			if (dn < dnext) {
				// update neighbor position with min distance
				result[0] = x+dir[0] ;
				result[1] = y+dir[1] ;
				dnext = dn ;
			}	
		}
		// expectation: we found a neighbor that is closer
		assert(getDistanceToExit(x, y) > dnext) : 
			"cannot identify direction towards solution: stuck at: " + x + ", "+ y ;
		// since assert statements need not be executed, check it 
		// to avoid giving back wrong result
		return (getDistanceToExit(x, y) > dnext) ? result : null;
	}

	/**
	 * Provides the (x,y) starting position.
	 * The starting position is typically chosen to by farthest away from the exit.
	 * @return the starting position, can be null
	 */
	public int[] getStartingPosition() {
		assert(isOperational()) : "accessing content before object is fully initialized!";
		return start;
	}

	/**
	 * Sets the starting position
	 * @param startingPosition the (x,y) coordinates of the starting position, can not be null
	 */
	public void setStartingPosition(int[] startingPosition) {
		assert (null != startingPosition && start.length == 2) : "MazeContainer.start illegal parameter value";
		assert isValidPosition(start[0], start[1]) : "Invalid starting position";
		this.start = startingPosition;
	}
	/** 
	 * Sets the starting position
	 * @param x is on the horizontal axis, {@code 0 <= x < width}
	 * @param y is on the vertical axis, {@code 0 <= y < height}
	 */
	public void setStartingPosition(int x, int y) {
		assert isValidPosition(x,y) : "Invalid starting position";
		if (null == start)
			start = new int[2] ;
		start[0] = x ;
		start[1] = y ;
	}
	/**
	 * Provides coordinates (x,y) of the exit position for this maze.
	 * This position is still inside the maze but at the very cell
	 * that has the exit at one of its sides.
	 * @return the exit position
	 */	
	public int[] getExitPosition() {
		assert(isOperational()) : "accessing content before object is fully initialized!";
		return mazedists.getExitPosition();
	}
	/**
	 * Checks if object is fully initialized and in a valid state.
	 * The object creation relies on a sequence of set methods being called,
	 * this method checks if that has led to a satisfying setting.
	 * @return true if all instance variables initialized with set-methods,
	 * 			false otherwise
	 */
	private boolean isOperational() {
		if (fullyInitialized) {
			return true; // was checked before
		}
		// check width and height, range of values determined by skill level
		// Constants class
		int last = Constants.SKILL_X.length-1;
		if (!(Constants.SKILL_X[0] <= width && width <= Constants.SKILL_X[last])) {
			return false;
		}
		if (!(Constants.SKILL_Y[0] <= height && height <= Constants.SKILL_Y[last])) {
			return false;
		}
		// check existence of cells, distances, bsp tree and starting position
		if (null == floorplan || null == mazedists || null == rootnode || null == start)
			return false;
		// the starting position must be 
		fullyInitialized = true;
		return true;
	}
}
