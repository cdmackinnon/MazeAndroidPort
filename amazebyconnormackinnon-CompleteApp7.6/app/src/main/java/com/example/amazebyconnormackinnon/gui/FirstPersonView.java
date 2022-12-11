/**
 * 
 */
package com.example.amazebyconnormackinnon.gui;

//import java.awt.Graphics;
//import java.awt.Graphics2D;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.List;
import java.util.logging.Logger;

import com.example.amazebyconnormackinnon.generation.BSPBranch;
import com.example.amazebyconnormackinnon.generation.BSPLeaf;
import com.example.amazebyconnormackinnon.generation.BSPNode;
import com.example.amazebyconnormackinnon.generation.Floorplan;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.generation.Wall;
import com.example.amazebyconnormackinnon.gui.ColorTheme.MazeColors;
import com.example.amazebyconnormackinnon.gui.GameInterface.MazePanel;

/**
 * This class encapsulates all functionality for drawing the current view 
 * at the maze from a first person perspective.
 * It is an drawing agent with redraw as its public method to redraw 
 * the maze while the user plays, i.e. navigates through the maze.
 *
 * This code is refactored code from Maze.java by Paul Falstad, 
 * www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class FirstPersonView {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(FirstPersonView.class.getName());

	// Constants
	final int viewZ = 50;  // constant from StatePlaying.java
	// Instance variables set once and for all in constructor call
	// keeps local copies of values determined in StatePlaying.java, 
	// values are basically constants or shared data structures across 
	// StatePlaying, MapDrawer and FirstPersonDrawer
	// constants, i.e. set in constructor call with values 
	// that are not subject to change in maze
	private final int viewWidth;  // = 400;
	private final int viewHeight; // = 400;
	private final int mapUnit;    // = 128;
	private final int stepSize;   // = map_unit/4;
	// map scale may be adjusted by user input, controlled in StatePlaying
	
	/**
	 * A data structure to store which wallboards have been visible during
	 * the game. The Map can color highlight such wallboards and the 
	 * FirstPersonDrawer supports this by storing this information.
	 */
	private final Floorplan seenWalls;
	
	/** 
	 * The root node to a tree with walls (Wall objects) in its leaves.
	 * This data structure carries the information about walls
	 * to draw. Walls are drawn as filled polygons. 
	 * The content for this tree is determined in MazeBuilder
	 * when the maze is created. 
	 * It also used to decide visibility of walls.
	 */
	private final BSPNode bspRoot;
	
	/** 
	 * The current viewing angle. This information is used in rotations
	 * and checking if the bounding box is visible to recognize special cases.
	 * It is only set in the draw method before the first person
	 * drawer starts drawing the new first person scene.
	 * An initial value of 0 matches East.
	 */
	private int angle;  
	
	/**
	 * The drawing is performed on a Graphics object. Storing it makes
	 * its access easier for code that traverses the tree of BSP nodes
	 * to draw walls. Drawing is performed in a piecemeal manner on
	 * a buffer image, such that updating the panel that is on screen 
	 * with the current buffer image is the responsibility of
	 * the StatePlaying class.
	 */
	//TODO UPDATE DOCUMENTATION, Graphics2D -> MazePanel
	private MazePanel panel;

	/**
	 * The current position (x,y) scaled by map_unit and 
	 * modified by the view direction is stored in fields (viewX, viewY).
	 * Values are set in the draw method. viewX gives the x coordinate,
	 * viewY the y coordinate.
	 */
	private int viewX; 
	private int viewY; 
	
	// set in constructor to then given view_height/2, used in bounding box
	private final int scaleZ;      // = view_height/2; which is the horizon
	
	/**
	 * If one focuses on the x-axis for the first person view 
	 * and looks at the center line (the horizon), one recognizes
	 * that for any [x,x+1] interval, there is either  
	 * an opening for the special case of the exit or 
	 * exactly 1 wall shown. So we can keep track of the intervals
	 * [x,x+delta_x] that we have already drawn a wall for and
	 * the intervals that still may need to be covered.
	 * The rset keeps track of candidate intervals that may still
	 * need to be covered. 
	 * It is initialized in the draw method to cover the whole width
	 * of the view. 
	 * Whenever polygons for walls are drawn, the covered interval
	 * is removed from the range set. Which can imply that we either
	 * shorten overlapping intervals in rset or split an existing 
	 * interval.
	 * The rset allows us to omit walls that are not needed.
	 */
	private RangeSet rSet; 
	
	// debug stuff
	private boolean deepDebug = false;
	private boolean allVisible = false;
	private int traverseNodeCounter;
	private int traverseWallSectorCounter;
	private int drawRectCounter;
	private int drawRectLateCounter;
	private int drawRectWallCounter;
	private int nesting = 0;
	
	/**
	 * Constructor
	 * @param width of display
	 * @param height of display
	 * @param mapUnit current scaling factor
	 * @param stepSize size of steps
	 * @param seenWalls to store which walls were put on display
	 * @param bspRoot the root node of the bsp tree
	 */
	public FirstPersonView(int width, int height, int mapUnit, int stepSize, Floorplan seenWalls, BSPNode bspRoot) {
		// store given parameter values
		viewWidth = width;
		viewHeight = height;
		this.mapUnit = mapUnit;
		this.stepSize = stepSize;
		this.seenWalls = seenWalls;
		this.bspRoot = bspRoot; 
		// constants and derived values
		angle = 0; // angle for initial setting of direction is 0 == East, hidden constraint across classes
		scaleZ = viewHeight/2;
		// initialize fields
		rSet = new RangeSet();
	}
	/**
	 * Draws the first person view on the screen during the game
	 * @param panel for drawing on the buffer image
	 * @param x coordinate of current position, only used to set viewX
	 * @param y coordinate of current position, only used to set viewY
	 * @param ang gives the current viewing angle
	 * @param percentToExit gives the distance to exit as a percentage
	 * @param walkStep, only used to set viewX and viewY
	 * 
	 */
	@SuppressLint("NewApi")
	public void draw(MazePanel panel, int x, int y, int walkStep, int ang, float percentToExit) {
		// obtain a Graphics2D object we can draw on
		//Graphics g = panel.getBufferGraphics() ;

        // viewers draw on the buffer graphics
        if (null == panel) {
            LOGGER.warning("Can't get graphics object to draw on, mitigate this by skipping draw operation") ;
            return;
        }
        this.panel = panel;
        
        // update fields angle, viewx, viewy for current position and viewing angle
        angle = ang ;
        setView(x, y, walkStep);
        
        // update graphics
        // draw background figure: lightGrey to green on bottom half, yellow to gold on top half
        //drawBackground(g, percentToExit);	REPLACED
		panel.addBackground(percentToExit);
        // set color to white and draw what ever can be seen from the current position
        panel.setColor(ColorTheme.getColor(MazeColors.FIRSTPERSON_DEFAULT));
        // reset the set of ranges to a single new element (0,width-1)
        // to cover the full width of the view 
        // as we have not drawn any polygons (walls) yet.
        rSet.set(0, viewWidth-1); 
        
        // debug: reset counters
        traverseNodeCounter = traverseWallSectorCounter =
        		drawRectCounter = drawRectLateCounter = drawRectWallCounter = 0;
        //
        drawAllVisibleSectors(bspRoot);
	}


	////////////////////////////// internal, private methods ///////////////////////////////
	private int getViewDX(int angle) {
		return (int) (Math.cos(radify(angle))*(1<<16));
	}
	private int getViewDY(int angle) {
		return (int) (Math.sin(radify(angle))*(1<<16));
	}
	final double radify(int x) {
        return x*Math.PI/180;
    }
	/**
	 * Updates viewX and viewY based on current angle and for current position.
	 * @param x coordinate of current position
	 * @param y coordinate of current position
	 * @param walkStep goes into a scaling factor
	 */
	private void setView(int x, int y, int walkStep) {
		// Notes: only used in draw method
		final int factor = stepSize*walkStep-Constants.VIEW_OFFSET;
		viewX = (x*mapUnit+mapUnit/2) + unscaleViewD(getViewDX(angle)*factor);
        viewY = (y*mapUnit+mapUnit/2) + unscaleViewD(getViewDY(angle)*factor);
	}
	/*
	 * Draws two solid rectangles to provide a background.
	 * Note that this also erases previous drawings of maze or map.
	 * The color setting adjusts to the distance to the exit to 
	 * provide an additional clue for the user.
	 * Colors transition from yellow to gold and from light grey to green.
	 * @param graphics to draw on, must be not null
	 * @param percentToExit gives the distance to exit
	 */

	/**
	 * TODO Update documentation
	 * @param percentToExit
	 */
	//MazePanel graphics,
	@SuppressLint("NewApi")
	private void drawBackground(float percentToExit) {
		panel.setColor(ColorTheme.getColor(MazeColors.BACKGROUND_TOP,percentToExit));
		//graphics.setColor(ColorTheme.getColor(MazeColors.BACKGROUND_TOP,percentToExit));
		panel.addFilledRectangle(0, 0, viewWidth, viewHeight/2);
		//graphics.fillRect(0, 0, viewWidth, viewHeight/2);
		panel.setColor(ColorTheme.getColor(MazeColors.BACKGROUND_BOTTOM,percentToExit));
		//graphics.setColor(ColorTheme.getColor(MazeColors.BACKGROUND_BOTTOM,percentToExit));
		panel.addFilledRectangle(0, viewHeight/2, viewWidth, viewHeight/2);
		//graphics.fillRect(0, viewHeight/2, viewWidth, viewHeight/2);
	}

	/**
	 * Recursive method to explore tree of BSP nodes and draw all walls in leaf nodes 
	 * where the bounding box is visible
	 * @param node is the current node of interest
	 */
	private void drawAllVisibleSectors(BSPNode node) {
		traverseNodeCounter++; // debug
		
		// Anchor, stop recursion at leaf nodes
		if (node.isIsleaf()) {
			drawAllWallsOfASector((BSPLeaf) node);
			return;
		}
		
		// for intermediate nodes proceed recursively through all visible branches
		BSPBranch n = (BSPBranch) node;
		
		// debug code
		if (deepDebug) {
			dbg("                               ".substring(0, nesting) +
					"traverse_node "+n.getX()+" "+n.getY()+" "+n.getDx()+" "+n.getDy()+" "+
					n.getLowerBoundX()+" "+n.getLowerBoundY()+" "+n.getUpperBoundX()+" "+n.getUpperBoundY());
		}
		nesting++; // debug
		
		final int dot = (viewX-n.getX())*n.getDy() - (viewY-n.getY())*n.getDx();
		// The type of tree traversal depends on the value of dot
		// either do right before left or vice versa
		// but only if node is visible at all
		
		// if dot >= 0 consider right node before left node
		BSPNode right = n.getRightBranch();
		if ((dot >= 0) && (boundingBoxIsVisible(right))) {
			drawAllVisibleSectors(right);
		}
		// consider left node
		BSPNode left = n.getLeftBranch();
		if (boundingBoxIsVisible(left))
			drawAllVisibleSectors(left);
		// if dot < 0 consider right node now (after left node)
		if ((dot < 0) && (boundingBoxIsVisible(right))) {
			drawAllVisibleSectors(right);
		}
		nesting--; // debug
	}
	/**
	 * Decide if the bounding box is visible
	 * @param node the current node
	 * @return true if node should be drawn, false otherwise
	 */
	private boolean boundingBoxIsVisible(BSPNode node) {
		
		if (allVisible) // unused feature, presumably for debugging
			return true;
		// check a few simple cases up front
		// if all x-coordinates are covered with walls,
		// there nothing left to draw or if the node is outside of viewing angle
		if (rSet.isEmpty() || isOutOfView(node)) {
			return false;
		}
		
		// calculate x coordinates for two points (x1,y1) and (x2,y2)
		// to check for an intersection with the range set
		// calculate 2 intermediate points (p1x,p1y) and (p2x,p2y) first
		// then derive x1 and x2
		final int xmin = node.getLowerBoundX() - viewX;
		final int ymin = node.getLowerBoundY() - viewY;
		final int xmax = node.getUpperBoundX() - viewX;
		final int ymax = node.getUpperBoundY() - viewY;
		// initialize (p1x,p1y) and (p2x,p2y) with bounds
		int p1x = xmin; 
		int p2x = xmax;
		int p1y = ymin; 
		int p2y = ymax;
		// adjust (p1x,p1y) and (p2x,p2y) for special cases
		if (ymin < 0 && ymax > 0) {
			p1y = ymin; 
			p2y = ymax;
			if (xmin < 0) {
				if (xmax > 0)
					return true;
				p1x = p2x = xmax;
			} else
				p1x = p2x = xmin;
		} else if (xmin < 0 && xmax > 0) {
			if (ymin < 0)
				p1y = p2y = ymax;
			else
				p1y = p2y = ymin;
		} else if ((xmin > 0 && ymin > 0) || (xmin < 0 && ymin < 0)) {
			p1x = xmax; 
			p2x = xmin;
		}
		RangePair rp = getNewRangePair(p1x, p2x, p1y, p2y);
		if (!rp.clip3d())
			return false;
		// note: zscale == view_height/2 is constant
		int x1 = rp.x1*scaleZ/rp.z1+(viewWidth/2);
		int x2 = rp.x2*scaleZ/rp.z2+(viewWidth/2);
		if (x1 > x2) { //switch if necessary
			int xj = x1;
			x1 = x2;
			x2 = xj;
		}
		// constraint: x1 <= x2
		// if interval [x1,x2] intersects with any of the intervals on 
		// the x-axis that have not been covered with a wall (a polygon)
		// yet, then this node might be needed.
		return (null != rSet.getIntersection(x1, x2));
	}
	/**
	 * Instantiates a new RangePair for the given parameters.
	 * Parameter values are adjusted for the viewing direction.
	 * @param p1x x coordinate of first point
	 * @param p2x x coordinate of 2nd point
	 * @param p1y y coordinate of first point
	 * @param p2y y coordinate of 2nd point
	 * @return new RangePair for adjusted parameters
	 */
	private RangePair getNewRangePair(int p1x, int p2x, int p1y, int p2y) {
		int viewDX = getViewDX(angle) ;
        int viewDY = getViewDY(angle) ;
		
        int x1 = -unscaleViewD(viewDY*p1x - viewDX*p1y);
		int z1 = -unscaleViewD(viewDX*p1x + viewDY*p1y);
		int x2 = -unscaleViewD(viewDY*p2x - viewDX*p2y);
		int z2 = -unscaleViewD(viewDX*p2x + viewDY*p2y);
		
		return new RangePair(x1, z1, x2, z2);
	}
	/**
	 * Checks if bounding box for current node is out of view.
	 * @param node the current node
	 * @return true if any of the special cases match, false otherwise
	 */
	private boolean isOutOfView(BSPNode node) {
		if (angle >= 45 && angle <= 135 && viewY > node.getUpperBoundY())
			return true;
		if (angle >= 225 && angle <= 315 && viewY < node.getLowerBoundY())
			return true;
		if (angle >= 135 && angle <= 225 && viewX < node.getLowerBoundX())
			return true;
		if ((angle >= 315 || angle <= 45) && viewX > node.getUpperBoundX())
			return true;
		return false;
	}

	/**
	 * Traverses all walls of this leaf and draws corresponding rectangles on screen
	 * @param node is the leaf node
	 */
	private void drawAllWallsOfASector(BSPLeaf node) {
		List<Wall> allWalls = node.getAllWalls();
		// debug
		traverseWallSectorCounter++;
		if (deepDebug) {
			dbg("                               ".substring(0, nesting) +
					"traverseWallSector "+node.getLowerBoundX()+" "+node.getLowerBoundY()+
					" "+node.getUpperBoundX()+" "+node.getUpperBoundY());
		}
		// for all walls of this node
		int i = 0;
		for (Wall wall: allWalls) {
			// draw rectangle
			drawWall(wall);
			// debug
			if (deepDebug) {
				dbg("                               ".substring(0, nesting) +
						" traverseWallSector(" + i +") "+
						wall.getStartPositionX()+" "+wall.getStartPositionY()+" "+
						wall.getExtensionX()+" "+wall.getExtensionY());
				i++;
			}

		}
	}

	/**
	 * Draws wall on screen via graphics attribute gc.
	 * Helper method for drawAllWallsOfASector.
	 * @param wall whose seen attribute may be set to true
	 */
	@SuppressLint("NewApi")
	private void drawWall(Wall wall) {
		drawRectCounter++; // debug, counter
		
		// some notes: 
		// perspective centers on (centerX,centerY) = (view_width/2,view_height/2)
		// initialization part for x1, x2, y11, y12, y21, y22
		final int ox1 = wall.getStartPositionX() - viewX;
		final int ox2 = wall.getEndPositionX()   - viewX;
		final int y1  = wall.getStartPositionY() - viewY;
		final int y2  = wall.getEndPositionY()   - viewY;
		
		RangePair rp = getNewRangePair(ox1, ox2, y1, y2);
		if (!rp.clip3d())
			return;
		// note: viewZ == 50 is a constant
		// note: scaleZ == view_height/2 is constant
		final int y11 = viewZ *scaleZ/rp.z1        +(viewHeight/2); 
		final int y12 = (viewZ-100) *scaleZ/rp.z1  +(viewHeight/2); 
		final int y21 = viewZ *scaleZ/rp.z2        +(viewHeight/2); 
		final int y22 = (viewZ-100) *scaleZ/rp.z2  +(viewHeight/2); 
		final int x1  = rp.x1 *scaleZ/rp.z1        +(viewWidth/2); 
		final int x2  = rp.x2 *scaleZ/rp.z2        +(viewWidth/2); 
		
		rp = null; // added just for clarity, rp not used anymore 
		
		if (x1 >= x2) /* reject backfaces */
			return;
		
		// moved code for drawing bits and pieces into yet another method to 
		// gain more clarity on what information is actually needed

		//gc.setColor(ColorTheme.getColor(wall.getColor()));
		panel.setColor(ColorTheme.getColor(wall.getColor()).toArgb());

		boolean drawn = drawPolygons(x1, x2, y11, y12, y21, y22);
		
		if (drawn && !wall.isSeen()) {
			wall.setSeen(true); // updates the wall
			// set the seenWalls bit for all wallboards of a wall
			// the wall parameter given is not modified
			seenWalls.addWall(wall, mapUnit); // updates seenWalls
		}
	}
	
	/**
	 * Draws all polygons for the parts of a wall that are visible.
	 * A single wall can contribute more than one polygon on the
	 * x-axis, e.g. if there is a room with 2 doors on one side
	 * and the same single long wall may be seen through both
	 * doors.
	 * @param x1 is the low end of the interval {@code x1 < x2}
	 * @param x2 is the high end of the interval
	 * @param y11
	 * @param y12
	 * @param y21
	 * @param y22
	 * @return true if at least one polygon has been drawn, false otherwise
	 */
	private boolean drawPolygons(int x1, int x2, int y11, int y12, int y21, int y22) {
		// debugging
		//dbg(drawrect_late_ct + " drawPieces: " + x1 + ", " + x2 
		//		+ ", " + y11 + ", " + y12 + ", " + y21 + ", " + y22 );
		drawRectLateCounter++; // debug, counter
		
		// some constants to improve readability in formulas for yps below
		final int xd = x2 - x1; // length of interval on x-axis
		final int yd1 = y21 - y11;
		final int yd2 = y22 - y12;
		boolean drawn = false;
		
		// go through the interval [x1,x2] and check for possible
		// intervals on the x-axis that are not covered yet.
		// Draw a polygon for each subinterval that can be covered.
		// Loop variable is x1i, starts at x1, upper limit x2 is fixed
		int x1i = x1; // init loop variable
		int x2i;      // initialized inside loop
		int[] intersect;
		while (x1i <= x2) {
			// check if there is an intersection, 
			// if there is none, done, exit the loop, 
			// if there is one, get it as (x1i,x2i)
			// rset represents the to-do list of intervals on the x-axis
			// that are not covered by a polygon yet
			// get one intersection for our interval[x1i,x2] with rset
			intersect = rSet.getIntersection(x1i, x2);
			if (null == intersect)
				break; // exit point for loop
			x1i = intersect[0]; // progress: can not be less than previous x1i
			x2i = intersect[1]; // x1i <= x2i 
			// draw polygon for intersection (x1i,x2i) on x-axis
			// 4 points needed for polygon, 
			// case 1: blocking wallboard: 
			// => rectangle, 2 vertical lines, 2 horizontal lines
			// case 2: wallboard with perspective: 
			// => trapezoid, 2 vertical lines (parallel), 
			// 2 lines directed towards center for correct drawing
			// for code below, in both cases
			// 1st line is vertical because 1st & 2nd point have same x coordinate 
			// 3rd line is vertical because 3rd & 4th point have same x coordinate
			// constant xd == x2-x1, yd1 = y21-y11, and yd2 = y22-y12
			// the additive term for the y coordinate depends on the point's x coordinate
			// polygon covers interval [x1i, x2i] on the x-axis
			int[] xps = { x1i, x1i, x2i+1, x2i+1 };
			// (x1i-x1)/xd is the percentage of what's left of [x1i,x2i] in [x1,x2]
			// (x2i-x2)/xd is the percentage of what's right of [x1i,x2i] in [x1,x2]
			// memo: warning for refactoring: this is integer division
			int[] yps = { y11+(x1i-x1)*yd1/xd,
					y12+(x1i-x1)*yd2/xd+1,
					y22+(x2i-x2)*yd2/xd+1,
					y21+(x2i-x2)*yd1/xd };
			// debug
			//dbg("polygon-x: " + xps[0] + ", " + xps[1] + ", " + xps[2] + ", " + xps[3]) ;
			//dbg("polygon-y: " + yps[0] + ", " + yps[1] + ", " + yps[2] + ", " + yps[3]) ;

			panel.addFilledPolygon(xps, yps, 4);
			//gc.fillPolygon(xps, yps, 4); REPLACED ^

			// for debugging purposes, code will draw a red line around polygon
			// this makes individual walls visible
			/*
			gc.setColor(new Color(240,20,20));
			gc.drawPolygon(xps, yps, 4);
			gc.setColor(seg.getColor());
			*/
			// end debugging
			drawn = true;           // at least one polygon was drawn, memorize for return value
			rSet.remove(x1i, x2i);  // update rset, remove interval [x1i,x2i] from to-do list 
			x1i = x2i+1;            // progress for while loop, value must increase
			
			drawRectWallCounter++; // debug, counter
		}
		return drawn;
	}
	
	////////////////////////////// static methods that do not rely on instance fields //////
	/**
	 * Unscale given value
	 * @param x input value
	 * @return unscaled input
	 */
	final int unscaleViewD(int x) {
		// >> is the signed right shift operator
		// shifts input x in its binary representation
		// 16 times to the right
		// same as divide by 2^16 and discard remainder
		// preserves sign
		return x >> 16;
	}
	/**
	 * Helper method for debugging 
	 * @param str is the message
	 */
	private void dbg(String str) {
		LOGGER.fine(str);
	}

	/**
	 * Trivial class to hold 4 integer values. Used only in FirstPersonDrawer.
	 */
	class RangePair {
		public int x1;
		public int z1; 
		public int x2; 
		public int z2;

		RangePair(int xx1, int zz1, int xx2, int zz2) {
			x1 = xx1;
			z1 = zz1;
			x2 = xx2;
			z2 = zz2;
		}
		/**
		 * Performs a clip3d operation.
		 * @return true if instance variables have been modified, false otherwise
		 */
		public boolean clip3d() {
			// check special cases for quick decision
			if (z1 > -4 && z2 > -4)
				return false;
			if (x1 > -z1 && x2 > -z2)
				return false;
			if (-x1 > -z1 && -x2 > -z2)
				return false;
			// calculate float pair
			final int dx = x2 - x1;
			final int dz = z2 - z1;
			FloatPair fp = new FloatPair(0, 1);
			if (!fp.clipt(-dx - dz, x1 + z1))
				return false;
			if (!fp.clipt(dx - dz, -x1 + z1))
				return false;
			if (!fp.clipt(-dz, z1 - 4))
				return false;
			// if float pair is valid, update instance variables
			if (fp.p2 < 1) {
				x2 = (int) (x1 + fp.p2 * dx);
				z2 = (int) (z1 + fp.p2 * dz);
			}
			if (fp.p1 > 0) {
				x1 += fp.p1 * dx;
				z1 += fp.p1 * dz;
			}
			return true;
		} 
	}
	/**
	 * Trivial class to hold to double values. Used only in FirstPersonDrawer.
	 *
	 */
	class FloatPair {
		public double p1;
		public double p2;

		/**
		 * Constructors, given parameters are stored
		 * @param pp1 stored as p1
		 * @param pp2 stored as p2
		 */
		FloatPair(double pp1, double pp2) {
			p1 = pp1;
			p2 = pp2;
		}
	    /**
	     * Helper method for clip3d
	     * @param denom is the denominator
	     * @param num is the numerator
	     * @return the result of the clipping operation
	     */
		public boolean clipt(int denom, int num) {
			if (denom > 0) {
				double t = num * 1.0 / denom;
				if (t > p2)
					return false;
				if (t > p1)
					p1 = t; // update field
			} else if (denom < 0) {
				double t = num * 1.0 / denom;
				if (t < p1)
					return false;
				if (t < p2)
					p2 = t; // update field
			} else if (num > 0)
				return false;
			return true;

		}
	}
}
