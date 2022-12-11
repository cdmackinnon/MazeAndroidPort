package com.example.amazebyconnormackinnon.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

//import gui.Constants;
import com.example.amazebyconnormackinnon.gui.Constants;

/**
 * This class has the responsibility to obtain the tree of BSP nodes for a given maze.
 * BSP stands for binary space partitioning. 
 * See https://en.wikipedia.org/wiki/Binary_space_partitioning for some details.
 * 
 * This code is refactored code from MazeBuilder.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 *
 */
public class BSPBuilder {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(BSPBuilder.class.getName());

	private final int width ; 				// width of maze
	private final int height ; 				// height of maze
	private final Distance dists ; 			// distance matrix
	private final Floorplan floorplan ;		// floorplan with maze layout
	private final int colchange ;			// comes from a random number, purpose unclear, 
	// colchange: reason for randomization unclear, used to determine color of wall 
	private final int expectedPartiters ; 	// comes from Constants partct array, entry chosen according to skill level
	// only usage is in updateProgressBar to estimate progress made in the BSP tree construction
	int partiters = 0 ; // relocated from MazeBuilder attribute partiters here. 
	private final Order order ; 		// current order
	/**
	 * Constructor
	 * @param order provides the spec for the maze to be generated, will receive the result
	 * @param dists the distances to the exit
	 * @param floorplan the floorplan
	 * @param width the width of the maze
	 * @param height the height of the maze
	 * @param colchange
	 * @param expectedPartiters the expected number of partition iterations
	 */
	public BSPBuilder(Order order, Distance dists, Floorplan floorplan, int width, int height, int colchange, int expectedPartiters) {
		this.order = order ;
		this.dists = dists ;
		this.floorplan = floorplan ;
		this.width = width ;
		this.height = height ;
		this.colchange = colchange ;
		this.expectedPartiters = expectedPartiters ;

		partiters = 0 ; // counter for keeping track of progress made in BSP calculation, starts at 0
	}

	/**
	 * Create tree of BSP nodes for a given list of walls.
	 * The binary space partitioning algorithm is recursive.
	 * General idea at each level of recursion: 
	 * pick one wall, split all other walls into two lists. 
	 * One list contains all walls that are in front of selected wall,
	 * the other list contains all walls that are behind the selected wall.
	 * Walls that intersect are split into two walls accordingly; the parts
	 * are added to the corresponding lists. 
	 * In theory, polygons can lie in the plane of the selected wall such
	 * that a BSP node would hold on to a list of polygons. This is not 
	 * possible in this maze application.
	 * The code names lists rather left and right to match the terminology of trees
	 * rather than front and back which would resemble terminology for drawing.
	 * The code selects the wall that has the minimum grade value as the one 
	 * for partitioning.
	 * If all the walls in one node are partitioned, it will stop to split.
	 * @param walls the list of walls (polygons) to partition
	 * @return root node for BSP tree
	 * @throws InterruptedException if its executing thread is interrupted
	 */
	private BSPNode genNodes(List<Wall> walls) throws InterruptedException {
		// Recursion anchor:
		// if there is no wall with a partition bit set to false, 
		// there is nothing else to do and we are at a leaf node
		if (countNonPartitions(walls) == 0)
			return new BSPLeaf(walls);
		// Step: pick the wall that is used to partition all others into left and right
		// Criterion: from the ones that have a partition bit set to false, 
		// pick a candidate with a low grade
		// Note: the splitter remains an element of the walls list but is marked as partitioned
		Wall splitter = findSplitter(walls);
		splitter.setPartition(true);
		
		// Step: split all walls into two lists
		// left the resulting list of walls for the left side of the subtree
		// right the resulting list of walls for the right side of the subtree
		final ArrayList<Wall> left = new ArrayList<>();
		final ArrayList<Wall> right = new ArrayList<>();
		splitter.splitWalls(walls, left, right, colchange);
		
		// Recursion anchor
		// Case: from the current node, the tree has only 1 branch.
		// Note: the splitter is in one of the 2 lists, so if one list is empty,
		// The current node is a leaf and 
		// we just terminate the recursion with the non-empty list.
		if (left.isEmpty())
			return new BSPLeaf(right);
		if (right.isEmpty())
			return new BSPLeaf(left);
		
		// Case: two sided recursion, need to create a node
		// and recursively calculate subtrees for both sides.
		return new BSPBranch(splitter.getStartPositionX(), splitter.getStartPositionY(), 
				splitter.getExtensionX(), splitter.getExtensionY(), 
				genNodes(left), genNodes(right)); 
	}
	

    /**
	 * Counts how many elements in the wall vector have their partition bit set to false
	 * @param walls all walls
	 * @return number of walls where the partition flag is not set
	 */
	private static int countNonPartitions(List<Wall> walls) {
		int result = 0 ;
		for (Wall wall: walls) {
			if (!wall.isPartition())
				result++;
		}
		return result;
	}

	/**
	 * It finds the wall which has the minimum grade value.
	 * @param walls list of walls, remains unchanged
	 * @return wall that is best candidate according to grade partition (smallest grade)
	 * @throws InterruptedException if its executing thread is interrupted
	 */
	private Wall findSplitter(List<Wall> walls) throws InterruptedException {
		Wall result = null ;
		int bestgrade = 5000; // used to compute the minimum of all observed grade values, set to some high initial value
		final int maxtries = 50; // constant, only used to determine skip
		// consider a subset of walls proportional to the number of tries, here 50, seems to randomize the access a bit
		int skip = (walls.size() / maxtries);
		if (skip == 0)
			skip = 1;
		assert 0 < skip : "Increment for loop must be positive";
		for (int i = 0; i < walls.size(); i += skip) {
			Wall wall = walls.get(i);
			// skip walls where the partition flag was set
			if (wall.isPartition())
				continue;
			// provide feedback for progress bar every 32 iterations
			partiters++;
			if ((partiters & 31) == 0) {
				updateProgressBar(partiters); // side effect: update progress bar
			}
			// check grade and keep track of minimum
			int grade = wall.calculateGrade(walls);
			if (grade < bestgrade) {
				bestgrade = grade;
				result = wall; // determine wall with smallest grade
			}
		}
		return result;
	}

	/**
	 * Push information on progress into maze such that UI can update progress bar.
	 * The published progress value may reach 100 before the actual maze generation is complete but it will never exceed 100.
	 * @param partiters counting partition iterations (precise semantics obscure)
	 * @throws InterruptedException if executing thread is interrupted
	 */
	private void updateProgressBar(int partiters) throws InterruptedException {
		// During maze generation, the most time consuming part needs to occasionally update the current screen
		// 
		if (null != order) {
			// the current level of progress is estimated
			// the expectedPartiters value is sometimes too low
			// so it is necessary to adjust the max value that is placed into the progress bar
			// this also means that 100 does not mean the generation is complete
			int percentage = partiters*100/expectedPartiters ;
			if (percentage > 100) {
				LOGGER.warning("Progress estimate exceeds 100, set to 100 to mitigate issue");
				percentage = 100;
			}
			//  update progress bar
			order.updateProgress(percentage) ;
			// give main thread a chance to process keyboard events
			if (percentage < 100) {
				Thread.sleep(10);
			}
		}
	}

	/**
	 * Set the partition bit to true for walls on the border and where the direction is 0
	 * @param walls the list of walls to consider
	 */
	private void setPartitionBitForCertainWalls(List<Wall> walls) {
	    for (Wall wall : walls) {
			wall.updatePartitionIfBorderCase(width*Constants.MAP_UNIT, height*Constants.MAP_UNIT);
		}
	}




	/**
	 * Generate tree of BSP nodes for a given maze.
	 * We use the binary space partitioning algorithm to compute a BSP tree.
	 * The method is recursive and operates on a list of polygons.
	 * Here each wall, i.e. a continuous sequence of wallboards, forms
	 * such a polygon.  
	 * @return the root node for the BSP tree
	 * @throws InterruptedException if executing thread is interrupted
	 */
	public BSPNode generateBSPNodes() throws InterruptedException {
		// Binary space partitioning operates on polygons (here: walls)
		// the floorplan only lists wallboards.
		// We need to determine walls, i.e. wallboards over multiple cells in
		// a vertical or horizontal direction.
		ListOfWallsBuilder builder = new ListOfWallsBuilder(width, height, floorplan, dists, colchange);
		List<Wall> walls = builder.generateWalls(); 

		// The size and balance of the resulting BSP tree depends on 
		// which polygons are selected for the partitioning.
		// Hypothesis: the partition bit is used for this decision 
		// Observation: partition bit true means that that polygon 
		// is not considered any further for node generation
		setPartitionBitForCertainWalls(walls); 

		// TODO: check why this is done. 
		// It creates a top wallboard on position (0,0). 
		// This may even corrupt a maze and block its exit!
		floorplan.addWallboard(new Wallboard(0, 0, CardinalDirection.North), false);
		
		// Start the recursive BSP calculation for the list of polygons
		// and return the root node of the tree
		return genNodes(walls); 
	}
}
