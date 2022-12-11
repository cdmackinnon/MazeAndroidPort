/**
 * 
 */
package com.example.amazebyconnormackinnon.generation;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.amazebyconnormackinnon.gui.Constants;

/**
 * DefaultOrder provides a basic implementation for an order to 
 * interact with a MazeFactory by implementing the Order interface
 * and to cater to a client class of the factory that wants
 * to generate a maze.
 * 
 * The expected usage scenario is that a client class uses set methods
 * to specify what maze is wanted: the size of the maze, if it needs
 * to be perfect and thus have no rooms, 
 * the builder algorithm, and the seed of the random number generator
 * that shall be used.
 * The MazeFactory uses get methods to obtain this specification
 * information and then provides updates on the level of progress
 * that has been made during the maze generation and finally 
 * delivers a reference to the computed maze object.
 * 
 * By providing the seed for the random number generation
 * together with all other configuration parameters for
 * an algorithm to compute a maze by the MazeFactory, 
 * the expectation is that the each order corresponds
 * to a unique maze in a 1-1 manner. So, given the same
 * order again, the MazeFactory is expected to deliver
 * another but equal maze.
 * 
 * @author Peter Kemper
 *
 */
public class DefaultOrder implements Order {
	/**
	 * The logger is used to track the interaction between
	 * collaborating classes.
	 * Collaborators are the UI and the MazeFactory.
	 * Level FINE: logs information flow in and out of its fields.
	 */
	private static final Logger LOGGER = Logger.getLogger(DefaultOrder.class.getName());
	
	/**
	 * The skill level represents the size of the maze.
	 * Range: {@literal {0,1,...,15} }, with 0 being the smallest maze.
	 * Class Constants contains information how
	 * skill level translates into width and height dimensions
	 * and the maximum number of rooms in a maze of that size.
	 * The MazeFactory needs this information to produce a 
	 * corresponding maze.
	 */
	int skillLevel;
	
	/**
	 * The builder denotes which algorithm shall be used
	 * to generate the maze. 
	 * The MazeFactory needs this information to produce a 
	 * corresponding maze.
	 */
	Builder builder;
	
	/**
	 * A maze can be perfect or not.
	 * The value denotes that a maze is requested to be
	 * perfect (or not).
	 * A perfect maze represents a spanning tree
	 * and there are not circles in the maze. The absence of
	 * circles implies that there is exactly one path from 
	 * one position to another.
	 * 
	 * This property is only guaranteed in the absence of 
	 * rooms, so for here, a perfect maze has no rooms.
	 * The field perfectMaze informs the factory whether
	 * to create rooms or not.
	 * 
	 * The MazeFactory needs this information to produce a 
	 * corresponding maze.
	 */
	boolean perfectMaze;
	
	/**
	 * The pseudo random number generation should be started with
	 * this seed value. This field represents the value
	 * that is used to configure the random number generator
	 * for the generation of a random maze. 
	 * 
	 * The MazeFactory needs this information to produce a 
	 * corresponding maze.
	 */
	int seed;
	
	/**
	 * The maze field provides a reference to the maze
	 * that the factory generates. 
	 * This field is null, when an order is given
	 * to the MazeFactory.
	 * This field is set by the MazeFactory once
	 * the maze generation is complete. 
	 */
	Maze maze;
	
	/**
	 * Progress denotes the current progress value.
	 * Range: {@literal {0,1,...,100} }, with 0 being
	 * the initial situation of no progress, 100 being
	 * the situation when the maze generation is close
	 * to completion or completed.
	 * This field is set by the MazeFactory and updated
	 * during the maze generation.
	 */
	int progress;
	
	/**
	 * Constructor that initializes fields to meaningful default values.
	 * A newly constructed object is sufficiently configured to 
	 * make the MazeFactory deliver a fully functional maze. 
	 */
	public DefaultOrder() {
		// select values 
		// skillLevel = 0; // smallest possible maze
		// builder = Builder.DFS; // use the Depth-First-Search algorithm
		// perfectMaze = true; // no rooms
		// seed = 13; // some arbitrary starting value
		// maze = null; // there is no maze object at this moment
		// progress = 0; // 0 is an ok starting value for progress
		init(0, Builder.DFS, true, 13, null, 0);
		
	}
	/**
	 * Constructor with a given size of maze that initializes fields to meaningful default values.
	 * A newly constructed object is sufficiently configured to 
	 * make the MazeFactory deliver a fully functional maze. 
	 * @param skillLevel the selected size of the maze, {@literal 0 <= skillLevel <= 15}
	 */
    public DefaultOrder(int skillLevel) {
    	init(skillLevel, Builder.DFS, true, 13, null, 0);
    }
	/**
	 * Constructor that initializes fields to the given values.
	 * If parameter values are chosen accordingly, 
	 * a newly constructed object is sufficiently configured to 
	 * make the MazeFactory deliver a fully functional maze. 
	 * @param skillLevel the selected size of the maze, {@literal 0 <= skillLevel <= 15}
	 * @param builder the selected builder algorithm
	 * @param perfect the maze shall be perfect or not
	 * @param seed the seed for the random number generator
	 */    
    public DefaultOrder(int skillLevel, Builder builder, boolean perfect, int seed) {
    	// assign parameter values
    	// resulting maze reference is null at this point
    	// current level of progress is 0
        init(skillLevel, builder, perfect, seed, null, 0);
    }
    
    /**
     * Sets all fields as given. 
     * The method is used to establish a consistent initialization across multiple constructor methods.
     * @param size the skill level
     * @param builder the builder algorithm
     * @param perfect if the maze is perfect or not
     * @param seed the random number generator seed
     * @param maze the maze
     * @param progress the current level of progress
     */
    private void init(int size, Builder builder, boolean perfect, int seed, Maze maze, int progress) {
    	setSkillLevel(size); // benefits from checking range constraints
    	this.builder = builder;
    	perfectMaze = perfect;
    	this.seed = seed;
    	this.maze = maze;
    	this.progress = progress; // do not use updateProgress method, subject to override in subclasses!
    	LOGGER.fine("Constructor asks for maze of size " + skillLevel 
    			+ ", algorithm " + builder 
    			+ ", perfect? " + perfectMaze 
    			+ ", seed " + seed 
    			+ ", resulting maze " + maze 
    			+ ", progress "  + progress);
    }
	
    ///////////// getters and setters ////////////////////////////////
	/**
	 * @return the requested size of the maze, {@literal 0 <= value <= 15}
	 */
	@Override
	public int getSkillLevel() {
		LOGGER.fine("provide skill level info: " + skillLevel);
		return skillLevel;
	}
	/**
	 * Selects the skill level, the requested size of the maze.
	 * @param skillLevel the size of the maze, {@literal 0 <= skillLevel <= 15}
	 */
	public void setSkillLevel(int skillLevel) {
		LOGGER.fine("receive skill level info: " + skillLevel);
		// Note: skillLevel is used as an index for arrays such as Constants.SKILL_X.
		// These arrays only support 0,1, ..., 15. 
		if (0 <= skillLevel && skillLevel <= Constants.MAX_SKILL_LEVEL) {
			this.skillLevel = skillLevel;
		}
		else {
			this.skillLevel = 0; // set to a default value
			LOGGER.severe("range violation, " + skillLevel + " outside 0,1,...,15 range. Use 0 as default instead.");
		}
			
	}

	/**
	 * @return the requested builder algorithm
	 */
	@Override
	public Builder getBuilder() {
		LOGGER.fine("provide builder info: " + builder);
		return builder;
	}
	/**
	 * Selects the builder algorithm. 
	 * @param builder the algorithm to be used by the MazeFactory
	 */
	public void setBuilder(Builder builder) {
		LOGGER.fine("receive builder info: " + builder);
		this.builder = builder;
	}

	/**
	 * @return true if the requested maze shall be perfect, false otherwise
	 */
	@Override
	public boolean isPerfect() {
		LOGGER.fine("provide info about the maze being perfect, maze is perfect? " + perfectMaze);
		return perfectMaze;
	}
	/**
	 * Selects if the requested maze should be perfect. 
	 * @param perfectMaze if true the maze should be perfect and have no rooms, otherwise rooms are allowed and circles can be present in the maze.
	 */
	public void setPerfect(boolean perfectMaze) {
		LOGGER.fine("receive info about the maze being perfect, maze is perfect? " + perfectMaze);
		this.perfectMaze = perfectMaze;
	}

	/**
	 * Selects the seed to be used for the random number stream during maze generation. 
	 * @param seed the seed for the random number generation
	 */
	public void setSeed(int seed) {
		LOGGER.fine("receive info about seed for random number generation: " + seed);
		this.seed = seed;
	}
	/**
	 * @return the seed to be used for the random number generation during maze generation
	 */
	@Override
	public int getSeed() {
		LOGGER.fine("provide info about seed for random number generation: " + seed);
		return seed;
	}


	/**
	 * @return a reference to the computed maze once it is computed, null otherwise
	 */
	public Maze getMaze() {
		LOGGER.log((maze == null)? Level.WARNING: Level.FINE,"provide reference to generated maze: " + maze);
		return maze;
	}
	/**
	 * Delivers and provides a reference to the generated maze. The maze is complete and
	 * ready to be played by the user.
	 * @param maze the maze generated by the MazeFactory
	 */
	@Override
	public void deliver(Maze maze) {
		LOGGER.fine("receive reference to generated maze: " + maze);
		this.maze = maze;

	}

	/**
	 * @return the current level of progress that has been made so far, {@literal 0 <= value <= 100}
	 */
	public int getProgress() {
		LOGGER.fine("provide info about progress (completion of maze generation): " + progress);
		return progress;
	}
	/**
	 * Provides an update for the current level of progress that has been made so far in the maze generation. 
	 * @param percentage the current level of progress,  {@literal 0 <= value <= 100}
	 */
	@Override
	public void updateProgress(int percentage) {
		LOGGER.fine("receive info about progress (completion of maze generation): " + percentage);
		if (0 <= percentage && percentage <= 100) {
			progress = percentage;
		}
		else {
			progress = (percentage < 0) ? 0 : 100;
			LOGGER.severe("range violation, " + percentage + " outside 0,1,...,100 range. Used closest legit value for mitigation.");
		}
	}

}
