package com.example.amazebyconnormackinnon.gui.RobotComponents;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Direction;

/**
 * A DistanceSensor provides information how for it is
 * to a wall for a given position and in a specific direction.
 * 
 * The sensor is assumed to be mounted at a particular angle
 * relative to the forward direction of the robot.
 * This way one can have a left sensor, a right sensor, 
 * a forward, or a backward sensor.
 * 
 * So if asked about the distance to an obstacle, one needs
 * to provide the current position in the maze and the 
 * forward direction such that one can calculate the distance
 * to an obstacle a sensor measures in the relative direction
 * that it is mounted on the robot. 
 * 
 * The sensor consumes energy for its sensing operations, which
 * is why sensing expects a power supply to deduct the consumed
 * energy from.
 * 
 * A sensor can experience temporary failures of its operations.
 * So there are alternating time periods when it is up and running 
 * and down and broken. 
 * The down time of a sensor is characterized by its the mean time
 * it takes to repair itself. This could be the time it takes
 * for a reboot of the subsystem if it is a software failure.
 * The up time is described by the mean time between failures 
 * which is the time when the system comes up again and the next
 * failure that brings it down. 
 *  
 * The power consumption for the repair operations is ignored.
 * 
 * It is possible for implementing classes to assume that failures
 * never happen.
 * 
 * @author Peter Kemper
 *
 */
public interface DistanceSensor {
	/**
	 * Tells the distance to an obstacle (a wallboard) that the sensor
	 * measures. The sensor is assumed to be mounted in a particular
	 * direction relative to the forward direction of the robot.
	 * Distance is measured in the number of cells towards that obstacle, 
	 * e.g. 0 if the current cell has a wallboard in this direction, 
	 * 1 if it is one step in this direction before directly facing a wallboard,
	 * Integer.MaxValue if one looks through the exit into eternity.
	 * 
	 * This method requires that the sensor has been given a reference
	 * to the current maze and a mountedDirection by calling 
	 * the corresponding set methods with a parameterized constructor.
	 * 
	 * @param currentPosition is the current location as (x,y) coordinates
	 * @param currentDirection specifies the direction of the robot
	 * @param powersupply is an array of length 1, whose content is modified 
	 * to account for the power consumption for sensing
	 * @return number of steps towards obstacle if obstacle is visible 
	 * in a straight line of sight, Integer.MAX_VALUE otherwise.
	 * @throws Exception with message 
	 * SensorFailure if the sensor is currently not operational
	 * PowerFailure if the power supply is insufficient for the operation
	 * @throws IllegalArgumentException if any parameter is null
	 * or if currentPosition is outside of legal range
	 * ({@code currentPosition[0] < 0 || currentPosition[0] >= width})
	 * ({@code currentPosition[1] < 0 || currentPosition[1] >= height}) 
	 * @throws IndexOutOfBoundsException if the powersupply is out of range
	 * ({@code powersupply < 0}) 
	 */
	int distanceToObstacle(int[] currentPosition, 
			CardinalDirection currentDirection, 
			float[] powersupply) throws Exception;
	
	/**
	 * Provides the maze information that is necessary to make
	 * a DistanceSensor able to calculate distances.
	 * @param maze the maze for this game
	 * @throws IllegalArgumentException if parameter is null
	 * or if it does not contain a floor plan
	 */
	void setMaze(Maze maze);
	
	/**
	 * Provides the angle, the relative direction at which this 
	 * sensor is mounted on the robot.
	 * If the direction is left, then the sensor is pointing
	 * towards the left hand side of the robot at a 90 degree
	 * angle from the forward direction. 
	 * @param mountedDirection is the sensor's relative direction
	 * @throws IllegalArgumentException if parameter is null
	 */
	void setSensorDirection(Direction mountedDirection);
	
	/**
	 * Returns the amount of energy this sensor uses for 
	 * calculating the distance to an obstacle exactly once.
	 * This amount is a fixed constant for a sensor.
	 * @return the amount of energy used for using the sensor once
	 */
	float getEnergyConsumptionForSensing();
	
	////// The following methods will be implemented in 
	////// Project assignment 4 for a class UnstableSensor.
	////// For P3 and the StableSensor class, 
	////// it is sufficient to throw the UnsupportedOperationException.
	
	/**
	 * Method starts a concurrent, independent failure and repair
	 * process that makes the sensor fail and repair itself.
	 * This creates alternating time periods of up time and down time.
	 * Up time: The duration of a time period when the sensor is in 
	 * operational is characterized by a distribution
	 * whose mean value is given by parameter meanTimeBetweenFailures.
	 * Down time: The duration of a time period when the sensor is in repair
	 * and not operational is characterized by a distribution
	 * whose mean value is given by parameter meanTimeToRepair.
	 * 
	 * This an optional operation. If not implemented, the method
	 * throws an UnsupportedOperationException.
	 * 
	 * @param meanTimeBetweenFailures is the mean time in seconds, must be greater than zero
	 * @param meanTimeToRepair is the mean time in seconds, must be greater than zero
	 * @throws UnsupportedOperationException if method not supported
	 */
	void startFailureAndRepairProcess(int meanTimeBetweenFailures, int meanTimeToRepair) throws UnsupportedOperationException;
	/**
	 * This method stops a failure and repair process and
	 * leaves the sensor in an operational state.
	 * 
	 * It is complementary to starting a 
	 * failure and repair process. 
	 * 
	 * Intended use: If called after starting a process, this method
	 * will stop the process as soon as the sensor is operational.
	 * 
	 * If called with no running failure and repair process, 
	 * the method will return an UnsupportedOperationException.
	 * 
	 * This an optional operation. If not implemented, the method
	 * throws an UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException if method not supported
	 */
	void stopFailureAndRepairProcess() throws UnsupportedOperationException;
	
	

}
