package com.example.amazebyconnormackinnon.gui;
/**
 * This class contains all constants that are used in the maze package 
 * and shared among several classes.
 * 
 * @author Peter Kemper
 *
 */
public class Constants {
	// The panel used to display the maze has a fixed dimension
	public static final int VIEW_WIDTH = 750;
	public static final int VIEW_HEIGHT = 1200;
	public static final int MAP_UNIT = 128;
	public static final int VIEW_OFFSET = MAP_UNIT/8;
	public static final int STEP_SIZE = MAP_UNIT/4;
	// Skill-level 
	// The user picks a skill level between 0 - 9, a-f 
	// The following arrays transform this into corresponding dimensions (x,y)
	// for the resulting maze as well as the number of rooms and parts
	// Example: level 3 is a 20 x 15 maze with at most 3 randomly positioned rooms
	// Special case: level 0 has 0 rooms 
	// Quadratic mazes: levels 0, 1, 2, 5, 7, 8, 9, 10 yield squares
	public static final int[] SKILL_X =     { 4, 12, 15, 20, 25, 25, 35, 35, 40, 60, 70, 80, 90, 110, 150, 300 };
	public static final int[] SKILL_Y =     { 4, 12, 15, 15, 20, 25, 25, 35, 40, 60, 70, 75, 75,  90, 120, 240 };
	public static final int[] SKILL_ROOMS = { 0,  2,  2,  3,  4,  5, 10, 10, 20, 45, 45, 50, 50,  60,  80, 160 };
	public static final int[] SKILL_PARTCT = { 60, 600, 900, 1200, 2100, 2700, 3300,
	5000, 6000, 13500, 19800, 25000, 29000, 45000, 85000, 85000*4 };
	public static final int MAX_SKILL_LEVEL = 15;
		
	// Possible user input  
	public enum UserInput {RETURNTOTITLE, START, UP, DOWN, LEFT, RIGHT, JUMP, TOGGLELOCALMAP, TOGGLEFULLMAP, TOGGLESOLUTION, ZOOMIN, ZOOMOUT }

	// fixing a value matching the escape key
	public static final int ESCAPE = 27;
}
