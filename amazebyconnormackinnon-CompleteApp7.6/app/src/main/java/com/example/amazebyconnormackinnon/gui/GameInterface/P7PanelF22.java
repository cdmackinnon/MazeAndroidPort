package com.example.amazebyconnormackinnon.gui.GameInterface;

/**
 * Provides an adapter for a graphics object for the first person view
 * and the map view to draw on.  
 * To its clients it offers the notion of an editor:
 * one can clear it by adding the background, 
 * then put graphical entities to draw into it, 
 * and finally commit the complete drawing to the UI.
 * The naming is technical as it is for Project 7
 * and should serve as a specification for the MazePanel class.
 * 
 * Note that methods for drawX in AWT have a corresponding
 * method addX with same parameters here. The naming emphasizes
 * that the P7Panel accumulates bits and pieces for an 
 * overall drawing that is then shown on the screen
 * when the editor's content is complete and committed
 * for drawing.
 * 
 * The documentation includes guidance which AWT method is encapsulated
 * or can be substituted by which interface method.
 * 
 * @author Peter Kemper
 *
 */
public interface P7PanelF22 {
	/**
	 * Commits all accumulated drawings to the UI.
	 * Substitute for MazePanel.update method. 
	 */
	public void commit();
	
	/**
	 * Tells if instance is able to draw. This ability depends on the
	 * context, for instance, in a testing environment, drawing
	 * may be not possible and not desired.
	 * Substitute for code that checks if graphics object for drawing is not null.
	 * @return true if drawing is possible, false if not.
	 */
	public boolean isOperational();
	
	/**
	 * Sets the color for future drawing requests. The color setting
	 * will remain in effect till this method is called again and
	 * with a different color.
	 * Substitute for Graphics.setColor method.
	 * @param argb gives the alpha, red, green, and blue encoded value of the color
	 */
	public void setColor(int argb);
	
    /**
     * Returns the ARGB value for the current color setting. 
     * @return integer ARGB value
     */
    public int getColor();
    
   
	/**
	 * Draws two solid rectangles to provide a background.
	 * Note that this also erases any previous drawings.
	 * The color setting adjusts to the distance to the exit to 
	 * provide an additional clue for the user.
	 * Colors transition from black to gold and from grey to green.
	 * Substitute for FirstPersonView.drawBackground method.
	 * @param percentToExit gives the distance to exit
	 */
	public void addBackground(float percentToExit);

    /**
     * Adds a filled rectangle. 
     * The rectangle is specified with the {@code (x,y)} coordinates
     * of the upper left corner and then its width for the 
     * x-axis and the height for the y-axis.
     * Substitute for Graphics.fillRect() method
     * @param x is the x-coordinate of the top left corner
     * @param y is the y-coordinate of the top left corner
     * @param width is the width of the rectangle
     * @param height is the height of the rectangle
     */
    public void addFilledRectangle(int x, int y, int width, int height);
    
    /**
     * Adds a filled polygon. 
     * The polygon is specified with {@code (x,y)} coordinates
     * for the n points it consists of. All x-coordinates
     * are given in a single array, all y-coordinates are
     * given in a separate array. Both arrays must have 
     * same length n. The order of points in the arrays
     * matter as lines will be drawn from one point to the next
     * as given by the order in the array.
     * Substitute for Graphics.fillPolygon() method
     * @param xPoints are the x-coordinates of points for the polygon
     * @param yPoints are the y-coordinates of points for the polygon
     * @param nPoints is the number of points, the length of the arrays
     */
    public void addFilledPolygon(int[] xPoints, int[] yPoints, int nPoints);
    
    /**
     * Adds a polygon.
     * The polygon is not filled. 
     * The polygon is specified with {@code (x,y)} coordinates
     * for the n points it consists of. All x-coordinates
     * are given in a single array, all y-coordinates are
     * given in a separate array. Both arrays must have 
     * same length n. The order of points in the arrays
     * matter as lines will be drawn from one point to the next
     * as given by the order in the array.
     * Substitute for Graphics.drawPolygon method
     * @param xPoints are the x-coordinates of points for the polygon
     * @param yPoints are the y-coordinates of points for the polygon
     * @param nPoints is the number of points, the length of the arrays
     */
    public void addPolygon(int[] xPoints, int[] yPoints, int nPoints);
    
    /**
     * Adds a line. 
     * A line is described by {@code (x,y)} coordinates for its 
     * starting point and its end point. 
     * Substitute for Graphics.drawLine method
     * @param startX is the x-coordinate of the starting point
     * @param startY is the y-coordinate of the starting point
     * @param endX is the x-coordinate of the end point
     * @param endY is the y-coordinate of the end point
     */
    public void addLine(int startX, int startY, int endX, int endY);
    
    /**
     * Adds a filled oval.
     * The oval is specified with the {@code (x,y)} coordinates
     * of the upper left corner and then its width for the 
     * x-axis and the height for the y-axis. An oval is
     * described like a rectangle.
     * Substitute for Graphics.fillOval method  
     * @param x is the x-coordinate of the top left corner
     * @param y is the y-coordinate of the top left corner
     * @param width is the width of the oval
     * @param height is the height of the oval
     */
    public void addFilledOval(int x, int y, int width, int height);
    /**
     * Adds the outline of a circular or elliptical arc covering the specified rectangle.
     * The resulting arc begins at startAngle and extends for arcAngle degrees, 
     * using the current color. Angles are interpreted such that 0 degrees 
     * is at the 3 o'clock position. A positive value indicates a counter-clockwise 
     * rotation while a negative value indicates a clockwise rotation.
     * The center of the arc is the center of the rectangle whose origin is 
     * (x, y) and whose size is specified by the width and height arguments.
     * The resulting arc covers an area width + 1 pixels wide 
     * by height + 1 pixels tall.
     * The angles are specified relative to the non-square extents of 
     * the bounding rectangle such that 45 degrees always falls on the 
     * line from the center of the ellipse to the upper right corner of 
     * the bounding rectangle. As a result, if the bounding rectangle is 
     * noticeably longer in one axis than the other, the angles to the start 
     * and end of the arc segment will be skewed farther along the longer 
     * axis of the bounds.
     * Substitute for Graphics.drawArc method 
     * @param x the x coordinate of the upper-left corner of the arc to be drawn.
     * @param y the y coordinate of the upper-left corner of the arc to be drawn.
     * @param width the width of the arc to be drawn.
     * @param height the height of the arc to be drawn.
     * @param startAngle the beginning angle.
     * @param arcAngle the angular extent of the arc, relative to the start angle.
     */
    public void addArc(int x, int y, int width, int height, int startAngle, int arcAngle) ;
    /**
     * Adds a string at the given position.
     * Substitute for CompassRose.drawMarker method 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param str the string
     */
    public void addMarker(float x, float y, String str) ;
	/**
     * An enumerated type to match 1-1 the awt.RenderingHints used
     * in CompassRose and MazePanel.
     */
    enum P7RenderingHints { KEY_RENDERING, VALUE_RENDER_QUALITY, KEY_ANTIALIASING, VALUE_ANTIALIAS_ON, KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR } ;
    /**
     * Sets the value of a single preference for the rendering algorithms.
     * It internally maps given parameter values into corresponding java.awt.RenderingHints
     * and assigns that to the internal graphics object. 
     * Hint categories include controls for rendering quality
     * and overall time/quality trade-off in the rendering process.
     * 
     * Refer to the awt RenderingHints class for definitions of some common keys and values.
     * 
     * Note for Android: start with an empty default implementation.
     * Postpone any implementation efforts till the Android default rendering 
     * results in unsatisfactory image quality.
     * 
     * @param hintKey the key of the hint to be set.
     * @param hintValue the value indicating preferences for the specified hint category.
     */
    public void setRenderingHint(P7RenderingHints hintKey, P7RenderingHints hintValue);
}
 
