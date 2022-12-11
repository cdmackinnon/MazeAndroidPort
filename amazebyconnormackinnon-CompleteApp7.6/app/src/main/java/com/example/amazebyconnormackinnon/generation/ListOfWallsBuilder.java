package com.example.amazebyconnormackinnon.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.amazebyconnormackinnon.gui.Constants;

/**
 * This class creates a list of walls for a floorplan.
 * The BSP tree operates on walls (polygons) while the floorplan
 * merely works with wallboards. The code in this class
 * helps bridging this gap for the BSPBuilder.
 * 
 * History: class resulted from refactoring the BSPBuilder class. 
 * The generation of walls is a substantial amount of code
 * that is not part of the core responsibility of the BSPBuilder.
 *  
 * @author Peter Kemper
 *
 */
public class ListOfWallsBuilder {
	private final int width ; 				// width of maze
	private final int height ; 				// height of maze
	private final Distance dists ; 			// distance matrix
	private final Floorplan floorplan ;		// floorplan with maze layout
	private int colchange;
	
	/**
	 * Constructor
	 * @param width the width of the maze
	 * @param height the height of the maze
	 * @param floorplan the floorplan with the location of wallboards
	 * @param dists the distances to the exit
	 * @param colchange 
	 */
	public ListOfWallsBuilder(int width, int height, Floorplan floorplan, Distance dists, int colchange) {
		this.floorplan = floorplan;
		this.width = width;
		this.height = height;
		this.dists = dists;
		this.colchange = colchange;
	}
	/**
	 * Identifies continuous sequences of wallboards on the maze and fills the wall list 
	 * @return vector of walls
	 */
	public List<Wall> generateWalls() {
		ArrayList<Wall> result = new ArrayList<>();
		generateWallsForHorizontalWallboards(result); 
		generateWallsForVerticalWallboards(result);
		// starting positions for walls seem to be chosen such that walls represent top or left wallboards
		return result ;
	}

	/**
     * Identify continuous sequences of wallboards in a vertical direction
     * @param sl the list of walls that carries the result of the computation
     */
	   private void generateWallsForVerticalWallboards(ArrayList<Wall> sl) {
	        int x;
	        int y;
	        Iterator<int[]> it;
	        int[] cur;
	        // we search for vertical wallboards, so for each row
	        for (x = 0; x < width; x++) {
	            it = floorplan.iterator(x, 0, CardinalDirection.West);
	            while(it.hasNext()) {
	                cur = it.next();
	                int starty = cur[0];
	                y = cur[1];
	                // create wall with (x,starty) being the actual start position of the wall, 
                    // y-starty being the positive length
                    sl.add(new Wall(x*Constants.MAP_UNIT, starty*Constants.MAP_UNIT,
                            0, (y-starty)*Constants.MAP_UNIT, dists.getDistanceValue(x, starty), colchange));
	            }
	            
	            it = floorplan.iterator(x, 0, CardinalDirection.East);
                while(it.hasNext()) {
                    cur = it.next();
                    int starty = cur[0];
                    y = cur[1];
                    // create wall with (x+1,y) being being one off in both directions from the last cell in this wall, starty-y being the negative length
                    // since we are looking at right wallboards, one off in the right direction (x+1) are then cells that have this wall on its left hand side
                    // for some reason the end position is used as a starting position and therefore the length & direction is inverse 
                    sl.add(new Wall((x+1)*Constants.MAP_UNIT, y*Constants.MAP_UNIT,
                            0, (starty-y)*Constants.MAP_UNIT, dists.getDistanceValue(x, starty), colchange));
                }
	        }
	    }
	/**
     * Identify continuous sequences of wallboards in a horizontal direction
     * @param sl  the list of walls that carries the result of the computation
     */
    private void generateWallsForHorizontalWallboards(ArrayList<Wall> sl) {
        int x;
        int y;
        Iterator<int[]> it;
        int[] cur;
        // we search for horizontal wallboards, so for each column
        for (y = 0; y < height; y++) {
            // first round through rows
            it = floorplan.iterator(0,y, CardinalDirection.North);
            while(it.hasNext()) {
                cur = it.next();
                int startx = cur[0];
                x = cur[1];
                // create wall with (x,y) being the end positions, startx-x being the negative length
                // note the (x,y) is not part of the wall
                sl.add(new Wall(x*Constants.MAP_UNIT, y*Constants.MAP_UNIT,
                        (startx-x)*Constants.MAP_UNIT, 0, dists.getDistanceValue(startx, y), colchange));
            }
            // second round through rows, same for bottom wallboards
            it = floorplan.iterator(0,y, CardinalDirection.South);
            while(it.hasNext()) {
                cur = it.next();
                int startx = cur[0];
                x = cur[1];
                // create wall with (startx,y+1) being one below the start position, x-startx being the positive length
                // so this may represent a wallboard at the bottom of the wall as the top wallboard one below
                sl.add(new Wall(startx*Constants.MAP_UNIT, (y+1)*Constants.MAP_UNIT,
                        (x-startx)*Constants.MAP_UNIT, 0, dists.getDistanceValue(startx, y), colchange));
            }
        }
    }
}
