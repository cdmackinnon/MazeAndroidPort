package com.example.amazebyconnormackinnon.generation;

//import java.awt.Point; Replaced awt import
import android.graphics.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;


/*   
* @author Connor MacKinnon
*/
public class MazeBuilderBoruvka extends MazeBuilder implements Runnable  {
	HashMap <Integer, Integer> weight_map = new HashMap<>();
	HashMap <Integer, ArrayList<Point>> clusterHashMap = new HashMap<>();
	private static final Logger LOGGER = Logger.getLogger(MazeBuilderPrim.class.getName());
	
	
	boolean first_time = true;
	
	/**
	 * Constructor for MazeBuilderBoruvka
	 */
	public MazeBuilderBoruvka() {
		super();
		LOGGER.config("Using Boruvka's algorithm to generate maze.");
		//Fills clusterHashmap with all coordinate points in the maze
		//Each coordinate belongs to a unique group number (key)
		//The value stored is an arraylist containing only the point
		//an arraylist is used for later points to be added to groups
		
	}
	
	/**
	 * Populates clusterHashmap with all coordinate points in the maze and
	 * assigns unique group numbers.
	 */
	private void populator() {
		int group = 1;
		for(int x = 0; x < this.width; x++) {
			for(int y = 0; y < this.height; y++) {
				Point pointer = new Point(x,y);
				ArrayList<Point> coordinates = new ArrayList<Point>();
				coordinates.add(pointer);
				clusterHashMap.put(group, coordinates);	
				group++;
			}
		}
	}

	/**
	 * Generates a random unique edge-weight that is then stored if an existing edge-weight doesn't exist.
	 * It then returns this edge-weight.
	 * @param x Takes a coordinate x value
	 * @param y Takes a coordinate y value
	 * @param cd Takes a CardinalDirection type corresponding to the desired edge-weight
	 * @return returns the weight of the edge at the specified coordinate and direction.
	 */
	public int getEdgeWeight(int x, int y, CardinalDirection cd) {
		int weight=(int)(Math.random()*400000);
		if (this.floorplan.hasWall(x, y, cd)){
			Wallboard tempWallboard = new Wallboard(x,y, cd);
			Wallboard neighbor = new Wallboard(tempWallboard.getNeighborX(), tempWallboard.getNeighborY(), cd.oppositeDirection());
			if (weight_map.containsKey(tempWallboard.hashCode())) {
				return weight_map.get(tempWallboard.hashCode());
			}
			else if(weight_map.containsKey(neighbor.hashCode())) {			
				return weight_map.get(neighbor.hashCode());				
			}													
			else {
				while(weight_map.containsValue(weight)) {
					weight=(int)(Math.random()*400000);
				}
				weight_map.put(tempWallboard.hashCode(), weight);
			}
			return weight;
		}
		else {
			return 400001;
		}
		
	}
	
	/**
	 * Finds the cardinal direction of the cheapest neighbor of (x,y).
	 * Also checks that it is not obtaining a border value.
	 * @param x Takes a coordinate x value
	 * @param y Takes a coordinate y value
	 * @Returns The cardinal direction pointing towards the cheapest edge of (x,y)
	 */
	private CardinalDirection cheapestNeighbor(int x, int y) {
		HashMap <Integer, CardinalDirection> int_Dir = new HashMap<>();
		int min = Integer.MAX_VALUE;
		if ( y!=0 && min>getEdgeWeight(x, y, CardinalDirection.North)){
			int_Dir.put(getEdgeWeight(x, y, CardinalDirection.North), CardinalDirection.North);
			min = getEdgeWeight(x, y, CardinalDirection.North);
		}
		if ( x!=(this.width-1) && min>getEdgeWeight(x, y, CardinalDirection.East)){
			int_Dir.put(getEdgeWeight(x, y, CardinalDirection.East), CardinalDirection.East);
			min = getEdgeWeight(x, y, CardinalDirection.East);
		}
		if ( y!=(this.height-1) && min>getEdgeWeight(x, y, CardinalDirection.South)){
			int_Dir.put(getEdgeWeight(x, y, CardinalDirection.South), CardinalDirection.South);
			min = getEdgeWeight(x, y, CardinalDirection.South);
		}
		if ( x!=0 && min>getEdgeWeight(x, y, CardinalDirection.West)){
			int_Dir.put(getEdgeWeight(x, y, CardinalDirection.West), CardinalDirection.West);
			min = getEdgeWeight(x, y, CardinalDirection.West);
		}
		
		
		return int_Dir.get(min);
		
	}
	
	/**
	 * Iterates through every key in clusterHashMap and checks if they contain the (x,y) coordinate.
	 * @param x Takes a coordinate x value
	 * @param y Takes a coordinate y value
	 * @Returns The key/group/cluster number of the (x,y) coordinates
	 */
	private int getGroupNumber(int x, int y){
		if (first_time) {
			first_time = false;
			populator();
		}
		Point target = new Point(x, y);
		Set<Integer> keys = clusterHashMap.keySet();
		for (int key : keys) {
			ArrayList<Point> pointlist = clusterHashMap.get(key);
			for (Point iterator : pointlist) {
				
				if (iterator.equals(target)) {
					return ((int)key);
				}
			}
		}
		assert(false) : "Error coordinate has no key (groupNumber)";
		return -1;
	}
	
	/**
	 * Removes wall at the specified coordinates and direction. Also adds 
	 * the new cell's cluster to the original cell's cluster which is stored in clusterHashMap.
	 * the new cell's group is then deleted from the clusterHashmap.
	 * 
	 * This should not receive any inputs for borders of the maze.
	 * 
	 * @param x Takes a coordinate x value
	 * @param y Takes a coordinate y value
	 * @param cd Takes a CardinalDirection type corresponding to the desired edge to be removed
	 */
	private void wallDestroyer(int x, int y, CardinalDirection cd) {
		int remove_key = -1;
		Wallboard tempWallboard = new Wallboard(x,  y, cd);
		int retain_key = getGroupNumber(x, y);
		if (cd == CardinalDirection.North || cd == CardinalDirection.South){
			remove_key  = getGroupNumber(x, tempWallboard.getNeighborY());
		}
		else if(cd == CardinalDirection.East || cd == CardinalDirection.West){
			remove_key  = getGroupNumber(tempWallboard.getNeighborX(), y);
		}
		assert(remove_key != -1) : "Key was not found";
		ArrayList<Point> oldValues = clusterHashMap.get(retain_key);	
		ArrayList<Point> newValues = clusterHashMap.get(remove_key);
		oldValues.addAll(newValues);
		ArrayList<Point> noDupes = new ArrayList<Point>(); 
        for (Point element : oldValues) { 
            if (!noDupes.contains(element)) { 
                noDupes.add(element); 
            } 
        } 
		clusterHashMap.remove(remove_key);
		clusterHashMap.put(retain_key, noDupes);
		this.floorplan.deleteWallboard(tempWallboard);
		
	}
	
	/**
	 * This method generates the pathways by utilizing the other methods of MazeBuilderBoruvka.
	 * This is done by first grouping the coordinates utilizing MazeBuilderBoruvka.cheapestNeighbor and removing walls between them.
	 * These groups are then merged and regrouped together until a single group remains.
	 */
	@Override
	protected void generatePathways(){
		HashMap <Point, CardinalDirection> marked = new HashMap<>();
		for(int x = 0; x<this.width;x++) {
			for(int y = 0; y<this.height;y++) {
				Point newpoint = new Point(x,y);
				CardinalDirection cd = cheapestNeighbor(x, y);
				marked.put(newpoint, cd);
			}
		}
		
		for (Point point : marked.keySet()) {
			wallDestroyer(point.x, point.y, marked.get(point));
		}
		HashMap <Point, CardinalDirection> groupMarked = new HashMap<>();
		while(clusterHashMap.size() > 1){
			for(int key : clusterHashMap.keySet()){	
				if (clusterHashMap.get(key).size()>0) {
					int min = Integer.MAX_VALUE;
					Point bestPoint = null;
					for(int x = 0; x<this.width;x++){
						for(int y = 0; y<this.height;y++) {
							if (getGroupNumber(x, y) == key  && (this.cheapestNeighbor(x, y) != null)) {
								int cheapest = this.getEdgeWeight(x, y, this.cheapestNeighbor(x, y));
								if(cheapest<min) {
									min = cheapest;
									bestPoint = new Point(x,y);
								}
							}
						}
					}
					groupMarked.put(bestPoint, cheapestNeighbor(bestPoint.x,bestPoint.y));
				}
			}
			for (Point point : groupMarked.keySet()) {
				wallDestroyer(point.x, point.y, groupMarked.get(point));
			}
		}
	}
}
