package com.example.amazebyconnormackinnon.generation;

import java.util.Random;
import java.util.logging.Logger;

/**
 * This class implements the generation of pseudo random numbers with a single instance of a random number generator.
 * It is possible to set the seed of the generator by calling setSeed before the internal random number generator is initialized. 
 * This happens when getRandom is called for the first time. Any subsequent call to setSeed resets the seed of the existing generator.
 * Design implements Singleton pattern.
 * 
 * @author Kemper
 *
 */
public final class SingleRandom {
	/**
	 * The logger is used to track execution and report issues.
	 */
	private static final Logger LOGGER = Logger.getLogger(SingleRandom.class.getName());

	/**  internal internal random number generator. This should NOT be made accessible. */
	private transient final Random generator;
	/** the single instance of this class. */
	private static SingleRandom instance = null ;
	/** optional: a seed value can be set to be able to reproduce a sequence of random numbers. */
	private static int theSeed;
	/** optional: flag to show if seed value can be used or not. */
	private static boolean validSeed = false;
	/** lock to make the private constructor thread safe, one could have used the class object as well */
	private static final Object LOCK = new Object() ;
	/**
	 * The constructor for a class that allows for a single instance must be private.
	 */
	private SingleRandom() {
		generator = validSeed ? new Random(theSeed) : new Random();
	}

	/**
	 * Delivers an instance of a random number generator.
	 * @return instance of random number generator
	 */
	public static SingleRandom getRandom() {
		synchronized(LOCK)
		{
			// check the instance being null needs synchronization to be thread safe
			if (null == instance)
			{
				// create the one and only instance of the random number generator on demand
				instance = new SingleRandom();
			}
		}
		return instance;
	}
	
	/**
	 * Delivers a random number.
	 * @return random integer value
	 */
	public int nextInt() {
		return generator.nextInt();
	}
	/**
	 * Generate an integer random number in interval [lowerBound,upperBound] 
	 * @param lowerBound for the interval of values (inclusive) 
	 * @param upperBound for the interval of values (inclusive, WARNING: unusual)
	 * @return random number within given range
	 */
	public int nextIntWithinInterval(final int lowerBound, final int upperBound) {
		// nextInt(x) delivers uniformly distributed value in [0,x-1]
		// say d = upperBound-lowerBound, then we want a result lowerBound + uniform(0,d) and uniform(0,d) is delivered by nextInt(d+1)
		assert(lowerBound <= upperBound) : "parameter error, lowerbound " + lowerBound + "> upper bound" + upperBound ;
		return lowerBound + generator.nextInt(upperBound - lowerBound + 1) ;
	}

	/**
	 * Prepares the generator to start with a particular seed value.
	 * @param seed is the seed value for the random number generator
	 */
	public static void setSeed(final int seed) {
		if (null != instance) {
			LOGGER.config("SingleRandom already instantiated, setting new seed value " + seed + "will start different RN stream");
			instance.generator.setSeed(seed) ;
		}
		theSeed = seed;
		validSeed = true;
	}
}
