package com.example.amazebyconnormackinnon.gui.RobotComponents;



//import java.awt.event.KeyEvent;
import android.util.Log;

import java.util.Arrays;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.generation.Wallboard;
import com.example.amazebyconnormackinnon.gui.Activities.GeneratingActivity;
import com.example.amazebyconnormackinnon.gui.Activities.PlayAnimationActivity;
import com.example.amazebyconnormackinnon.gui.Constants.UserInput;
import com.example.amazebyconnormackinnon.gui.StatePlaying;

/**
 * CRC:
 *
 *Responsibilities: Can move and Turn, Uses sensors for info, Tracks battery/energy, Checks if at exit, Has working odometer
 *Collaborators: 	Control (to turn and move),  Reliable Sensor (to check walls)
 */

/**
* @author Connor MacKinnon
*/
public class ReliableRobot implements Robot {

	/**
	 *Links the robot with the Controller
	 *Done through the control class 
	 */
	

	StatePlaying playing;

	int[] coordinates;
	float battery;
	int odometer = 0;
	boolean hasStopped = false;
	
	@Override
	public void setController(StatePlaying playing) {
		this.playing = playing;
	}
	
	

	/**
	* Creates a new sensor object and adds it to the desired direction
	* Uses sensor.setSensorDirecion
	*/
	@Override
	public void addDistanceSensor(DistanceSensor sensor, Direction mountedDirection) {
		sensor.setMaze(playing.getMaze());
		sensor.setSensorDirection(mountedDirection);
	}
	
	/*
	* Returns the coordinates of the robot from the maze 
	* @exception if the robot is outside of the maze
	*/
	@Override
	public int[] getCurrentPosition() throws Exception {
		
		int[] coordinates = playing.getPosition();
		
		//checks coordinates inside maze boundaries
		if (coordinates[0] > playing.getMaze().getWidth() || (coordinates[0] < 0 || coordinates[1] >= playing.getMaze().getHeight() || (coordinates[1] < 0 ))){
			throw new Exception();
		}
		
		return coordinates;
	}

	/**
	* Returns the current direction that the robot is facing
	*/
	@Override
	public CardinalDirection getCurrentDirection() {
		return playing.getCurrentDirection();
	}
	
	/**
	 * @Returns the locally stored battery variable
	 */
	@Override
	public float getBatteryLevel() {
		return battery;
	}

	/**
	 * updates the locally stored battery variable
	 */
	@Override
	public void setBatteryLevel(float level) {
		battery = level;
	}

	/**
	 * Gives the energy consumption for a full 360 degree rotation. 
	 * Scaling by other degrees approximates the corresponding consumption.
	 */
	@Override
	public float getEnergyForFullRotation() {
		//(3 energy per 90 degree rotation * 4 rotations = 12 energy)
		return 12;
	}

	/**
	 * Gives the energy consumption for moving forward for a distance of 1 step
	 */
	@Override
	public float getEnergyForStepForward() {
		//returns the constant float for moving one step (6 energy)
		return 6;
	}

	/**
	 * Returns the odometer value
	 */
	@Override
	public int getOdometerReading() {
		return odometer;
	}

	/**
	 * Resets the odometer to 0
	 */
	@Override
	public void resetOdometer() {
		odometer = 0;
	}

	/**
	 * Spoofs control keyboard inputs to turn the robot
	 * calls itself again to do a 180
	 */
	@Override
	public void rotate(Turn turn) {
		if (!hasStopped() && !(battery==0)) {
			if (turn == Turn.LEFT) {
				playing.handleUserInput(UserInput.LEFT,1);
				//controller.handleKeyboardInput(UserInput.LEFT, KeyEvent.VK_H);
				battery -= getEnergyForFullRotation()/4;
			}
			else if (turn == Turn.RIGHT) {
				playing.handleUserInput(UserInput.RIGHT,1);
				//controller.handleKeyboardInput(UserInput.RIGHT, KeyEvent.VK_L);
				battery -= getEnergyForFullRotation()/4;
			}
			else if(turn == Turn.AROUND){
				playing.handleUserInput(UserInput.RIGHT,1);
				//controller.handleKeyboardInput(UserInput.RIGHT, KeyEvent.VK_L);
				battery -= getEnergyForFullRotation()/4;
				this.rotate(Turn.RIGHT);
				//calls itself again to complete the 180
			}
		}
	}

	/**
	 * Moves the robot forward the distance passed in.
	 * Subtracts the cost from the battery.
	 * Checks there are no collisions with walls.
	 * Updates the odometer.
	 */
	@Override
	public void move(int distance) throws InterruptedException {
		if (!hasStopped() && battery>0 && distance > 0) {

			Thread.sleep(100- PlayAnimationActivity.getSpeed());

			while(PlayAnimationActivity.isPaused()){
				Thread.sleep(100);
			}

			//This huge statement is checking if there is a wall at the robot's current position and direction
			if(playing.getMaze().getFloorplan().hasWall(playing.getPosition()[0], playing.getPosition()[1], getCurrentDirection())) {
				hasStopped = true;
				return;
			}
			playing.handleUserInput(UserInput.UP,1);
			//controller.handleKeyboardInput(UserInput.UP, KeyEvent.VK_K);
			battery -= getEnergyForStepForward();
			odometer += 1;
			move(distance-1);
		}
		//checks hasStopped is false
		//subtracts battery, checks if >0 else trigger has Stopped
		//checks for wall, if no collision:
		//update odometer
		//else trigger has_stopped and position variable is unchanged

	}

	/**Does same operations as move but jumps over wall and
	 * is more expensive for battery.
	 */
	@Override
	public void jump() {
		if (!hasStopped() && battery>0) {
			
			//This is checking if the wall being jumped is a border wall and stopping if it is
				Wallboard borderWallboard = new Wallboard(playing.getPosition()[0], playing.getPosition()[1], getCurrentDirection());
				if(playing.getMaze().getFloorplan().isPartOfBorder(borderWallboard)) {
					hasStopped = true;
					return;
				}
			}
			playing.handleUserInput(UserInput.JUMP,1);
			//controller.handleKeyboardInput(UserInput.JUMP, KeyEvent.VK_W);
			battery -= 40;	//jump energy constant
			odometer += 1;
		//checks hasStopped is false
		//subtracts battery, checks if >0 else trigger has Stopped //order of operations coded to jump first now
		//checks if not at border wall
		//update odometer
		//else trigger has_stopped and position variable is unchanged (in the else case of jumping a border wall)

	}
	
	/*
	 * Returns true or false if the robot is at the exit
	 */
	@Override
	public boolean isAtExit() {
		//returns true if maze.getExit() matches with the current position
		try {
			if (Arrays.equals(getCurrentPosition(),playing.getMaze().getExitPosition())){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns whether the robot is inside a room
	 */
	@Override
	public boolean isInsideRoom() {
		//references the current position with maze.isInRoom, returns same value
		int[] coordinates = playing.getPosition();
		return playing.getMaze().isInRoom(coordinates[0], coordinates[1]);
	}

	/**
	 * Returns whether the robot has stopped
	 */
	@Override
	public boolean hasStopped() {
		// returns state of hasStopped boolean
		return hasStopped;
	}

	@Override
	public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
		//subtract battery amount by sensor.getEnergyConsumptionForSensing() 
		//check has_stopped
		//adds a new distance sensor in the desired direction and uses it to return the distance
		
		if (!hasStopped() && battery>0) {
			ReliableSensor sensor = new ReliableSensor();
			addDistanceSensor(sensor, direction);
			float[] battery = {getBatteryLevel()};
			this.battery -= 1; //cost of sensing
			
			try {
				return(sensor.distanceToObstacle(getCurrentPosition(), getCurrentDirection(), battery));
				//if position is out of bounds return Max Integer
			} catch (Exception e) {
				return Integer.MAX_VALUE;
			}
		}
		if (battery <= 0) {
			hasStopped = true;
		}
		return -1;
	}


	//subtract battery amount by sensor.getEnergyConsumptionForSensing() 
	//check has_stopped
	//adds a new distance sensor in the desired direction and uses it to return the distance
	//if that distance is MAX_INTEGER return true, else false
	//return false;
	@Override
	public boolean canSeeThroughTheExitIntoEternity(Direction direction) throws UnsupportedOperationException {
		if (!hasStopped() && battery>0) {
			ReliableSensor sensor = new ReliableSensor();
			sensor.setMaze(playing.getMaze());
			addDistanceSensor(sensor, direction);
			float[] battery = {getBatteryLevel()};
			this.battery -= 1; //cost of sensing
			try {
				if(sensor.distanceToObstacle(getCurrentPosition(), getCurrentDirection(), battery)== Integer.MAX_VALUE) {
					return true;
				}
				//if position is invalid returns false
			} catch (Exception e) {
				return false;
			}
		}
		hasStopped = false;
		return false;
	}
	
	@Override
	public void directionFacer(CardinalDirection desiredCardinalDirection){
		if (getCurrentDirection() == CardinalDirection.South){
			if(desiredCardinalDirection == CardinalDirection.South) {
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.East) {
				rotate(Turn.RIGHT);
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.West) {
				rotate(Turn.LEFT);
				return;
			}
			else {
				rotate(Turn.AROUND);
				return;
			}
		}
		else if (getCurrentDirection() == CardinalDirection.East){
			if(desiredCardinalDirection == CardinalDirection.East) {
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.North) {
				rotate(Turn.RIGHT);
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.South) {
				rotate(Turn.LEFT);
				return;
			}
			else {
				rotate(Turn.AROUND);
				return;
			}
		}
		
		else if (getCurrentDirection() == CardinalDirection.North){
			if(desiredCardinalDirection == CardinalDirection.North) {
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.East) {
				rotate(Turn.LEFT);
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.West) {
				rotate(Turn.RIGHT);
				return;
			}
			else {
				rotate(Turn.AROUND);
				return;
			}
		}
		
		else if (getCurrentDirection() == CardinalDirection.West){
			if(desiredCardinalDirection == CardinalDirection.West) {
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.North) {
				rotate(Turn.LEFT);
				return;
			}
			else if (desiredCardinalDirection == CardinalDirection.South) {
				rotate(Turn.RIGHT);
				return;
			}
			else {
				rotate(Turn.AROUND);
				return;
			}
		}
	}
	

	@Override
	public void startFailureAndRepairProcess(Direction direction, int meanTimeBetweenFailures, int meanTimeToRepair)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void stopFailureAndRepairProcess(Direction direction) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
