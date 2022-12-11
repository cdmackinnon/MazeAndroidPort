package com.example.amazebyconnormackinnon.generation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class represents the floorplan of a maze.
 * It describes a rectangular shape of squares or quadratic cells
 * with a 2 dimensional grid and it encapsulates access to each 
 * cell in that grid.
 *  
 * The internal two-dimensional array matches with a grid of cells as follows:
 * floorplan[0,y] form the left border, hence there is a wallboard on  left.
 * floorplan[width-1,y] form the right border, hence there is a wallboard on right.
 * floorplan[x,0] form the top border, hence there is a wallboard on top.
 * floorplan[x,height-1] form the bottom border, hence there is a wallboard on bottom.
 * The upper left corner is seen as position [0][0].
 * 
 * Warning: MapDrawer locates (0,0) at the bottom-left corner on the screen
 * when it draws the map.
 * 
 * Each cell has a location (x,y) on the grid. 
 * It stores information for each direction 
 * whether there is a wallboard installed on that side and
 * if so whether that wallboard needs to stay like a load bearing wall.
 * The exterior walls of the maze and the exterior walls of rooms
 * inside the maze are "load bearing" and need to stay in place.
 * This is captured with a notion of a border. 
 * 
 * A border is not removed by the maze generation procedure. 
 * It is used to mark the outside border of the maze as well 
 * as those wallboards of internal rooms that should remain in place. 
 * One can think of the border attribute as a little sticker 
 * on a wallboard that says "leave this wallboard in place".
 * Wallboards can be taken down by the maze generation procedure
 * otherwise.
 * 
 * Note that for a calculated maze, at least one cell on the border 
 * will have a missing wallboard for an exit somewhere.
 * 
 * The internal encoding of this information into a single integer
 * for each cell is performed with bit operations {@code (&,|)} 
 * to set individual bits in a 4 byte/32 bit integer 
 * and thus error prone. The encapsulation within this class 
 * localizes all bit operations for this encoding.
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * The class resulted from refactoring the int[][] cells area in the original Maze and Mazebuilder classes into a class of its own.
 * Refactored by Peter Kemper
 */
public class Floorplan {
	
	// Integer constants to encode 4 possible wallboards for a single cell (CW = cell wallboard) 
	// on top, bottom, left, right in a single byte of an integer
	// WARNING: The numerical values are used for bit operations and thus matter, 
	// they encode a particular bit pattern for & and | operations 
	public static final int CW_TOP = 1;  // 2^0
	public static final int CW_BOT = 2;  // 2^1
	public static final int CW_LEFT = 4; // 2^2
	public static final int CW_RIGHT = 8;// 2^3
	public static final int CW_VISITED = 16; // 2^4 is used as a flag to indicate if a cell is new (1) or if it has been visited before (0)
	public static final int CW_ALL = CW_TOP|CW_BOT|CW_LEFT|CW_RIGHT; // constant to simplify checking if all wallboards are present
	// Integer constants to encode 4 possible sides that touch a border (or bound)
	// a separate encoding of borders allows for flexible layouts (not just rectangles)
	// Note: encoding matches the wallboard encoding with respect to directions such that same encoding applies plus a shift
	public static final int CW_BOUND_SHIFT = 5; // used to shift encoding from dirsx, dirsy below from wallboard range to bound range 
	// following values get calculated by getBoundForBit
	//public static final int CW_TOP_BOUND = 32; // 2^5
	//public static final int CW_BOT_BOUND = 64; // 2^6
	//public static final int CW_LEFT_BOUND = 128; // 2^7
	//public static final int CW_RIGHT_BOUND = 256; // 2^8
	//public static final int CW_ALL_BOUNDS = CW_TOP_BOUND|CW_BOT_BOUND|CW_LEFT_BOUND|CW_RIGHT_BOUND; // constant to simplify check if all all bounds are present
	public static final int CW_IN_ROOM = 512; // 2^9
	// we put all encodings into a single array such that it is easier to iterate over the array
	// note that the numerical values are used for bitwise calculations 
	// so a refactoring with other values in an enumeration can break the code
	// Directions:
	// columns mean right, bottom, left, top (as implemented in getBit())
	// note that multiplication with -1 to a column switches directions
	//public static int[] DIRS_X = { 1, 0, -1, 0 };
	//public static int[] DIRS_Y = { 0, 1, 0, -1 };
	//Current mapping between cardinal directions and (dx,dy)
	//east  = (1,0)
	//south = (0,1)
	//west  = (-1,0)
	//north = (0,-1)
	
	private int width;
	private int height ;
	private int[][] cells; // width x height array of cells, cells[width][height]
	// each cell contains an integer which encodes presence/absence of wallboards
	// cells[i][j] can be read as (i,j) coordinates much like (x,y) coordinates
	// where the first dimension x grows towards the right and 
	// the second dimension y grows towards the bottom
	// as if the (0,0) position is in the top-left corner.
	// cells[i][j] is not intuitive in terms of rows and column indices in matrix notation 
	// as position (i,j) would be column i and row j which is opposite to the normal  
	// use of rows and columns indices for matrices.
	
	/**
	 * Constructor
	 * @param w width, {@code 0 < w}
	 * @param h height, {@code 0 < h}
	 */
	public Floorplan(int w, int h) {
		width = w ;
		height = h ;
		cells = new int[w][h];
	}

	/**
	 * Constructor that dimensions and initializes cells with the values from the given matrix.
	 * This constructor is useful to obtain a duplicate of a floorplan.
	 * @param input provides input data to copy cell content from
	 * precondition input != null
	 */
	public Floorplan(int[][] input){
		// Alternative, 2d array is a 1d array with arrays as elements
		width = input.length ;
		height = input[0].length ;
		cells = new int[width][];
		for(int i = 0; i < width; i++)
		    cells[i] = input[i].clone(); // clone row
		/* Basic version
		this(input.length, input[0].length);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = input[i][j];
			}
		}
		*/
	}
	
	/**
	 * Initialize floorplan such that all cells have not been visited,
	 * all wallboards inside the maze are up,
	 * and borders form a rectangle on the outside of the maze.
	 * It is fully enclosed, there is no exit.
	 */
	public void initialize() {
		int x, y;
		// Step 1: erect wallboards everywhere, reset visited flag
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				setBitToOne(x, y, (CW_VISITED | CW_ALL));
			}
		} 
		// Step 2: mark wallboards on exterior walls as a border
		// Assumption: (0,0) at top-left corner
		// mark exterior wallboards to top and bottom
		for (x = 0; x < width; x++) {
			setBitToOne(x, 0, getBoundForBit(CW_TOP));
			setBitToOne(x, height-1, getBoundForBit(CW_BOT));
		} 
		// mark exterior wallboards to left and right 
		for (y = 0; y < height; y++) {
			setBitToOne(0, y, getBoundForBit(CW_LEFT));
			setBitToOne(width-1, y, getBoundForBit(CW_RIGHT));
		}
	}
	
	/**
	 * Equals method that checks if the other object matches in dimensions and content.
	 * @param other provides fully functional floorplan object to compare its content
	 */
	@Override
	public boolean equals(Object other){
		// trivial special cases
		if (this == other)
			return true ;
		if (null == other)
			return false ;
		if (getClass() != other.getClass())
			return false ;
		// general case
		final Floorplan o = (Floorplan)other ; // type cast safe after checking class objects
		if ((width != o.width)||(height != o.height))
			return false ;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (cells[i][j] != o.cells[i][j])
					return false ;
			}
		}
		return true ;
	}
	/**
	 * Hashcode method is not implemented as it is not needed here.
	 * Dummy method to recognize common pitfall from overriding equals() but 
	 * not hashcode() method.
	 */
	@Override
	public int hashCode() {
		  assert false : "hashCode not designed";
		  return 42; // any arbitrary constant will do
	}
	/**
	 * Get the value of a cell at the given position (x,y).
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 * @return value with internal encoding of wallboards and other attributes for the cell at position (x,y)
	 */
	public int getValueOfCell( int x, int y )
	{
		return cells[x][y] ;
	}
	
	/**
	 * checks if cell (x,y) and adjacent neighbor in the given direction are not separated by a border 
	 * and the neighbor has not been visited before.
	 * @param wallboard provides (x,y) coordinates for cell and the direction
	 * precondition borders limit the outside of the maze area
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 * @return true if neighbor in the given direction is new and wallboard can be taken down, false otherwise
	 */
	public boolean canTearDown(Wallboard wallboard) {
		int x = wallboard.getX();
		int y = wallboard.getY();
		int[] d = wallboard.getDirection().getDxDyDirection() ;
		int dx = d[0];
		int dy = d[1];
		// borders limit rooms (but for potential doors) and the outside limit of the maze
		if (hasBorder(x, y, dx, dy))
			return false;
		// if there is no border, neighbor should be in legal range of values
		// return true if neighbor has not been visited before
		return isFirstVisit(x+dx, y+dy);
	}
	/**
	 * Checks if the given wall board contributes to a border.
	 * One can think of exterior walls of the maze and its 
	 * rooms as essential for the structure and their wall boards
	 * should not be removed, like a load-bearing wall.
	 * @param wallboard the wall board of interest
	 * @return true is the wall board is part of a border
	 */
	public boolean isPartOfBorder(Wallboard wallboard) {
		int x = wallboard.getX();
		int y = wallboard.getY();
		int[] d = wallboard.getDirection().getDxDyDirection() ;
		int dx = d[0];
		int dy = d[1];
		// borders limit rooms (but for potential doors) 
		// and the outside limit of the maze
		return hasBorder(x, y, dx, dy);
	}
	/**
	 * checks if cell (x,y) has a border in the given direction (dx,dy)
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction towards side
	 * @param dy direction towards side
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 * precondition {@code dx, dy in {-1,0,1}}
	 * @return true if that side is marked as a border, false otherwise
	 */
	private boolean hasBorder(int x, int y, int dx, int dy) {
		return hasMaskedBitsTrue(x, y, (getBoundForBit(getBit(dx, dy))));
	}

	/// Methods that deal with visiting a particular cell //////////////////////////////////
	// life cycle of visited flag
	// stage 1: 0 after instantiation
	// stage 2: 1 after initialization with method initialize()
	// stage 3: 0 after any call to method setCellAsVisited()
	// it is used to differentiate new cells that were not explored from
	// cells that have been visited and explored
	/**
	 * Tells if the given position is visited for the first time.
	 * This is true after cells.initialize() and before setCellAsVisited(x,y).
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return true if (x,y) position is visited for the first time
	 */
	private boolean isFirstVisit(int x, int y) {
		return hasMaskedBitsTrue(x, y, CW_VISITED);
	}
	
	/**
	 * Marks the given cell at position (x,y) as visited
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 */
	public void setCellAsVisited(int x, int y) {
		setBitToZero(x,y,CW_VISITED) ; 
	}
	
	/**
	 * Establish exit position by breaking down wallboard to outside area.
	 * If (x,y) is not located next to an outside wallboard, the method no effect.
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 */
	public void setExitPosition(int x, int y) {
		int bit = 0;
		// Assumption: (0,0) at top-left corner
		// find direction to outside wallboard
		if (x == 0)
			bit = CW_LEFT;
		else if (x == width-1)
			bit = CW_RIGHT;
		else if (y == 0)
			bit = CW_TOP;
		else if (y == height-1)
			bit = CW_BOT;
		else
		{
			dbg("set exit position failed for position " + x + ", " + y);
			return ;
		}
		setBitToZero(x, y, bit);
		//System.out.println("exit position set to zero: " + remotex + " " + remotey + " " + bit + ":" + cells.hasMaskedBitsFalse(remotex, remotey, bit)
		//		+ ", Corner case: " + ((0 == remotex && 0 == remotey) || (0 == remotex &&  height-1 == remotey) || (width-1 == remotex && 0 == remotey) || (width-1 == remotex && height-1 == remotey)));
	}
	/**
	 * Tells if current position is an exit position. 
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @return true if position is on the border and there is no wallboard to the outside, false otherwise
	 */
	public boolean isExitPosition(int x, int y) {
		int bit = 0;
		// Assumption: (0,0) at top-left corner
		// check corner cases since they have two options
		if (x == 0 && y == 0) { // top left
			return hasMaskedBitsFalse(x, y, CW_LEFT) || hasMaskedBitsFalse(x, y, CW_TOP);
		}
		if (x == width-1 && y == 0) { // top right
			return hasMaskedBitsFalse(x, y, CW_RIGHT) || hasMaskedBitsFalse(x, y, CW_TOP);
		}
		if (x == 0 && y == height-1) { // bottom left
			return hasMaskedBitsFalse(x, y, CW_LEFT) || hasMaskedBitsFalse(x, y, CW_BOT);
		}
		if (x == width-1 && y == height-1) { // bottom right
			return hasMaskedBitsFalse(x, y, CW_RIGHT) || hasMaskedBitsFalse(x, y, CW_BOT);
		}
		// check 4 sides
		// find direction to outside wallboard as in method setExitPosition
		if (x == 0)
			bit = CW_LEFT;
		else if (x == width-1)
			bit = CW_RIGHT;
		else if (y == 0)
			bit = CW_TOP;
		else if (y == height-1)
			bit = CW_BOT;
		else
		{
			return false ;
		}
		return hasMaskedBitsFalse(x, y, bit) ;
	}
	// Methods that deal with rooms ///////////////////////////////////
	// lifecycle of room bit
	// stage 1: 0 after instantiation, unchanged in initialization phase
	// stage 2: 1 by calling markAreaAsRoom or by setInRoomToOne
	//
	/**
	 * Sets the InRoom bit to one for a given cell and direction
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 */
	public void setInRoomToOne(int x, int y) {
		setBitToOne(x, y, CW_IN_ROOM);
	}
	/**
	 * Tells if the given position is inside a room.
	 * This is false after cells.initialize() and before calling setInRoomToOne() or markAreaAsRoom().
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @return true if (x,y) position resides in an area marked as a room before, false otherwise
	 */
	public boolean isInRoom(int x, int y) {
		return hasMaskedBitsTrue(x, y, CW_IN_ROOM);
	}
	/**
	 * Checks if there is a cell in the given area that belongs to a room.
	 * The first corner (rx,ry) is at the upper left position, the second corner (rxl,ryl) is at the lower right position.
	 * Precondition {@code 0 <= rx <= rxl < width, 0 <= ry <= ryl < height}
	 * @param rx 1st corner, x coordinate
	 * @param ry 1st corner, y coordinate
	 * @param rxl 2nd corner, x coordinate
	 * @param ryl 2nd corner, y coordinate
	 * @return true if area contains a cell that is already in a room or if it is too close to the border, false otherwise
	 */
	public boolean areaOverlapsWithRoom(int rx, int ry, int rxl, int ryl) {
		// loop start and end are chosen such that there is at least one cell 
		// between area and any existing room or the outside border
		int startX = rx-1 ;
		int startY = ry-1 ;
		int stopX = rxl+1 ;
		int stopY = ryl+1 ;
		// check if room is too close to border
		if (((startX < 0)||(startY < 0))||((stopX >= width)||(stopY >= height)))
			return true ;
		// check area
		for (int x = startX; x <= stopX; x++)
		{
			for (int y = startY; y <= stopY; y++)
			{
				if (isInRoom(x, y))
					return true ;
			}
		}
		return false ;
	}
	/**
	 * Marks a given area as a room on the maze and positions up to five doors randomly.
	 * The first corner is at the upper left position, the second corner is at the lower right position.
	 * Assumes that given area is located on the map and does not intersect with any existing room.
	 * The wallboards of a room are declared as borders to prevent the generation mechanism from tearing them down.
	 * Of course there must be a few walls where doors can be created so the border protection is removed
	 * for .
	 * @param rw room width
	 * @param rh room height
	 * @param rx 1st corner, x coordinate
	 * @param ry 1st corner, y coordinate
	 * @param rxl 2nd corner, x coordinate
	 * @param ryl 2nd corner, y coordinate
	 */
	public void markAreaAsRoom(int rw, int rh, int rx, int ry, int rxl, int ryl) {
		// Step 1: 
		// clear all cells in area of room from all wallboards and borders
		// mark all cells in area as being inside the room
		int x;
		int y;
		for (x = rx; x <= rxl; x++)
			for (y = ry; y <= ryl; y++) { 
				setAllToZero(x, y);
				setInRoomToOne(x, y);
			} 
		// Step 2: set border flag at the perimeter
		// this distinguishes these wallboards as special 
		// and the mazebuilder algorithm can leave them in place
		// if possible
		encloseArea(rx, ry, rxl, ryl);
		// Step 3: prepare for doors
		// we only remove the border flag for some
		// randomly selected wallboards on the perimeter.
		// This creates candidates for doors
		// so a maze generation algorithm can identify
		// these wallboards and delete them to get into the room
		int numBoards = (rw+rh)*2; // counter for the total number of wallboards
		SingleRandom random = SingleRandom.getRandom() ;
		// pick at most 5 wallboards
		for (int ct = 0; ct != 5; ct++) { 
			int door = random.nextIntWithinInterval(0, numBoards-1); // pick a random wallboard
			// calculate position and direction of this candidate wallboard
			int dx, dy;
			if (door < rw*2) {
				y = (door < rw) ? 0 : rh-1;
				dy = (door < rw) ? -1 : 1;
				x = door % rw;
				dx = 0;
			} else {
				door -= rw*2;
				x = (door < rh) ? 0 : rw-1;
				dx = (door < rh) ? -1 : 1;
				y = door % rh;
				dy = 0;
			} 
			// tear down the border protection.
			// The wallboard itself is left
			// for the maze builder to decide to tear it down.
			deleteBorder(x+rx, y+ry, dx, dy);
		}
	}
	/**
	 * Erects wallboards marked as borders on the perimeter of an internal area
	 * to fully enclose area.
	 * Wallboards marked as borders are added from both directions
	 * so this method only applies to areas where adjacent outside
	 * neighboring cells are still inside the maze floorplan.
	 * Rectangular area represent by upper left corner (rx,ry)
	 * and lower right corner (rxl,ryl).
	 * @param rx x-coordinate of upper left corner
	 * @param ry y-coordinate of upper left corner
	 * @param rxl x-coordinate of lower right corner
	 * @param ryl y-coordinate of lower right corner
	 */
	private void encloseArea(int rx, int ry, int rxl, int ryl) {
		// add wallboards all around the area and mark them as border
		// top and bottom
		for (int x = rx; x <= rxl; x++) {
			addBorderWallboard(x, ry, 0, -1);
			addBorderWallboard(x, ryl, 0, 1);
		} 
		// left and right
		for (int y = ry; y <= ryl; y++) {
			addBorderWallboard(rx, y, -1, 0);
			addBorderWallboard(rxl, y, 1, 0);
		}
	}
	////////////////// Methods that deal with wallboards and borders/bounds  ///////////////////////	
	/**
	 * Sets the wallboard bit to zero for a given cell and direction
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dx direction x, in {@code{ -1, 0, 1}} obtained from dirsx[]
	 * @param dy direction y, in {@code{ -1, 0, 1}} obtained from dirsy[]
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 */
	private void setWallToZero(int x, int y, int dx, int dy) {
		setBitToZero(x, y, getBit(dx, dy));
	}
	private void setWallToOne(int x, int y, int dx, int dy) {
		setBitToOne(x, y, getBit(dx, dy));
	}
	/**
	 * Sets the border bit to zero for a given cell and direction
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 */
	private void setBorderToZero(int x, int y, int dx, int dy) {
		int bit = getBit(dx, dy);
		setBitToZero(x,y,getBoundForBit(bit)) ; 
	}
	
	/**
	 * Sets the border and wallboard bit to one for a given cell and direction
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * precondition {@code 0 <= x < width, 0 <= y < height}
	 */
	private void setBorderAndWallToOne(int x, int y, int dx, int dy) {
		int bit = getBit(dx, dy);
		setBitToOne(x, y, (bit | getBoundForBit(bit)));
	}

	/**
	 * Calculates the bitmask for a bound that corresponds to 
	 * a given bitmask for a direction CW_LEFT, CW_RIGHT, etc
	 * @param bit in {CW_LEFT, CW_RIGHT, CW_TOP, CW_BOT}
	 * @return corresponding bitmask to check bound
	 */
	protected int getBoundForBit(int bit) {
		return bit<< CW_BOUND_SHIFT;
	}


	/**
	 * Delete a border/bound between to adjacent cells (x,y) and (x+dx,y+dy).
	 * Only used in markAreaAsRoom. Does not delete wallboard.
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	private void deleteBorder(int x, int y, int dx, int dy) {
		setBorderToZero(x, y, dx, dy);
		setBorderToZero(x+dx, y+dy, -dx, -dy) ;
	}

	/**
	 * Add a wallboard that contributes to a border between to adjacent cells (x,y) and (x+dx,y+dy).
	 * Only used in markAreaAsRoom.
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	private void addBorderWallboard(int x, int y, int dx, int dy) {
		setBorderAndWallToOne(x, y, dx, dy);
		setBorderAndWallToOne(x+dx, y+dy, -dx, -dy);
	}

	/**
	 * Add a wallboard. If internal it is added on both sides 
	 * between two adjacent cells (x,y) and (x+dx,y+dy).
	 * @param wallboard gives the wallboard to add
	 * @param internalWall denotes if wallboard is added on both cells (true) or just for the given cell (false)
	 */
	public void addWallboard(Wallboard wallboard, boolean internalWall) {
		int x = wallboard.getX();
		int y = wallboard.getY();
		int[] d = wallboard.getDirection().getDxDyDirection() ;
		// add wallboard on (x,y)
		setWallToOne(x, y, d[0], d[1]);
		// add same wallboard but for adjacent cell
		if (internalWall)
			setWallToOne(x+d[0], y+d[1], -d[0], -d[1]);
	}

	/**
	 * Delete both wallboards between to adjacent cells (x,y) and (x+dx,y+dy)
	 * to create an opening.
	 * Deleting the wallboard at (x,y) implies that also a wallboard in opposite
	 * direction at (x+dx,y+dy) gets deleted.
	 * Do not use this method to create an exit as the adjacent location
	 * is outside of the legal range of values for cell locations.
	 * @param wallboard provides (x,y) coordinate of cell and direction
	 */
	public void deleteWallboard(Wallboard wallboard) {
		int x = wallboard.getX();
		int y = wallboard.getY();
		int[] d = wallboard.getDirection().getDxDyDirection() ;
		int dx = d[0];
		int dy = d[1];
		// delete wallboard on (x,y)
		setWallToZero(x, y, dx, dy);
		// delete same wallboard but for adjacent cell
		setWallToZero(x+dx, y+dy, -dx, -dy);
		/////////////////// THE FOLLOWING 2 LINES ARE USED FOR GRADING PROJECT 2, DO NOT ALTER OR DELETE /////////////////
		if (deepdebugWall) // for debugging: track sequence of walls that are deleted
			logWall( x,  y,  dx,  dy);
		/////////////////// END OF SPECIAL CODE FOR GRADING //////////////////////////////////////////////////////////////
	}
    /**
     * Add wallboards in either north or west direction for the given wall.
     * Wall coordinates need to be rescaled by map_unit to translate into
     * cell coordinates.
     * Method is only used to track seen cells in the FirstPersonDrawer for
     * the MapDrawer. So achieved properties for cells object differ than for
     * other methods used to represent the maze.
     * @param seg gives the wall whose wallboards need to be added
     * @param map_unit gives the scaling factor to obtain the cell coordinates
     */
    public void addWall(Wall seg, int map_unit) {
        // moved method from FirstPersonDrawer here
        // Why: changes seencells by adding wallboards based on info in given seg
        // only piece of information used from FirstPersonDrawer is map_unit
        
        // we need to obtain the starting position (sx,sy) of the wall
        // and the direction (sdsx,sdsy) in which the wall proceeds
        // all these values are inflated by map_unit and need to be adjusted
        
        // Step 1: get the direction of the wall
        //final int sdx = seg.getExtensionX() / map_unit; // constant, only set once here
        //final int sdy = seg.getExtensionY() / map_unit; // constant, only set once here
        // note: either sdx or sdy is 0
        // define constants to avoid method calls in following loop
        final int sdsx = MazeBuilder.getSign(seg.getExtensionX()); // 0: vertical, -1,1: horizontal
        final int sdsy = MazeBuilder.getSign(seg.getExtensionY()); // 0: horizontal, -1,1: vertical
        
        // Step 2: get initial position (sx,sy) right
        int sx = seg.getStartPositionX() / map_unit;
        if (sdsx < 0) // this direction and negative 
            sx--;
        int sy = seg.getStartPositionY() / map_unit; 
        if (sdsy < 0) // this direction and negative
            sy--;
        
         
        final CardinalDirection cd = (sdsx != 0) ? CardinalDirection.North : CardinalDirection.West ;
        //final int len = Math.abs(sdx + sdy);  
        //if (len != seg.length() / map_unit) {
        //    System.out.print("Error: seg length wrong: " + len + " vs " + seg.length());
        //}
        final int len = seg.getLength() / map_unit;
        
        // check conditions
        // warning: step 2 could range for sx, sy such that starting point may be at -1,
        // but this does not happen
        assert (0 <= sx && sx < width) : "Starting position for x must be in range";
        assert (0 <= sy && sy < height) : "Starting position for y must be in range";
        assert (sdsx != 0 && sdsy == 0) || (sdsx == 0 && sdsy != 0)
            : "Wall needs to extend into exactly one direction";
        // note: step 2 possibly changes range for sx, sy such that end point may be at -1
        // this does happen, for loop below does not go to full length
        assert ((-1 <= sx + sdsx*len) && (sx + sdsx*len <= width)) : "End position for x must be in range";
        assert ((-1 <= sy + sdsy*len) && (sy + sdsy*len <= height)) : "End position for y must be in range";
        
        // Step 3: add wallboard information to this cells object for each wallboard in wall
        Wallboard wallboard = new Wallboard(0,0,CardinalDirection.East) ; // initial values don't matter
        // true loop variables are (sx,sy), a position in the maze  
        //for (int i = 0; i != len; i++) {
        for (int i = 0; i < len; i++) {
            // cd is either NORTH or WEST
            // so we basically add a wallboard NORTH or WEST
            // but only from one side for the given cell (not its neighbor)
            wallboard.setLocationDirection(sx, sy, cd);
            //seencells.addWall(wallboard, false) ;
            addWallboard(wallboard, false) ;
            // move to neighbor cell in the direction of the wall
            // note that exactly one of sdsx or sdsy is not 0
            sx += sdsx;
            sy += sdsy;
        }    
    }
	//////////////////// get methods (is..., has...) for various attributes ///////////////////////
	/**
	 * Tells if the given position has a wallboard in the given direction.
	 * This is true after cells.initialize() and before deleting this wallboard. 
	 * A wallboard can be deleted by directly calling deleteWall() or by removing all wallboards within a room,
	 * method markAreaAsRoom().
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dir gives the cardinal direction
	 * @return true if (x,y) position has wallboard in the given direction
	 */
	public boolean hasWall(int x, int y, CardinalDirection dir) {
		return hasMaskedBitsTrue(x, y, getCWConstantForDirection(dir));
	}
	/**
	 * Tells if the given position has no wallboard in the given direction.
	 * This is true after cells.initialize() and before deleting this wallboard. 
	 * A wallboard can be deleted by directly calling deleteWall() or by removing all wallboards within a room,
	 * method markAreaAsRoom().
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param dir gives the cardinal direction
	 * @return true if (x,y) position has no wallboard in the given direction
	 */
	public boolean hasNoWall(int x, int y, CardinalDirection dir) {
		return !hasMaskedBitsTrue(x, y, getCWConstantForDirection(dir));
	}
	/**
	 * Gives the matching bit encoded value,
	 * the matching CW_TOP, BOT, LEFT, RIGHT constants in Constants.java
	 * for the given direction.
	 * TOP is matched with North, Right is matched with East
	 * @param dir gives the direction.
	 * @return the matching integer value (CW_ constant)
	 */ 
	protected int getCWConstantForDirection(CardinalDirection dir) {
		/* compare with definition for consistency
		static final int CW_TOP = 1;  // 2^0
		static final int CW_BOT = 2;  // 2^1
		static final int CW_LEFT = 4; // 2^2
		static final int CW_RIGHT = 8;// 2^3
		 */
		switch(dir) {
		case North : 
			return CW_TOP; // flipped: CW_TOP ;
		case East : 
			return CW_RIGHT ;
		case South : 
			return CW_BOT; // flipped CW_BOT ;
		case West : 
			return CW_LEFT ;
		default:
			throw new RuntimeException("Unsupported value in enum type") ;
		}
	}
	////////////////// iterator to access continuous sequences of wallboards //////////////////////////////////////////
	public Iterator<int[]> iterator(int x, int y, CardinalDirection cd) {
        return new SequenceIterator(x,y,cd);
    }

    /**
     * Inner class to provide an iterator that delivers tuples of [start,end] indices
     * for the begin and end of a continuous sequence of wallboards.
     *  
     * The class resulted from refactoring the BSPBuilder class.
     * In order to cater to the existing needs, the delivered data is quite particular.
     * 
     * A sequence of wallboards can extend in two directions (horizontal, i.e. along x axis or
     * vertical, i.e., along the y axis) and for each direction wallboards can be on
     * one of the two sides.
     * Vertical sequence: along east or west side of a cell
     * Horizontal sequence: along north or south side of a cell
     * 
     * The iterator goes through increasing values of x if horizontal (y if vertical).
     * 
     * While the start index refers to a cell that has a wallboard and where the sequence 
     * starts (i.e. the cell before has no wallboard), the end index refers
     * to a cell where the sequence of wallboards has ended before (i.e. the cell before
     * has a wallboard but the end index cell has no wallboard).
     * @author pk
     *
     */
    private class SequenceIterator implements
                    Iterator<int[]> {
        private int[] cursor; // current sequence of wallboards
        private int[] next; // next sequence of wallboards
        private int startX; // stores the current position during iteration
        private int startY; // stores the current position during iteration
        private final CardinalDirection cd;

        /**
         * Constructor to obtain an iterator that provides [start,end] value pairs
         * for either x or y coordinates of cells that have a continuous sequence
         * of wallboards on the given side. Note that start is a cell with a wallboard, 
         * end is a cell that is the first with no wallboard after the end of the
         * sequence.
         * If the given direction is North or South, the method looks for 
         * a horizontal sequence of wallboards (increments x, so it walks east).
         * If the given direction is East or West, the method looks for 
         * a vertical sequence of wallboards (increments y).
         * @param x x-coordinate of first cell to consider
         * @param y y-coordinate of first cell to consider
         * @param wallsInThisDirection gives the direction of interest
         */
        public SequenceIterator(int x, int y, CardinalDirection wallsInThisDirection) {
            startX = x;
            startY = y;
            cd = wallsInThisDirection;
            cursor = null;
            // compute first element, necessary to make hasNext operational
            next = computeNextSequence();
        }
        @Override
        public boolean hasNext() {
            return next != null;
        }
        @Override
        public int[] next() {
            if(!this.hasNext()) {
                throw new NoSuchElementException();
            }
            cursor = next;
            next = computeNextSequence();
            return cursor;
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        /**
         * Finds the next sequence of wallboards for the stored direction
         * that starts from the current (startX,startY) position.
         * Method updates either startX or startY. If the given
         * direction is North or South, the method looks for 
         * a horizontal sequence of wallboards (increments x).
         * If the given direction is East or West, the method looks for 
         * a vertical sequence of wallboards (increments y).
         * @return [start,end] pair of either x or y coordinates
         * where start has a wallboard for the given direction and
         * end is the first cell beyond the sequence that does
         * not have a wallboard. 
         */
        private int[] computeNextSequence() {
            int[] result = null;
            // note that startX and startY are used to memo the current position
            // across multiple calls for computeNextSequence
            switch(cd) {
            case North: /* same code as for South */
            case South:
                startX = findBeginningOfHorizontalSequence(startX, startY);
                if (startX == width) // no sequence
                    return null;
                result = new int[2];
                result[0] = startX;
                startX = findEndOfHorizontalSequence(startX, startY);
                result[1] = startX;
                break;
            case East: /* same code as for west */ 
            case West:
                startY = findBeginningOfVerticalSequence(startX, startY);
                if (startY == height) // no sequence
                    return null;
                result = new int[2];
                result[0] = startY;
                startY = findEndOfVerticalSequence(startX, startY);
                result[1] = startY;
                break;
            }
            return result;
        }
        private int findBeginningOfVerticalSequence(final int x, int y) {
            while (y < height && hasNoWall(x, y, cd)) {
                y++;
            } 
            return y;
        }
        private int findBeginningOfHorizontalSequence(int x, final int y) {
            while (x < width && hasNoWall(x, y, cd)) {
                x++;
            } 
            return x;
        }
        private int findEndOfVerticalSequence(final int x, int y) {
            while (hasWall(x, y, cd)) {
                y++;
                // at limit, return
                if (y == height) 
                    break;
                // at a crossing wallboard: as we go down, we hit a ceiling, a corner
                // if the neighbor (x incremented already) has wallboard on the north side
                // same as hasWall(x, y-1, CardinalDirection.South)
                if (hasWall(x, y, CardinalDirection.North))
                    break;
            }
            return y;
        }
        private int findEndOfHorizontalSequence(int x, final int y) {
            while (hasWall(x,y, cd)) {
                x++;
                // at limit, return
                if (x == width)
                    break;
                // at a crossing wallboard: as we go east, we hit a blocking wallboard, 
                // if the neighbor (x incremented already) has wallboard on the west side
                // this is the same as hasWall(x-1,y, CardinalDirection.East)
                if (hasWall(x,y, CardinalDirection.West))
                    break;
            }
            return x;
        }
                    }
	////////////////// low level methods operating on bits and bitmasks //////////////////////////////////////////
	//  long term goal is to make all of these methods private to encapsulate the encoding ///////////////////////
	/**
	 * sets given bit in to zero in given cell
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param cw_bit like CW_LEFT, CW_RIGHT, CW_TOP, CW_BOTTOM
	 */
	private void setBitToZero(int x, int y, int cw_bit) {
		cells[x][y] &= ~cw_bit;
	}
	/**
	 * Sets all wallboard bits to zero for a given cell
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 */
	private void setAllToZero(int x, int y) {
		setBitToZero(x, y, CW_ALL) ;
	}

	//////// replace bitmask access 
	// gets called as mazecells.hasMaskedBitsFalse(px, py, Constants.MASKS[a]) ;
	// update: only used within cells and Junit test cases
	// better to directly change client classes
	//
	protected boolean hasMaskedBitsTrue(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) != 0;
	}
	protected boolean hasMaskedBitsFalse(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) == 0;
	}

	/**
	 * encodes (dx,dy) into a bit pattern for right, left, top, bottom direction
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @return bit pattern, 0 in case of an error
	 */
	private int getBit(int dx, int dy) {
		/*
		int bit = 0;
		switch (dx + dy * 2) {
		case 1:  bit = Constants.CW_RIGHT; break; //  dx=1,  dy=0
		case -1: bit = Constants.CW_LEFT;  break; //  dx=-1, dy=0
		case 2:  bit = Constants.CW_BOT;   break; //  dx=0,  dy=1
		case -2: bit = Constants.CW_TOP;   break; //  dx=0,  dy=-1
		default: dbg("getBit problem "+dx+" "+dy); break;
		}
		return bit;
		*/
		// changed to reduce chances for inconsistent mappings
		CardinalDirection dir = CardinalDirection.getDirection(dx, dy);
		return getCWConstantForDirection(dir);
	}
	/**
	 * Sets bits to 1 for given bitmask
	 * @param x coordinate of cell, {@code 0 <= x < width}
	 * @param y coordinate of cell, {@code 0 <= y < height}
	 * @param bitmask is the bitmask to use for adding 1's
	 */
	private void setBitToOne(int x, int y, int bitmask) {
		cells[x][y] |= bitmask ;
	}

	///////////////// code for debugging ///////////////////////////////////////
	private void dbg(String str) {
		System.out.println("Cells: "+str);
	}
	/**
	 * Methods dumps internal data into a string, intended usage is for debugging purposes. 
	 * Maze is represent as a matrix of integer values.
	 */
	public String toString() {
		String s = "" ;
		String prefix = null;
		for (int i = 0 ; i < width ; i++)
		{
			prefix = " i:" + i + " j:";
			for (int j = 0 ; j < height ; j++)
				s += prefix + j + "=" + cells[i][j] ;
			s += "\n" ;
		}
		return s ;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// THE FOLLOWING CODE IS USED FOR GRADING PROJECT 2 ///////////////////////////////
	//////////////////////////// DO NOT ALTER THE CODE BELOW ////////////////////////////////////////////////////
	// flag to trigger that a log is constructed that lists the sequence of wallboards that are deleted
	public static boolean deepdebugWall = false;
	public static final String deepedebugWallFileName = "logDeletedWalls.txt" ;
	StringBuffer traceWall = (deepdebugWall) ? new StringBuffer("x  y  dx  dy\n") : null ;

	/**
	 * Append wallboard information to logging data. Currently used to log the sequence of wallboards that are deleted in the maze generation phase
	 * @param x current position, x coordinate
	 * @param y current position, y coordinate
	 * @param dx direction, x coordinate, {@code -1 <= dx <= 1}
	 * @param dy direction, y coordinate, {@code -1 <= dy <= 1}
	 */
	private void logWall(int x, int y, int dx, int dy) {
		if (null != traceWall)
		{
			traceWall.append(x + " " + y + " " + dx + " " + dy + "\n");
		}
	}
	/**
	 * Write log data to given file
	 * @param filename is a valid filename
	 */
	public void saveLogFile( String filename )
	{
		try {  
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename));  
	        out.write(traceWall.toString());   
	        out.close(); 
        } catch (Exception e) {  
            e.printStackTrace();
        }  
	}


}