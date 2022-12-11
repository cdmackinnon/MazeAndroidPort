package com.example.amazebyconnormackinnon.generation;

import java.util.logging.Logger;

import com.example.amazebyconnormackinnon.gui.Constants;


/**
 * This class has the responsibility to create a maze of given dimensions (width, height) 
 * together with a solution based on a distance matrix.
 * The MazeBuilder implements Runnable such that it can be run a separate thread.
 * The MazeFactory has a MazeBuilder and handles the thread management.   
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class MazeBuilder implements Runnable {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(MazeBuilder.class.getName());

	// Given input information: 
	protected int width, height ; 	// width and height of maze, 
	private int rooms; 		// requested number of rooms in maze, a room is an area with no walls and larger than a single cell
	private int expectedPartiters; 	// user given limit for partiters
	
	// Produced output information to create the new maze
	// root, cells, dists, startx, starty
	protected int startx ; // starting position inside maze for entity to search for exit
	protected int starty;
	// conventional encoding of maze as a 2 dimensional integer array encapsulated in the Floorplan class
	// a single integer entry can hold information on wallboards, borders/bounds
	protected Floorplan floorplan; // the internal representation of a maze layout
	protected Distance dists ; // distance matrix that stores how far each position is away from the exit position
	// floorplan and dists internally operate 2d arrays with same dimension and same indexing, i.e., dists(i,j) gives
	// the distance to exit for a cell at position (i,j) in the floor plan.

	// class internal local variables
	protected SingleRandom random ; // random number stream, used to make randomized decisions, e.g for direction to go
	Order order; // describes what is wanted, e.g. a perfect maze or not
	
	// constants
	static final long SLEEP_INTERVAL = 100 ; //constant used for brief breaks to recognize interrupted signal, unit is millisecond
	static final int MAX_TRIES = 250 ; // room generation: max number of tries to find a random location for a room
	static final int MIN_ROOM_DIMENSION = 3; // room generation: min dimension
	static final int MAX_ROOM_DIMENSION = 8; // room generation: max dimension

	/**
	 * Constructor for a randomized maze generation
	 */
	public MazeBuilder(){
	}

	/**
	 * Sets internal fields according to given order
	 * @param order provides the spec for the maze generation from
	 */
	public void buildOrder(Order order) {
		this.order = order;
		// configure and obtain the random number generator
		SingleRandom.setSeed(order.getSeed());
		random = SingleRandom.getRandom();
		// derive parameters 
		int skill = order.getSkillLevel() ;
		width = Constants.SKILL_X[skill];
		height = Constants.SKILL_Y[skill];
		// only algorithm without rooms guarantees a perfect maze
		// rooms can result in loops, so for a perfect maze, set room number to 0
		rooms = order.isPerfect() ? 0 : Constants.SKILL_ROOMS[skill];
		expectedPartiters = Constants.SKILL_PARTCT[skill];
		// instantiate data structures
		floorplan = new Floorplan(width,height) ;
		dists = new Distance(width,height) ;
	}
	/**
	 * Main method to run construction of a new maze in a thread of its own.
	 * This method is called by the MazeFactory to generate a maze.
	 */
	public void run() {
		// try-catch block to recognize if thread is interrupted
		try {
			// create an initial invalid maze where all wallboards and borders are up
			floorplan.initialize();
			// place rooms in maze as needed
			if (rooms > 0)
				generateRooms();
			
			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop

			// put pathways into the maze, determine its starting and end position and calculate distances
			generate();

			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop

			final int colchange = random.nextIntWithinInterval(0, 255); // used in the constructor for Segments  class Seg
			final BSPBuilder b = new BSPBuilder(order, dists, floorplan, width, height, colchange, expectedPartiters) ;
			BSPNode root = b.generateBSPNodes(); // takes a long time, updates progressbar, 
			// it also internally checks for cancel requests
			// and throws an interrupted exception if that happens

			Thread.sleep(SLEEP_INTERVAL) ; // test if thread has been interrupted, i.e. notified to stop

			// communicate results back to Controller
			order.updateProgress(100); // Order interface promises to communicate 100% upon delivery
			order.deliver(new MazeContainer(width, height, floorplan, dists, root, startx, starty));
            // reset order and other fields for safe repeated operation and garbage collection
			reset() ;
		}
		catch (InterruptedException ex) {
			// if user cancels a lengthy maze generation, we need to stop and 
			// clean up internal data structures
			// exception mechanism is basically used to exit method in a controlled way
			// 
			LOGGER.fine("Catching signal to stop") ;
			// reset order and other fields for safe repeated operation and garbage collection
			reset();
		}
	}
	
	/**
	 * Reset all fields to initial values
	 */
	private void reset() {
		width = 0 ;
		height = 0 ;
		rooms = 0 ;
		expectedPartiters = 0 ;
		startx = 0 ;
		starty = 0 ;
		floorplan = null ;
		dists = null ;
		// leave random number generator as is
		order = null ;
	}
	
	
	
	/**
	 * Generate all rooms in a given maze where initially all wallboards are up. Rooms are placed randomly and of random sizes
	 * such that the maze can turn out to be too small to accommodate the requested number of rooms (class attribute rooms). 
	 * In that case less rooms are produced.
	 * @return generated number of rooms
	 */
	protected int generateRooms() {
		// Rooms are randomly positioned such that it may be impossible to place the all rooms if the maze is too small
		// to prevent an infinite loop we limit the number of failed to MAX_TRIES == 250
		int tries = 0 ;
		int result = 0 ;
		while (tries < MAX_TRIES && result < rooms) {
			if (placeRoom())
				result++ ;
			else
				tries++ ;
		}
		return result ;
	}
	
	
	/**
	 * Allocates space for a room of random dimensions in the maze.
	 * The position of the room is chosen randomly. The method is not sophisticated 
	 * such that the attempt may fail even if the maze has ample space to accommodate 
	 * a room of the chosen size. 
	 * @return true if room is successfully placed, false otherwise
	 */
	private boolean placeRoom() {
		// get width and height of random size that are not too large
		// if too large return as a failed attempt
		final int rw = random.nextIntWithinInterval(MIN_ROOM_DIMENSION, MAX_ROOM_DIMENSION);
		if (rw >= width-4)
			return false;

		final int rh = random.nextIntWithinInterval(MIN_ROOM_DIMENSION, MAX_ROOM_DIMENSION);
		if (rh >= height-4)
			return false;
		
		// proceed for a given width and height
		// obtain a random position (rx,ry) such that room is located on as a rectangle with (rx,ry) and (rxl,ryl) as corner points
		// upper bound is chosen such that width and height of room fits maze area.
		final int rx = random.nextIntWithinInterval(1, width-rw-1);
		final int ry = random.nextIntWithinInterval(1, height-rh-1);
		final int rxl = rx+rw-1;
		final int ryl = ry+rh-1;
		// check all cells in this area if they already belong to a room
		// if this is the case, return false for a failed attempt
		if (floorplan.areaOverlapsWithRoom(rx, ry, rxl, ryl))
			return false ;
		// since the area is available, mark it for this room and remove all wallboards
		// from this on it is clear that we can place the room on the maze
		floorplan.markAreaAsRoom(rw, rh, rx, ry, rxl, ryl); 
		return true;
	}


	
	/**
	 * This method generates a maze.
	 * It computes distances, determines a start and exit position that are as far apart as possible. 
	 */
	protected void generate() {
		// generate paths in cells such that there is one strongly connected component
		// i.e. between any two cells in the maze there is a path to get from one to the other
		// the search algorithms starts at some random point
		generatePathways(); 

		final int[] remote = dists.computeDistances(floorplan) ;

		// identify cell with the greatest distance
		final int[] pos = dists.getStartPosition();
		startx = pos[0] ;
		starty = pos[1] ;

		// make exit position at true exit in the cells data structure
		floorplan.setExitPosition(remote[0], remote[1]);
	}
	/**
	 * This method generates pathways into the maze.
	 * Comments have been removed to make students 
	 * recognize the difference between readable and unreadable code
	 * and what kind of comments are needed to understand code.
	 * A previous version is kept below and commented out which is 
	 * even worse than this one.
	 */
	protected void generatePathways() {
		int x = random.nextIntWithinInterval(0, width-1);
		int y = 0; 
		final int firstx = x; 
		final int firsty = y;
		
		CardinalDirection[][] origcds = new CardinalDirection[width][height]; 
		CardinalDirection cd = CardinalDirection.East;
		CardinalDirection origcd = cd;
		
		floorplan.setCellAsVisited(x, y); 
		Wallboard wallboard = new Wallboard(x, y, cd);
		while (true) { 
			wallboard.setLocationDirection(x, y, cd);
			if (!floorplan.canTearDown(wallboard)) {
				cd = cd.rotateClockwise();
				if (origcd == cd) {				
					if (x == firstx && y == firsty)
						break; 			
					int[] dxy = origcds[x][y].getDxDyDirection();
					x -= dxy[0];
					y -= dxy[1];
					if (null == origcds[x][y]) {
						// Happens at starting position
						assert (x == firstx && y == firsty) : "catching null elsewhere than starting position" ;
						cd = cd.randomDirection() ;
					}
					else
						cd = origcds[x][y] ;
					cd = cd.rotateClockwise();
					origcd = cd;
				}
			} else {
				floorplan.deleteWallboard(wallboard);
				int[] dxy = cd.getDxDyDirection();
				x += dxy[0];
				y += dxy[1];
				floorplan.setCellAsVisited(x, y);
				origcds[x][y] = cd;
				cd = cd.randomDirection();
				origcd = cd;
			}
		}
	}
	/* original, kept  for discussion in class !!!!
	protected void generatePathways() {
		int[][] origdirs = new int[width][height] ; 
		int x = random.nextIntWithinInterval(0, width-1) ;
		int y = 0; 
		final int firstx = x ; 
		final int firsty = y ;
		int dir = 0; 	 	
		int origdir = dir; 	
		cells.setCellAsVisited(x, y); 
		while (true) { 		
			int dx = Constants.DIRS_X[dir];
			int dy = Constants.DIRS_Y[dir];
			if (!cells.canGo(x, y, dx, dy)) { 
				dir = (dir+1) & 3; 
				if (origdir == dir) { 
					if (x == firstx && y == firsty)
						break; 
					int odr = origdirs[x][y];
					dx = Constants.DIRS_X[odr];
					dy = Constants.DIRS_Y[odr];
					x -= dx;
					y -= dy;
					origdir = dir = random.nextIntWithinInterval(0, 3);
				}
			} else {
				cells.deleteWall(x, y, dx, dy);
				x += dx;
				y += dy;
				cells.setCellAsVisited(x, y);
				origdirs[x][y] = dir;
				origdir = dir = random.nextIntWithinInterval(0, 3);
			}
		}
	}
	*/
	/**
	 * Provides the sign of a given integer number
	 * @param num is the integer whose sign value is determined
	 * @return {@code -1 if num < 0, 0 if num == 0, 1 if num > 0}
	 */
	public static int getSign(int num) {
		return (num < 0) ? -1 : (num > 0) ? 1 : 0;
	}
}
