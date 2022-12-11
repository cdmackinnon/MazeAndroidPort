package com.example.amazebyconnormackinnon.generation;

/**
 * Represents absolute directions as for a map to match with the orientation
 * on the screen, i.e. North is no the top of the screen.
 * In addition to limit the set of possible values, this enum supports operations
 * such as rotateClockwise and getOppositeDirection.
 * Translation methods between cardinal directions and 
 * dx,dy pairs are supported to facilitate its integration in current code base.
 * 
 * Current mapping between cardinal directions and (dx,dy)
 * east  = (1,0)
 * south = (0,1)
 * west  = (-1,0)
 * north = (0,-1)
 * 
 * Status: March 2016, coded, working but north/south upside down in graphics
 * Inconsistency: 
 * Cells.java: (0,0) at top-left corner, y coordinate increases downwards
 * MapDrawer: (0,0) at bottom-left corner, y coordinate increases upwards
 * 
 * @author pk
 *
 */
public enum CardinalDirection {
	North, East, South, West ;
	/** 
	 * Gives the direction that results from a 90 degree clockwise rotation
	 * applied to the current direction. 
	 * @return direction after 90 degree clockwise rotation
	 */
	public CardinalDirection rotateClockwise() {
		switch(this) {
		case North : 
			return CardinalDirection.East ;
		case East : 
			return CardinalDirection.South ;
		case South : 
			return CardinalDirection.West ;
		case West : 
			return CardinalDirection.North ;
		default:
			throw new RuntimeException("Inconsistent enum type") ;
		}
	}
	/** 
	 * Gives the opposite direction which is the same as applying a 180 degree
	 * rotation. 
	 * @return direction that is opposite to the current direction
	 */
	public CardinalDirection oppositeDirection() {
		switch(this) {
		case North : 
			return CardinalDirection.South ;
		case East : 
			return CardinalDirection.West ;
		case South : 
			return CardinalDirection.North ;
		case West : 
			return CardinalDirection.East ;
		default:
			throw new RuntimeException("Inconsistent enum type") ;
		}
	}
	/**
	 * Gives a random direction. Values are picked with equal probabilities.
	 * @return a random direction, distribution is uniform
	 */
	public CardinalDirection randomDirection() {
		int i = SingleRandom.getRandom().nextIntWithinInterval(0, 3) ;
		switch(i) {
		case 0 : 
			return CardinalDirection.North ;
		case 1 : 
			return CardinalDirection.East ;
		case 2 : 
			return CardinalDirection.South ;
		case 3 : 
			return CardinalDirection.West ;
		default:
			throw new RuntimeException("Random variable out of bounds: " + i) ;
		}
	}
	


	/**
	 * Gives the matching direction for (dx,dy) pair as used in the floorplan
	 * @param dx is the x direction of a (dx,dy) pair, {@code dx,dy in {-1,0,1}}
	 * @param dy is the y direction of a (dx,dy) pair, {@code dx,dy in {-1,0,1}}
	 * @return matching cardinal direction
	 */
	static public CardinalDirection getDirection(int dx, int dy) {
		/* Compare with Floorplan.java for consistency
		 Directions: right=east, down=south, left=west, up=north
		public static int[] DIRS_X = { 1, 0, -1, 0 };
		public static int[] DIRS_Y = { 0, 1, 0, -1 };
		 */
		//System.out.println("CardinalDirection.getDirection: Warning: check consistency with direction on screen") ;
		switch(dx) {
		case -1 : // must by (-1,0)
			return CardinalDirection.West ;
		case 0 : 
			if (dy == 1) // is (0,1)
				return CardinalDirection.South; // flipped South ;
			if (dy == -1) // is (0,-1)
				return CardinalDirection.North; // flipped North ;
			throw new IllegalArgumentException("Illegal input value for dx: " + dx) ;
		case 1 : // must be (1,0)
			return CardinalDirection.East ;
		default:
			throw new IllegalArgumentException("Illegal input value for dx: " + dx) ;
		}
	}
	/**
	 * Gives the (dx,dy) pair as in Floorplan.java for the current direction
	 * @return (dx,dy) pair, dx,dy in {-1,0,1}
	 */
	public int[] getDxDyDirection() {
		/* Compare with Floorplan.java for consistency
		 Directions: right=east, down=south, left=west, up=north
		public static int[] DIRS_X = { 1, 0, -1, 0 };
		public static int[] DIRS_Y = { 0, 1, 0, -1 };
	
		 */
		int[] result = new int[2] ;
		switch(this) {
		case North: // flipped North : 
			result[0] = 0 ;
			result[1] = -1 ;
			break ;
		case East : 
			result[0] = 1 ;
			result[1] = 0 ;
			break ;
		case South: // flipped South : 
			result[0] = 0 ;
			result[1] = 1 ;
			break ;
		case West : 
			result[0] = -1 ;
			result[1] = 0 ;
			break ;
		default:
			throw new RuntimeException("Inconsistent enum type") ;
		}
		return result ;
	}
	/**
	 * Maps the given angle to the closest (dx,dy) pair
	 * @return (dx,dy) pair, dx,dy in {-1,0,1}
	 */
    static public int[] getDxDyDirection(int angle) {
    	// radify angle, i.e. angle x pi / 180
    	// the apply sine/cosine to obtain direction
		/* Compare with Floorplan.java for consistency
		 Directions: right=east, down=south, left=west, up=north
		public static int[] DIRS_X = { 1, 0, -1, 0 };
		public static int[] DIRS_Y = { 0, 1, 0, -1 };
		Memo
		rad = X x pi / 180
		Angle	rad		x-cos	y-sin
		0		0		1		0 	east
		90		0.5pi	0		1	south
		180		pi		-1		0	west
		270		1.5pi	0		-1	north	
		 */
    	
    	double radifiedAngle = angle*Math.PI/180;
		int[] result = new int[2] ;
		result[0] = (int) Math.cos(radifiedAngle) ;
		result[1] = (int) Math.sin(radifiedAngle) ;
		return result ;
	}
	/**
	 * Gives the angle for the current direction
	 * @return angle in {@literal {0,90,180,270}}
	 */
	public int angle() {
		/* Compare with Floorplan.java for consistency
		 Directions: right=east, down=south, left=west, up=north
		public static int[] DIRS_X = { 1, 0, -1, 0 };
		public static int[] DIRS_Y = { 0, 1, 0, -1 };
		Memo
		rad = X x pi / 180
		Angle	rad		x-cos	y-sin
		0		0		1		0 	east
		90		0.5pi	0		1	south
		180		pi		-1		0	west
		270		1.5pi	0		-1	north	
		 */
		int result = 0 ;
		switch(this) {
		case North: // flipped North : 
			result = 270 ;
			break ;
		case East : 
			result = 0 ;
			break ;
		case South: // flipped South : 
			result = 90 ;
			break ;
		case West : 
			result = 180 ;
			break ;
		default:
			throw new RuntimeException("Inconsistent enum type") ;
		}
		return result ;
	}
	
	/**
	 * Maps the given angle to the closest direction
	 * @return matching cardinal direction
	 */
    static public CardinalDirection getDirection(int angle) {
    	double radifiedAngle = angle*Math.PI/180;
		return getDirection((int) Math.cos(radifiedAngle), (int) Math.sin(radifiedAngle));
    }
 
}
