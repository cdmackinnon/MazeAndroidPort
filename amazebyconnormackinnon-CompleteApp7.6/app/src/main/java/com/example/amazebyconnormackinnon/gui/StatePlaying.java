package com.example.amazebyconnormackinnon.gui;

import com.example.amazebyconnormackinnon.gui.Constants.UserInput;

import java.util.logging.Logger;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Floorplan;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.GameInterface.MazePanel;


/**
 * Class handles the user interaction
 * while the game is in the third stage
 * where the user plays the game.
 * This class is part of a state pattern for the
 * Controller class.
 * 
 * It implements a state-dependent behavior that controls
 * the display and reacts to key board input from a user. 
 * At this point user keyboard input is first dealt
 * with a key listener in control and then 
 * and then handed over 
 * by way of the handleUserInput method.
 * 
 * Responsibilities:
 * Keep track of the current position and direction in the game.
 * Show the first person view and the map view,
 * Accept input for manual operation (left, right, up, down etc),  
 * Update the graphics, recognize termination.
 *
 * This code contains refactored code from Maze.java by Paul Falstad, 
 * www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class StatePlaying {
	/**
	 * The logger is used to track execution and report issues.
	 * Collaborators are the UI and the MazeFactory.
	 * Level INFO: logs mitigated issues such as unexpected user input
	 * Level FINE: logs information flow in and out of its fields.
	 */
	private static final Logger LOGGER = Logger.getLogger(StatePlaying.class.getName());

	/**
	 * The first person view determines what is seen on the screen with a first person perspective.
	 * This includes the background of two rectangles that cover the whole area.
	 * Drawing polygons for walls is the main contribution.
	 */
	private FirstPersonView firstPersonView;
	/**
	 * The view determines what is seen on the screen for a top view.
	 * Drawing the maze from above as a lines for walls, the current position and direction, 
	 * and the solution as a yellow line is the man contribution.
	 * The map is drawn on top of the first person view.
	 */
	private Map mapView;
	/**
	 * The compass rose provides additional guidance on the current direction
	 * and visualizes it with a compass rose. It draws on top of the 
	 * first person view. As map and compass rose compete for space on the 
	 * screen, one can show at most one of the two at any point in time.
	 */
	private CompassRose cr; 
	   
    /**
     * The panel is the capability to draw on the screen.
     */
    private MazePanel panel;
    
    /**
     * Control is the context class of the State pattern.
     * The reference is needed to pull some pieces of information
     * plus switch control to the next state, which 
     * is the maze generating state.
     */
    //private Control control;


    /**
     * Maze holds the main information on where walls are.
     */
    Maze maze ; 

    private boolean showMaze;           // toggle switch to show overall maze on screen
    private boolean showSolution;       // toggle switch to show solution in overall maze on screen
    private boolean mapMode; // true: display map of maze, false: do not display map of maze
    // mapMode is toggled by user keyboard input, causes a call to drawMap during play mode

    // current position and direction with regard to MazeConfiguration
    int px, py ; // current position on maze grid (x,y)
    CardinalDirection cd;
    
  
    Floorplan seenCells; // a matrix with cells to memorize which cells are visible from the current point of view
    // the FirstPersonView obtains this information and the Map uses it for highlighting currently visible walls on the map
    
    // debug stuff
    //private boolean deepdebug = false;
    //private boolean allVisible = false;
    //private boolean newGame = false;

    /** 
     * Started is used to enforce ordering constraint on method calls.
     * start() must be called before keyDown()
     * to make sure control variable has been set.
     * initial setting: false, start sets it to true.
     */
    boolean started;

    /**
     * String in the case of an unreliable sensor indicating which sensors
     * are functional. Initially null; must be set with setSensorString()
     * Can be returned with getSensorString();
     */
    private String sensorString;

    public void setSensorString(String sensorString) {
        this.sensorString = sensorString;
    }

    public String getSensorString(){
        return sensorString;
    }

    /**
     * Constructor uses default settings but does not deliver a fully operation instance,
     * requires a call to start() and setMaze().
     */
    public StatePlaying() {
    	// initialization of some fields is delayed and done in start method
    	firstPersonView = null; // initialized in start method
    	mapView = null; // initialized in start method
    	panel = null; // provided by start method
    	//control = null; // provided by start method
    	started = false; // method start has not been called yet

    	maze = null; // provided by set method

    	// visibility settings, default values
    	showMaze = false ;
    	showSolution = false ;
    	mapMode = false;

    	// initial position and direction in absence of maze
    	// position at [0,0], direction is east
    	px = 0;
    	py = 0;
    	cd = CardinalDirection.East;

    	seenCells = null;
    	cr = null;
    }
    /**
     * Provides the maze to play.
     * @param maze a fully operational complete maze ready to play
     */
    public void setMaze(Maze maze) {
        this.maze = maze;
    }
    /**
     * Start the actual game play by showing the playing screen.
     * If the panel is null, all drawing operations are skipped.
     * This mode of operation is useful for testing purposes, 
     * i.e., a dryrun of the game without the graphics part.
     *
     * @param panel is part of the UI and visible on the screen, needed for drawing
     */
                        //@param controller provides access to the controller this state resides in
                        //Control controller
    public void start(MazePanel panel) {
    	assert null != maze : "StatePlaying.start: maze must exist!";
    	
        started = true;
        // keep the reference to the controller to be able to call method to switch the state
        //control = controller;
        // keep the reference to the panel for drawing
        this.panel = panel;
        //
        // adjust visibility settings to default values
        showMaze = false ;
        showSolution = false ;
        mapMode = false;
        
        // adjust internal state of maze model
        // init data structure for visible walls
        seenCells = new Floorplan(maze.getWidth()+1,maze.getHeight()+1) ;
        // set the current position and direction consistently with the viewing direction
        setPositionDirectionViewingDirection();

        if (panel != null) {
        	startDrawer();
        }
        else {
        	// else: dry-run without graphics, most likely for testing purposes
        	printWarning();
        }
    }
    /**
     * Initializes the drawer for the first person view
     * and the map view and then draws the initial screen
     * for this state.
     */
	protected void startDrawer() {
		cr = new CompassRose();
        cr.paintComponent(panel);
		cr.setPositionAndSize(panel.getCanvas().getWidth()/2,
				(int)(panel.getCanvas().getHeight()*.2),160);

        //Constants.VIEW_WIDTH
        //Constants.VIEW_HEIGHT

		firstPersonView = new FirstPersonView(panel.getCanvas().getWidth(),
				panel.getCanvas().getHeight(), Constants.MAP_UNIT,
				Constants.STEP_SIZE, seenCells, maze.getRootnode()) ;
		
		mapView = new Map(seenCells, 15, maze) ;
		// draw the initial screen for this state
		draw(cd.angle(), 0);
	}
    /**
     * Internal method to set the current position, the direction
     * and the viewing direction to values consistent with the 
     * given maze.
     */
	private void setPositionDirectionViewingDirection() {
        int[] start = maze.getStartingPosition() ;
        setCurrentPosition(start[0],start[1]) ;
        cd = CardinalDirection.East;
	}
 
	/**
     * Switches the controller to the final screen
     * @param pathLength gives the length of the path
     */
    public void switchFromPlayingToWinning(int pathLength) {
    	// need to instantiate and configure the winning state
        //StateWinning currentState = new StateWinning();
        
        // The playing state needs 
        // 1) the path length
        // 
        //currentState.setPathLength(pathLength);
        
        LOGGER.fine("Control switches from playing to winning screen, game completed.");
        
        // update the context class with the new state
        // and hand over control to the new state
       // control.setState(currentState);
        //currentState.start(control, panel);
    }
    
    /**
     * Switches the controller to the initial screen.
     */
    boolean flag = false;
    public void switchToTitle() {
       	// need to instantiate and configure the title state
       // StateTitle currentState = new StateTitle();
        flag = true;
        LOGGER.fine("Control switches from playing to title screen, game play interrupted.");
        
        // update the context class with the new state
        // and hand over control to the new state

       // control.setState(currentState);
        //currentState.start(control, panel);
    }
    
    /**
     * The method provides an appropriate response to user keyboard input. 
     * The control calls this method to communicate input and delegate its handling.
     * Method requires {@link #start(Control, MazePanel) start} to be
     * called before.
     * @param userInput provides the feature the user selected
     * @param value is not used in this state, exists only for consistency across State classes
     * @return false if not started yet otherwise true
     */
    public boolean handleUserInput(UserInput userInput, int value) {
        if (flag){
            return true;
        }
    	// user input too early, not sure how this could happen
        if (!started) {
        	LOGGER.info("Premature keyboard input:" + userInput + "with value " + value + ", ignored for mitigation");
            return false;
        }

        // react to input for directions and interrupt signal (ESCAPE key)  
        // react to input for displaying a map of the current path or of the overall maze (on/off toggle switch)
        // react to input to display solution (on/off toggle switch)
        // react to input to increase/reduce map scale
        switch (userInput) {
        case START: // misplaced, do nothing
            break;
        case UP: // move forward
        	LOGGER.fine("Move 1 step forward");
            walk(1);
            // check termination, did we leave the maze?
            if (isOutside(px,py)) {
            	// TODO: provide actual path length
                switchFromPlayingToWinning(0);
            }
            break;
        case LEFT: // turn left
        	LOGGER.fine("Turn left");
            rotate(1);
            break;
        case RIGHT: // turn right
        	LOGGER.fine("Turn right");
            rotate(-1);
            break;
        case DOWN: // move backward
        	LOGGER.fine("Move 1 step backward");
            walk(-1);
            // check termination, did we leave the maze?
            if (isOutside(px,py)) {
            	// TODO: provide actual path length
                switchFromPlayingToWinning(0);
            }
            break;
        case RETURNTOTITLE: // escape to title screen
            switchToTitle();
            break;
        case JUMP: // make a step forward even through a wall
        	LOGGER.fine("Jump 1 step forward");
            // go to position if within maze
        	int[] tmpDxDy = cd.getDxDyDirection();
        	if (maze.isValidPosition(px + tmpDxDy[0], py + tmpDxDy[1])) {
                setCurrentPosition(px + tmpDxDy[0], py + tmpDxDy[1]) ;
                draw(cd.angle(), 0) ;
            }
            break;
        case TOGGLELOCALMAP: // show local information: current position and visible walls
            // precondition for showMaze and showSolution to be effective
            // acts as a toggle switch
            mapMode = !mapMode;         
            draw(cd.angle(), 0) ; 
            break;
        case TOGGLEFULLMAP: // show the whole maze
            // acts as a toggle switch
            showMaze = !showMaze;       
            draw(cd.angle(), 0) ; 
            break;
        case TOGGLESOLUTION: // show the solution as a yellow line towards the exit
            // acts as a toggle switch
            showSolution = !showSolution;       
            draw(cd.angle(), 0) ;
            break;
        case ZOOMIN: // zoom into map
        	mapView.incrementMapScale(); 
            draw(cd.angle(), 0) ;
            break ;
        case ZOOMOUT: // zoom out of map
        	mapView.decrementMapScale(); 
            draw(cd.angle(), 0) ; 
            break ;
        } // end of internal switch statement for playing state
        return true;
    }
    /**
     * Draws the current content on panel to show it on screen.
     * @param angle the current viewing angle, east == 0 degrees, south == 90, west == 180, north == 270
     * @param walkStep a counter for intermediate steps within a single step forward or backward
     */
    protected void draw(int angle, int walkStep) {
    	 
    	if (panel == null) {
    		printWarning();
    		return;
    	}
    	// draw the first person view and the map view if wanted
    	firstPersonView.draw(panel, px, py, walkStep, angle, 
    			maze.getPercentageForDistanceToExit(px, py)) ;
        if (isInMapMode()) {
			mapView.draw(panel, px, py, angle, walkStep,
					isInShowMazeMode(),isInShowSolutionMode()) ;
		}
		// update the screen with the buffer graphics
        panel.commit(); ;
    }

    /**
     * Prints the warning about a missing panel only once
     */
    boolean printedWarning = false;
    protected void printWarning() {
    	if (printedWarning)
    		return;
    	LOGGER.info("No panel for drawing during executing, dry-run game without graphics!");
    	printedWarning = true;
    }
    ////////////////////////////// set methods ///////////////////////////////////////////////////////////////
    ////////////////////////////// Actions that can be performed on the maze model ///////////////////////////
    protected void setCurrentPosition(int x, int y) {
        px = x ;
        py = y ;
    }
 
    ////////////////////////////// get methods ///////////////////////////////////////////////////////////////
    protected int[] getCurrentPosition() {
        int[] result = new int[2];
        result[0] = px;
        result[1] = py;
        return result;
    }
    public CardinalDirection getCurrentDirection() {
        return cd;
    }
    boolean isInMapMode() { 
        return mapMode ; 
    } 
    boolean isInShowMazeMode() { 
        return showMaze ; 
    } 
    boolean isInShowSolutionMode() { 
        return showSolution ; 
    } 
    public Maze getMaze() {
        return maze ;
    }
    //////////////////////// Methods for move and rotate operations ///////////////
    
    /**
     * Determines if one can walk in the given direction
     * @param dir is the direction of interest, either 1 or -1
     * @return true if there is no wall in this direction, false otherwise
     */
    protected boolean wayIsClear(int dir) {
        switch (dir) {
        case 1: // forward
        	return !maze.hasWall(px, py, cd);
        case -1: // backward
            return !maze.hasWall(px, py, cd.oppositeDirection());
        default:
            throw new RuntimeException("Unexpected direction value: " + dir);
        }
    }
    /**
     * Draws and waits. Used to obtain a smooth appearance for rotate and move operations
     */
    private void slowedDownRedraw(int angle, int walkStep) {
    	LOGGER.fine("Drawing intermediate figures: angle " + angle + ", walkStep " + walkStep);
        draw(angle, walkStep) ;
        try {
            Thread.sleep(25);
        } catch (Exception e) { 
        	// may happen if thread is interrupted
        	// no reason to do anything about it, ignore exception
        }
    }
 	
    /**
     * Performs a rotation with 4 intermediate views, 
     * updates the screen and the internal direction
     * @param dir for current direction, values are either 1 or -1
     */
    private synchronized void rotate(int dir) {
        final int originalAngle = cd.angle();//angle;
        final int steps = 4;
        int angle = originalAngle; // just in case for loop is skipped
        for (int i = 0; i != steps; i++) {
            // add 1/4 of 90 degrees per step 
            // if dir is -1 then subtract instead of addition
            angle = originalAngle + dir*(90*(i+1))/steps; 
            angle = (angle+1800) % 360;
            // draw method is called and uses angle field for direction
            // information.
			slowedDownRedraw(angle, 0);
        }
        // update maze direction only after intermediate steps are done
        // because choice of direction values are more limited.
        cd = CardinalDirection.getDirection(angle);
        logPosition(); // debugging
        drawHintIfNecessary(); 
    }
	
    /**
     * Moves in the given direction with 4 intermediate steps,
     * updates the screen and the internal position
     * @param dir, only possible values are 1 (forward) and -1 (backward)
     */
    private synchronized void walk(int dir) {
    	// check if there is a wall in the way
        if (!wayIsClear(dir))
            return;
        int walkStep = 0;
        // walkStep is a parameter of FirstPersonView.draw()
        // it is used there for scaling steps
        // so walkStep is implicitly used in slowedDownRedraw
        // which triggers the draw operation in 
        // FirstPersonView and Map
        for (int step = 0; step != 4; step++) {
            walkStep += dir;
            slowedDownRedraw(cd.angle(), walkStep);
        }
        // update position to neighbor
        int[] tmpDxDy = cd.getDxDyDirection();
        setCurrentPosition(px + dir*tmpDxDy[0], py + dir*tmpDxDy[1]) ;
        logPosition(); // debugging
        drawHintIfNecessary();
        panel.commit();
    }

    /**
     * Checks if the given position is outside the maze
     * @param x coordinate of position
     * @param y coordinate of position
     * @return true if position is outside, false otherwise
     */
    private boolean isOutside(int x, int y) {
        return !maze.isValidPosition(x, y) ;
    }
    /**
     * Draw a visual cue to help the user unless the 
     * map is on display anyway. 
     * This is the map if current position faces a dead end
     * otherwise it is a compass rose.
     */
    private void drawHintIfNecessary() {
    	if (isInMapMode())
    		return; // no need for help
    	// in testing environments, there is sometimes no panel to draw on
    	// or the panel is unable to deliver a graphics object
    	// check this and quietly move on if drawing is impossible
    	if (panel == null ){
    		printWarning();
    		return;
    	}
    	// if current position faces a dead end, show map with solution
    	// for guidance
    	if (maze.isFacingDeadEnd(px, py, cd)) {
        	//System.out.println("Facing deadend, help by showing solution");
        	mapView.draw(panel, px, py, cd.angle(), 0, true, true) ;
        }
    	else {
    		// draw compass rose
    		cr.setCurrentDirection(cd);
            cr.paintComponent(panel);
    		//cr.paintComponent(panel.getBufferGraphics());
    	}
    	panel.commit();
    }
 
    /////////////////////// Methods for debugging ////////////////////////////////
    
    private void dbg(String str) {
        LOGGER.fine(str);
    }
    
    private void logPosition() {
    	 int[] tmpDxDy = cd.getDxDyDirection();
    	 LOGGER.fine("x="+px+",y="+py+",dx="+tmpDxDy[0]+",dy="+tmpDxDy[1]+",angle="+cd.angle());
    	//LOGGER.fine("x="+px+",y="+py+",dx="+dx+",dy="+dy+",angle="+angle);
    	/*
        if (!deepdebug)
            return;
        dbg("x="+viewx/Constants.MAP_UNIT+" ("+
                viewx+") y="+viewy/Constants.MAP_UNIT+" ("+viewy+") ang="+
                angle+" dx="+dx+" dy="+dy+" "+viewdx+" "+viewdy);
                */
    }

    public int[] getPosition(){
        int[] pos = {px,py};
        return pos;
    }
    
}



