package com.example.amazebyconnormackinnon.generation;

import java.util.ArrayList;
import java.util.logging.Logger;


/**
  * This class has the responsibility to create a maze of given dimensions (width, height) 
 * together with a solution based on a distance matrix.
 * The MazeBuilder implements Runnable such that it can be run a separate thread.
 * The MazeFactory has a MazeBuilder and handles the thread management.   

 * 
 * The maze is built with a randomized version of Prim's algorithm. 
 * This means a spanning tree is expanded into a set of cells by removing wallboards from the maze.
 * Algorithm leaves wallboards in tact that carry the border flag.
 * Borders are used to keep the outside surrounding of the maze enclosed and 
 * to make sure that rooms retain outside walls and do not end up as open stalls. 
 *   
 * @author Jones.Andrew, refactored by Peter Kemper
 */

public class MazeBuilderPrim extends MazeBuilder implements Runnable {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(MazeBuilderPrim.class.getName());

	public MazeBuilderPrim() {
		super();
		LOGGER.config("Using Prim's algorithm to generate maze.");
	}

	/**
	 * This method generates pathways into the maze by using Prim's algorithm to generate a spanning tree for an undirected graph.
	 * The cells are the nodes of the graph and the spanning tree. An edge represents that one can move from one cell to an adjacent cell.
	 * So an edge implies that its nodes are adjacent cells in the maze and that there is no wallboard separating these cells in the maze. 
	 */
	@Override
	protected void generatePathways() {
		/*
		 * The main idea of Prim's is to grow a single minimal spanning tree (MST).
		 * This means a cell is either part of the one and only MST considered or not.
		 * A boolean encoding of that status is sufficient. 
		 * We encode the spanning tree with the help of the "visited" flag on the floorplan, i.e.
		 * a cell (x,y) is marked as visited if and only if it is part of the MST.
		 * When we add a new cell (x,y) to the MST, we add all of its wallboards that one could 
		 * tear down to expand the MST to the list of candidates for such consideration.
		 * CanTearDown is true iff
		 * - the adjacent cell is not marked
		 * - the wall between (x,y) and that neighbor is not a border wall 
		 */
		
		// Initialization: an MST of 1 cell with a non-empty set of wallboards
		// around it that we could tear down to expand the MST
		final ArrayList<Wallboard> candidates = initMST();
		assert(!candidates.isEmpty());
		
		// Expanding the MST till all cells are connected.
		// Since each newly added cell contributes its wallboards that face
		// towards adjacent cells that are not part of the MST yet, the
		// list of candidate wallboards is the frontier into the set of
		// cells that are not connected yet. It is like a perimeter of
		// wallboards.
		//
		// How does this handle rooms, as the algorithm seems to break into rooms, 
		// but for the lack of wallboards it will not expand through cells that
		// are in the middle of the room and have no adjacent wallboards at all.
		// Of course, these cells are naturally connected to cells adjacent to 
		// cells all other cells inside the room. So it is sufficient if at 
		// least one cell in the room is connected with the MST.
		// This will happen at wallboards that are potential doors, i.e. not 
		// marked as borderwalls for a room. The algorith will eventually reach 
		// cell at a door outside of the room and recognize that the adjacent cell
		// inside the room has not been visited yet and this will add the wallboard
		// to the list of candidates.
		// Implication: 
		// After termination, many cells will be marked as visited, but some cells
		// inside a room may not be marked as such although they belong to the MST.
		//
		Wallboard curWallboard;
		// We need to consider each candidate wallboard and consider it only once
		while(!candidates.isEmpty()){
			// in order to have a randomized algorithm,
			// we randomly select and extract a wallboard from our candidate set
			// this also reduces the set to make sure we terminate the loop
			curWallboard = extractWallboardFromCandidateSetRandomly(candidates);
			// check if wallboard leads to a new cell that is not connected to the spanning tree yet
			if (floorplan.canTearDown(curWallboard))
			{
				// delete wallboard from maze, note that this takes place from both directions
				floorplan.deleteWallboard(curWallboard);
				// add the adjacent cell to the MST and update the list of candidates		
				addCellToMST(curWallboard.getNeighborX(), curWallboard.getNeighborY(), candidates);
				
				// note that each wallboard can get added to the list of candidates at most once. 
				// This is important for termination and efficiency
			}
			// else: just ignore the wallboard, move on to next one
		}
		// when we run out of candidates, we can't expand our MST any further for lack 
		// of wallboards that we are allowed to tear down and that would lead to a cell
		// that is not part of the MST yet.
		// So this must be it.
	}
	/**
	 * Initialize the MST by randomly selecting a cell as the initial, incomplete MST and populate the list 
	 * of candidate wallboards that could be removed to expand the tree.
	 * @return a non-empty list of candidate wallboards to expand the MST
	 */
	private ArrayList<Wallboard> initMST() {
		final ArrayList<Wallboard> result = new ArrayList<Wallboard>();
		
		
		// create an initial list of all wallboards that could be removed
		// for a randomly picked initial cell that should belong to the MST
		// those wallboards lead to adjacent cells that are not part of the spanning tree yet.
		int startX = random.nextIntWithinInterval(0, width-1);
		int startY = random.nextIntWithinInterval(0, height-1);
		// decided to avoid selecting a starting cell inside the middle of a room
		// to make sure we end up with a non-empty list of wallboard candidates to begin with
		// MEMO: the code is overly conservative, it would be ok if the selected cell is inside
		// a room at a location where it has at least one adjacent wallboard that could be removed
		while (floorplan.isInRoom(startX, startY)) {
			// randomly pick another one
			startX = random.nextIntWithinInterval(0, width-1);
			startY = random.nextIntWithinInterval(0, height-1);

		}
		// so we settled on the starting cell
		
		// we need to mark the cell as being part of the MST and add its wallboards to the list
		addCellToMST(startX, startY, result);
		
		return result;
	}

	/**
	 * Add the given cell (x,y) to the MST by marking it as visited and add its wallboards
	 * that lead to cells outside of the MST to the list of candidates (unless they are borderwalls).
	 * @param x the x coordinate of interest
	 * @param y the y coordinate of interest
	 * @param candidates the new elements should be added to, must not be null
	 */
	protected void addCellToMST(int x, int y, final ArrayList<Wallboard> candidates) {
		floorplan.setCellAsVisited(x, y); // the flag is never reset, so this ensure we never go to (x,y) again
		updateListOfWallboards(x, y, candidates); // checks to see if it has wallboards to new cells, if it does it adds them to the list
	}
	/**
	 * Pick a random position in the list of candidates, remove the candidate from the list and return it
	 * @param candidates is the list of candidates to randomly remove a wall board from
	 * @return candidate from the list, randomly chosen
	 */
	private Wallboard extractWallboardFromCandidateSetRandomly(final ArrayList<Wallboard> candidates) {
		return candidates.remove(random.nextIntWithinInterval(0, candidates.size()-1)); 
	}
	

	/**
	 * Updates a list of all wallboards that could be removed from the maze based on wallboards towards new cells.
	 * For the given x, y coordinates, one checks all four directions
	 * and for the ones where one can tear down a wallboard, a 
	 * corresponding wallboard is added to the list of wallboards.
	 * @param x the x coordinate of interest
	 * @param y the y coordinate of interest
	 * @param wallboards the new elements should be added to, must not be null
	 */
	private void updateListOfWallboards(int x, int y, ArrayList<Wallboard> wallboards) {
		if (reusedWallboard == null) {
			reusedWallboard = new Wallboard(x, y, CardinalDirection.East) ;
		}
		for (CardinalDirection cd : CardinalDirection.values()) {
			reusedWallboard.setLocationDirection(x, y, cd);
			if (floorplan.canTearDown(reusedWallboard)) // 
			{
				wallboards.add(new Wallboard(x, y, cd));
			}
		}
	}
	// exclusively used in updateListOfWallboards
	Wallboard reusedWallboard; // reuse a wallboard in updateListOfWallboards to avoid repeated object instantiation

}