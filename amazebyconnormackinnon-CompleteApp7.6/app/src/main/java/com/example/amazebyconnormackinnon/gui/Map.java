/**
 * 
 */
package com.example.amazebyconnormackinnon.gui;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Floorplan;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.ColorTheme.MazeColors;
import com.example.amazebyconnormackinnon.gui.GameInterface.MazePanel;

//import java.awt.Graphics;
import android.annotation.SuppressLint;
import android.graphics.Color;
import java.util.logging.Logger;

/**
 * This class encapsulates all functionality to draw a map of the overall maze,
 * the set of visible walls, the solution.
 * The map is drawn on the screen in such a way that the current position
 * remains at the center of the screen.
 * The current position is visualized as a red dot with an attached arc
 * for its current direction.
 * The solution is visualized as a yellow line from the current position
 * towards the exit of the map.
 * Walls that have been visible in the first person view are drawn white,
 * all other walls that were never shown before are drawn in grey.
 * It is possible to zoom in and out of the map by increasing or decreasing
 * the map scale.
 * 
 * This code is refactored code from Maze.java by Paul Falstad,
 * www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 *
 */
public class Map {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(Map.class.getName());


	// keep local copies of values determined for UI appearance
	final int viewWidth;  // set to Constants.VIEW_WIDTH, 
	final int viewHeight; // set to Constants.VIEW_HEIGHT
	final int mapUnit;    // set to Constants.MAP_UNIT
	final int stepSize;  // set to Constants.STEP_SIZE, typical value: map_unit/4
	
	/**
	 * The user can increment or decrement the scale of the map.
	 * map_scale is used to keep track of the current setting.
	 * Minimum value is 1.
	 */
	int mapScale;
	
	/**
	 * SeenWalls contains information on walls that are seen from the current point of view.
	 * The field is set by the constructor. The referenced object is shared with
	 * the FirstPersonDrawer that writes content into it. The MapDrawer only
	 * reads content to decide which lines to draw and in which color.
	 */
	final Floorplan seenWalls ; 

	/**
	 * Contains all necessary information about current maze, i.e.
	 * cells: location of wallboards
	 * dists: distance to exit
	 * width and height of the maze
	 */
	final Maze maze ;

	/**
	 * Constructor 
	 * @param width of display
	 * @param height of display
	 * @param mapUnit gives the map unit
	 * @param stepSize give the step size
	 * @param seenWalls stores information on which walls have been on display, must be non-null
	 * @param mapScale gives the current scale, must be greater or equal 1
	 * @param maze gives the current maze, must be non-null, a fully functional maze
	 */
	public Map(int width, int height, int mapUnit, int stepSize, Floorplan seenWalls, int mapScale, Maze maze){
		//System.out.println("MapDrawer: constructor called") ;
		viewWidth = width;
		viewHeight = height;
		this.mapUnit = mapUnit;
		this.stepSize = stepSize ;
		this.seenWalls = seenWalls ;
		this.mapScale = 50 ;//mapScale >= 1 ? mapScale: 1 // 1 <= map_scale
		this.maze = maze ;
		// correctness considerations
		assert maze != null : "Map:c maze configuration can't be null at instantiation!" ;
		assert seenWalls != null : "Map: seencells can't be null at instantiation!" ;
	}
	
	/**
	 * Constructor with default settings
	 * from Constants.java for width, height, mapUnit and stepSize.
	 * @param seenWalls stores information on which walls have been on display, must be non-null
	 * @param mapScale gives the current scale, must be greater or equal 1
	 * @param maze gives the current maze, must be non-null, a fully functional maze
	 */
	public Map(Floorplan seenWalls, int mapScale, Maze maze){
		this(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,Constants.MAP_UNIT,
    			Constants.STEP_SIZE, seenWalls, mapScale, maze);
		}
	
	/**
	 * Make the map being drawn bigger on the screen
	 */
	public void incrementMapScale() {
		mapScale += 10 ;
	}
	
	/**
	 * Make the map being drawn smaller on the screen
	 */
	public void decrementMapScale() {
		mapScale -= 10 ;
		if (1 > mapScale)
			mapScale = 1 ;
	}

	/**
	 * Draws the current map on top of the first person view.
	 * Method assumes that we are in the playing state and that
	 * the map mode is switched on.
	 * @param panel is the panel to draw with
	 * @param x current position, x coordinate
	 * @param y current position, y coordinate
	 * @param angle the current angle, used to derive the viewing angle
	 * @param walkStep is a counter between 0, 1, 2, ..., 3
	 * for in between stages for a walk operation, needed to obtain
	 * exact location in map
	 * @param showMaze if true, highlights already seen walls in white
	 * @param showSolution if true shows a path to the exit as a yellow line,
	 * otherwise path is not shown.
	 */
	public void draw(MazePanel panel, int x, int y, int angle, int walkStep,
					 boolean showMaze, boolean showSolution) {
		//Graphics g = panel.getBufferGraphics() ;
		MazePanel g = panel;
        // viewers draw on the buffer graphics
        if (null == g) {
        	LOGGER.warning("Can't get graphics object to draw on, mitigate this by skipping draw operation") ;
            return;
        }
        final int viewDX = getViewDX(angle); 
        final int viewDY = getViewDY(angle);
        drawMap(g, x, y, walkStep, viewDX, viewDY, showMaze, showSolution) ;
        drawCurrentLocation(g, viewDX, viewDY) ;
	}
	//////////////////////////////// private, internal methods //////////////////////////////
	/**
	 * Computes the x coordinate for the viewing direction for the given angle
	 * @param angle is a viewing angle
	 * @return the corresponding viewing direction, x coordinate
	 */
	private int getViewDX(int angle) {
		return (int) (Math.cos(radify(angle))*(1<<16));
	}
	/**
	 * Computes the y coordinate for the viewing direction for the given angle
	 * @param angle is a viewing angle
	 * @return the corresponding viewing direction, y coordinate
	 */
	private int getViewDY(int angle) {
		return (int) (Math.sin(radify(angle))*(1<<16));
	}
	final double radify(int x) {
        return x*Math.PI/180;
    }
	/**
	 * Helper method for draw, called if map_mode is true, i.e. the users wants to see the overall map.
	 * The map is drawn only on a small rectangle inside the maze area such that only a part of the map is actually shown.
	 * Of course a part covering the current location needs to be displayed.
	 * The current cell is (px,py). There is a viewing direction (view_dx, view_dy).
	 * @param panel is the panel being drawn on
	 * @param px current position, x index
	 * @param py current position, y index
	 * @param walkStep is a counter between 0, 1, 2, ..., 3
	 * for in between stages for a walk operation, needed to obtain
	 * exact location in map
	 * @param viewDX is the current viewing direction, x coordinate
	 * @param viewDY is the current viewing direction, y coordinate
	 * @param showMaze is the flag to show the walls
	 * @param showSolution  is the flag to show the solution, the yellow line to the exit
	 */
	@SuppressLint("NewApi")
	private void drawMap(MazePanel panel, int px, int py, int walkStep,
						 int viewDX, int viewDY, boolean showMaze, boolean showSolution) {
		// dimensions of the maze in terms of cell ids
		final int mazeWidth = maze.getWidth() ;
		final int mazeHeight = maze.getHeight() ;
		
		panel.setColor(ColorTheme.getColor(MazeColors.MAP_DEFAULT));
		
		// note: 1/2 of width and height is the center of the screen
		// the whole map is centered at the current position
		final int offsetX = getOffset(px, walkStep, viewDX, viewWidth);
		final int offsetY = getOffset(py, walkStep, viewDY, viewHeight);
		
		// We need to calculate bounds for cell indices to consider
		// for drawing. Since not the whole maze may be visible
		// for the given screen size and the current position (px,py)
		// is fixed to the center of the drawing area, we need
		// to find the min and max indices for cells to consider.
		// compute minimum for x,y
		final int minX = getMinimum(offsetX);
		final int minY = getMinimum(offsetY);
		// compute maximum for x,y
		final int maxX = getMaximum(offsetX, viewWidth, mazeWidth);
		final int maxY = getMaximum(offsetY, viewHeight, mazeHeight);
		
		// iterate over integer grid between min and max of x,y indices for cells
		for (int y = minY; y <= maxY; y++)
			for (int x = minX; x <= maxX; x++) {
				// starting point of line
				int startX = mapToCoordinateX(x, offsetX);
				int startY = mapToCoordinateY(y, offsetY);
				if (x < mazeWidth)
					drawHorizontalLine(panel, showMaze, x, y, startX, startY);
				if (y < mazeHeight)
					drawVerticalLine(panel, showMaze, x, y, startX, startY);
			}
		
		if (showSolution) {
			drawSolution(panel, offsetX, offsetY, px, py) ;
		}
	}

	/**
	 * Draw a vertical line for the overall map
	 * @param panel is the panel being drawn on
	 * @param showMaze if the whole maze is to be drawn otherwise only the visible walls
	 * @param x current x index
	 * @param y current y index
	 * @param startX the x coordinate for drawing
	 * @param startY the y coordinate for drawing
	 */
	@SuppressLint("NewApi")
	private void drawVerticalLine(MazePanel panel, boolean showMaze, int x, int y, int startX, int startY) {
		if (hasAVerticalWall(x, y) && 
				(seenWalls.hasWall(x, y, CardinalDirection.West) || showMaze)) {
			panel.setColor(seenWalls.hasWall(x, y, CardinalDirection.West) ?
					ColorTheme.getColor(MazeColors.MAP_WALL_SEENBEFORE) : 
						ColorTheme.getColor(MazeColors.MAP_WALL_DEFAULT));
			panel.addLine(startX, startY, startX, startY - mapScale);
		}
	}
	
	/**
	 * Check if there is a wall on the west side of the (x,y) position in the maze
	 * @param x index for position in maze
	 * @param y index for position in maze
	 * @return true if there is a wall on the west side
	 */
	private boolean hasAVerticalWall(int x, int y) {
		return (x < maze.getWidth()) ? 
				maze.hasWall(x, y, CardinalDirection.West) :
					maze.hasWall((x-1), y, CardinalDirection.East);
	}
	
	/**
	 * Draw a horizontal line for the overall map
	 * @param panel is the panel being drawn on
	 * @param showMaze if the whole maze is to be drawn
	 * @param x current x index
	 * @param y current y index
	 * @param startX the x coordinate for drawing
	 * @param startY the y coordinate for drawing
	 */
	@SuppressLint("NewApi")
	private void drawHorizontalLine(MazePanel panel, boolean showMaze, int x, int y, int startX,
									int startY) {
		if (hasAHorizontalWall(x, y) && (seenWalls.hasWall(x,y, CardinalDirection.North) || showMaze) ) {
			panel.setColor(seenWalls.hasWall(x,y, CardinalDirection.North) ?
					ColorTheme.getColor(MazeColors.MAP_WALL_SEENBEFORE) : 
						ColorTheme.getColor(MazeColors.MAP_WALL_DEFAULT));
			panel.addLine(startX, startY, startX + mapScale, startY);
		}
	}
	
	/**
	 * Check if there is a wall on the north side of the (x,y) position in the maze
	 * @param x index for position in maze
	 * @param y index for position in maze
	 * @return true if there is a wall on the north side
	 */
	private boolean hasAHorizontalWall(int x, int y) {
		return (y < maze.getHeight()) ?
				maze.hasWall(x,y, CardinalDirection.North) :
					maze.hasWall(x,y-1, CardinalDirection.South);
	}
	
	/**
	 * Obtains the maximum for a given offset
	 * @param offset either in x or y direction
	 * @param viewLength is either viewWidth or viewHeight
	 * @param mazeLength is either mazeWidth or mazeHeight
	 * @return maximum that is bounded by mazeLength
	 */
	private int getMaximum(int offset, int viewLength, int mazeLength) {
		int result = (viewLength-offset)/mapScale+1;
		return (result >= mazeLength) ? mazeLength : result; 
	}

	/**
	 * Obtains the minimum for a given offset
	 * @param offset either in x or y direction
	 * @return minimum that is greater or equal 0
	 */
	private int getMinimum(final int offset) {
		final int result = -offset/mapScale;
		return (result < 0) ? 0 : result;
	}

	/**
	 * Calculates the offset in either x or y direction
	 * @param coordinate is either x or y coordinate of current position
	 * @param walkStep is a counter between 0, 1, 2, ..., 3
	 * for in between stages for a walk operation, needed to obtain
	 * exact location in map
	 * @param viewDirection is either viewDX or viewDY
	 * @param viewLength is either viewWidth or viewHeight
	 * @return the offset
	 */
	private int getOffset(int coordinate, int walkStep, int viewDirection, int viewLength) {
		final int tmp = coordinate*mapUnit + mapUnit/2 + mapToOffset((stepSize*walkStep),viewDirection);
		return -tmp*mapScale/mapUnit + viewLength/2;
	}
	
	/**
	 * Maps the y index for some cell (x,y) to a y coordinate
	 * for drawing.
	 * @param cellY, {@code 0 <= cellY < height}
	 * @param offsetY  is the offset for Y
	 * @return y coordinate for drawing
	 */
	private int mapToCoordinateY(int cellY, int offsetY) {
		// TODO: bug suspect: inversion with height is suspect for upside down effect on directions
		// note: (cellY*map_scale + offsetY) same as for mapToCoordinateX
		return viewHeight-1-(cellY*mapScale + offsetY);
	}

	/**
	 * Maps the x index for some cell (x,y) to an x coordinate
	 * for drawing. 
	 * @param cellX is the index of some cell, {@code 0 <= cellX < width}
	 * @param offsetX is the offset for X 
	 * @return x coordinate for drawing
	 */
	private int mapToCoordinateX(int cellX, int offsetX) {
		return cellX*mapScale + offsetX;
	}
	
	/**
	 * Maps a given length and direction into an offset for drawing coordinates.
	 * @param length is the length
	 * @param direction is the direction
	 * @return offset is the offset
	 */
	private int mapToOffset(final int length, final int direction) {
		// Signed bit shift to the right performs a division by 2^16
		// preserves the sign
		// discards the remainder as the result is int
		return (length * direction) >> 16;
	}
	/**
	 * Unscale value
	 * @param x the input value
	 * @return the input value after a right shift by 16
	 */
	final int unscaleViewD(int x) {
		// >> is the signed right shift operator
		// shifts input x in its binary representation
		// 16 times to the right
		// same as divide by 2^16 and discard remainder
		// preserves sign
		// essentially used here for the following mapping
		// (based on debug output observations)
		// dbg("right shift: " + x + " gives " + (x >> 16));
		// -2097152 gives -32
		// -4194304 gives -64
		// -6291456 gives -96
		// -8388608 gives -128
		// 2097152 gives 32
		// 4194304 gives 64
		// 6291456 gives 96
		// 8388608 gives 128
		return x >> 16;
	}
	/**
	 * Draws a red circle at the center of the screen and
	 * an arrow for the current direction.
	 * It always reside on the center of the screen. 
	 * The map drawing moves if the user changes location.
	 * The size of the overall visualization is limited by
	 * the size of a single cell to avoid that the circle
	 * or arrow visually collide with an adjacent wallboard on the
	 * map visualization. 
	 * @param panel is the panel being drawn on
	 * @param viewDX is the current viewing direction, x coordinate
	 * @param viewDY is the current viewing direction, y coordinate
	 */
	@SuppressLint("NewApi")
	private void drawCurrentLocation(MazePanel panel, int viewDX, int viewDY) {
		panel.setColor(ColorTheme.getColor(MazeColors.MAP_CURRENTLOCATION));
		// draw oval of appropriate size at the center of the screen
		int centerX = viewWidth/2; // center x
		int centerY = viewHeight/2; // center y
		int diameter = mapScale/4; // circle size
		// we need the top left corner of a bounding box the circle is in
		// and its width and height to draw the circle
		// top left corner is (centerX-radius, centerY-radius)
		// width and height is simply the diameter

		//(centerX-diameter)/2
		//(centerY-diameter)/2
		panel.addFilledOval(centerX, centerY, diameter, diameter);

		// draw a red arrow with the oval to show current direction
		drawArrow(panel, viewDX, viewDY, centerX, centerY);
	}

	/**
	 * Draws an arrow either in horizontal or vertical direction.
	 * @param panel is the panel being drawn on
	 * @param viewDX is the current viewing direction, x coordinate
	 * @param viewDY is the current viewing direction, y coordinate
	 * @param startX is the x coordinate of the starting point
	 * @param startY is the y coordinate of the starting point
	 */
	private void drawArrow(MazePanel panel, int viewDX, int viewDY,
			final int startX, final int startY) {
		// calculate length and coordinates for main line
		final int arrowLength = mapScale*7/16; // arrow length, about 1/2 map_scale
		final int tipX = startX + mapToOffset(arrowLength, viewDX);
		final int tipY = startY - mapToOffset(arrowLength, viewDY);
		// draw main line, goes from starting (x,y) to end (tipX,tipY)
		panel.addLine(startX, startY, tipX, tipY);
		// calculate length and positions for 2 lines pointing towards (tipX,tipY)
		// find intermediate point (tmpX,tmpY) on main line
		final int length = mapScale/4;
		final int tmpX = startX + mapToOffset(length, viewDX);
		final int tmpY = startY - mapToOffset(length, viewDY);
		// find offsets at intermediate point for 2 points orthogonal to main line
		// negative sign used for opposite direction
		// note the flip between x and y for view_dx and view_dy
		/*
		final int offsetX = -(length * view_dy) >> 16;
		final int offsetY = -(length * view_dx) >> 16;
		*/
		final int offsetX = mapToOffset(length, -viewDY);
		final int offsetY = mapToOffset(length, -viewDX);
		// draw two lines, starting at tip of arrow
		panel.addLine(tipX, tipY, tmpX + offsetX, tmpY + offsetY);
		panel.addLine(tipX, tipY, tmpX - offsetX, tmpY - offsetY);
	}


	
	/**
	 * Draws a yellow line to show the solution on the overall map. 
	 * Method is only called if in state playing and map_mode 
	 * and showSolution are true.
	 * Since the current position is fixed at the center of the screen, 
	 * all lines on the map are drawn with some offset.
	 * @param panel is the panel being drawn on
	 * @param offsetX is the offset for x coordinates
	 * @param offsetY is the offset for y coordinates
	 * @param px is the current position, an index x for a cell
	 * @param py is the current position, an index y for a cell
	 */
	@SuppressLint("NewApi")
	private void drawSolution(MazePanel panel, int offsetX, int offsetY, int px, int py) {

		if (!maze.isValidPosition(px, py)) {
			LOGGER.warning("Parameter error: position out of bounds: (" + px + "," + 
					py + ") for maze of size " + maze.getWidth() + "," + 
					maze.getHeight() + ", mitigation: skip drawing the solution line") ;
			return ;
		}
		// current position on the solution path (sx,sy)
		int sx = px;
		int sy = py;
		int distance = maze.getDistanceToExit(sx, sy);
		
		panel.setColor(ColorTheme.getColor(MazeColors.MAP_SOLUTION));
		
		// while we are more than 1 step away from the final position
		while (distance > 1) {
			// find neighbor closer to exit (with no wallboard in between)
			int[] neighbor = maze.getNeighborCloserToExit(sx, sy) ;
			if (null == neighbor)
				return ; // error
			// scale coordinates, original calculation:
			// x-coordinates
			// nx1     == sx*map_scale + offx + map_scale/2;
			// nx1+ndx == sx*map_scale + offx + map_scale/2 + dx*map_scale == (sx+dx)*map_scale + offx + map_scale/2;
			// y-coordinates
			// ny1     == view_height-1-(sy*map_scale + offy) - map_scale/2;
			// ny1+ndy == view_height-1-(sy*map_scale + offy) - map_scale/2 + -dy * map_scale == view_height-1 -((sy+dy)*map_scale + offy) - map_scale/2
			// current position coordinates
			//int nx1 = sx*map_scale + offx + map_scale/2;
			//int ny1 = view_height-1-(sy*map_scale + offy) - map_scale/2;
			//
			// we need to translate the cell indices x and y into
			// coordinates for drawing, the yellow lines is centered
			// so 1/2 of the size of the cell needs to be added to the
			// top left corner of a cell which is + or - map_scale/2.
			int nx1 = mapToCoordinateX(sx,offsetX) + mapScale/2;
			int ny1 = mapToCoordinateY(sy,offsetY) - mapScale/2;
			// neighbor position coordinates
			//int nx2 = neighbor[0]*map_scale + offx + map_scale/2;
			//int ny2 = view_height-1-(neighbor[1]*map_scale + offy) - map_scale/2;
			int nx2 = mapToCoordinateX(neighbor[0],offsetX) + mapScale/2;
			int ny2 = mapToCoordinateY(neighbor[1],offsetY) - mapScale/2;
			panel.addLine(nx1, ny1, nx2, ny2);
			
			// update loop variables for current position (sx,sy)
			// and distance d for next iteration
			sx = neighbor[0];
			sy = neighbor[1];
			distance = maze.getDistanceToExit(sx, sy) ;
		}
	}
	

	/**
	 * Debug output
	 * @param str the string to be printed for debugging purposes
	 */
	private void dbg(String str) {
		LOGGER.fine(str);
	}
}
