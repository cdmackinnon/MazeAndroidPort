package com.example.amazebyconnormackinnon.gui.RobotComponents;

import android.util.Log;

import java.lang.ref.Reference;


import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Direction;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Turn;
import com.example.amazebyconnormackinnon.gui.StatePlaying;

/**
 * 
 * CRC:
 * 
 * Responsibilities: 
 * Inherits all of the reliable robot methods, 
 * Has a selection of sensors that are both reliable and unreliable,
 * Functionality depends on sensor uptime, 
 * Sensor Repairing Functionality
 * 
 * Classes: ReliableRobot, ReliableSensor, UnreliableSensor
 * 
 *
 * @author Connor MacKinnon
 */
public class UnreliableRobot extends ReliableRobot {
	
	
	DistanceSensor forward;
	DistanceSensor backward;
	DistanceSensor left;
	DistanceSensor right;
	
	
	public UnreliableRobot(StatePlaying playing) {
		//this.setController(controller);
		//FORWARD SENSOR
		if (playing.getSensorString().substring(0,1).equals("1")) {
			forward = new ReliableSensor();
		}
		else {
			forward = new UnreliableSensor();
			forward.setSensorDirection(Direction.FORWARD);
			forward.startFailureAndRepairProcess(4, 2);
		}
		
		
		//LEFT SENSOR
		if (playing.getSensorString().substring(1,2).equals("1")) {
			left = new ReliableSensor();
		}
		else {
			//waits 1.3 seconds between initializing each unreliable sensor
			//This allows for one sensor to always be active
			try {Thread.sleep(1300);}catch(InterruptedException e){}
			left = new UnreliableSensor();
			left.setSensorDirection(Direction.LEFT);
			left.startFailureAndRepairProcess(4, 2);
		}
		
		
		//RIGHT SENSOR
		if (playing.getSensorString().substring(2,3).equals("1")) {
			right = new ReliableSensor();
		}
		else {
			try {Thread.sleep(1300);}catch(InterruptedException e){}
			right = new UnreliableSensor();
			right.setSensorDirection(Direction.RIGHT);
			right.startFailureAndRepairProcess(4, 2);
		}
		
		//BACKWARD SENSOR
		if (playing.getSensorString().substring(3).equals("1")) {
			backward = new ReliableSensor();
		}
		else {
			try {Thread.sleep(1300);}catch(InterruptedException e){}
			backward = new UnreliableSensor();
			backward.setSensorDirection(Direction.BACKWARD);
			backward.startFailureAndRepairProcess(4, 2);
		}
		
		forward.setMaze(playing.getMaze());
		left.setMaze(playing.getMaze());
		right.setMaze(playing.getMaze());
		backward.setMaze(playing.getMaze());
	}
	
	/**
	* Creates a new sensor object and adds it to the desired direction
	* Uses sensor.setSensorDirecion
	*/
	@Override
	public void addDistanceSensor(DistanceSensor sensor, Direction mountedDirection) {
		if (mountedDirection == Direction.FORWARD) {
			forward = sensor;
			sensor.setSensorDirection(Direction.FORWARD);
			forward.setMaze(playing.getMaze());
			forward.setSensorDirection(mountedDirection);
		}
		if (mountedDirection == Direction.LEFT) {
			left = sensor;
			sensor.setSensorDirection(Direction.LEFT);
			left.setMaze(playing.getMaze());
			left.setSensorDirection(mountedDirection);
		}
		if (mountedDirection == Direction.RIGHT) {
			right = sensor;
			sensor.setSensorDirection(Direction.RIGHT);
			right.setMaze(playing.getMaze());
			right.setSensorDirection(mountedDirection);
		}
		if (mountedDirection == Direction.BACKWARD) {
			backward = sensor;
			sensor.setSensorDirection(Direction.BACKWARD);
			backward.setMaze(playing.getMaze());
			backward.setSensorDirection(mountedDirection);
		}
	}
	
	
	@Override
	public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
		//subtract battery amount by sensor.getEnergyConsumptionForSensing() 
		//check has_stopped
		//checks the desired sensor direction with the corresponding sensor reference 
		
		if (!hasStopped() && battery>0) {
			
			float[] battery = {getBatteryLevel()};
			this.battery -= 1; //cost of sensing
			
			DistanceSensor reference = null;
			//Creating a reference 
			if (direction == Direction.FORWARD) {
				reference = forward;			//TODO double check that this is only a pointer
			}
			else if (direction == Direction.LEFT) {
				reference = left;
				}
			else if (direction == Direction.RIGHT) {
				reference = right;
				}
			else if (direction == Direction.BACKWARD) {
				reference = backward;
				}
			
			try {
				return(reference.distanceToObstacle(getCurrentPosition(), relativeToCardinalDirection(direction), battery));
				//if sensor is not operating (or out of bounds) return -1
			} catch (Exception e) {
				return -1;
			}
		}
		if (battery <= 0) {
			hasStopped = true;
		}
		return -1;
	}
	
	public CardinalDirection relativeToCardinalDirection(Direction dir) {
		if (dir == Direction.LEFT){
			if (getCurrentDirection() == CardinalDirection.North) {
				return CardinalDirection.East;
			}
			else if (getCurrentDirection() == CardinalDirection.East) {
				return CardinalDirection.South;
			}
			else if (getCurrentDirection() == CardinalDirection.South) {
				return CardinalDirection.West;
			}
			else if (getCurrentDirection() == CardinalDirection.West) {
				return CardinalDirection.North;
			}
		}
		else if (dir == Direction.RIGHT){
			if (getCurrentDirection() == CardinalDirection.North) {
				return CardinalDirection.West;
			}
			else if (getCurrentDirection() == CardinalDirection.West) {
				return CardinalDirection.South;
			}
			else if (getCurrentDirection() == CardinalDirection.South) {
				return CardinalDirection.East;
			}
			else if (getCurrentDirection() == CardinalDirection.East) {
				return CardinalDirection.North;
			}
		}
		//no backwards functionality
		return getCurrentDirection();
		
	}
	
	
		//subtract battery amount by sensor.getEnergyConsumptionForSensing() 
		//check has_stopped
		//checks the distance sensor in the desired direction
		//if that distance is MAX_INTEGER return true, else false
		@Override
		public boolean canSeeThroughTheExitIntoEternity(Direction direction) throws UnsupportedOperationException {
			if (!hasStopped() && battery>0) {
				
				DistanceSensor reference = null;
				//Creating a reference 
				if (direction == Direction.FORWARD) {
					reference = forward;			//TODO double check that this is only a pointer
				}
				else if (direction == Direction.LEFT) {
					reference = left;
					}
				else if (direction == Direction.RIGHT) {
					reference = right;
					}
				else if (direction == Direction.BACKWARD) {
					reference = backward;
					}
				
				//if the sensor is unreliable and not operating the function waits for it to be active
				if (reference instanceof UnreliableSensor) {
					if (!((UnreliableSensor) reference).getStatus()){
						try {Thread.sleep(2050);
						}catch (InterruptedException e) {e.printStackTrace();}
					}
				}
				
				float[] battery = {getBatteryLevel()};
				this.battery -= 1; //cost of sensing
				try {
					if(reference.distanceToObstacle(getCurrentPosition(), getCurrentDirection(), battery)== Integer.MAX_VALUE) {
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
	

		public void disableSensors(){
			forward.stopFailureAndRepairProcess();
			backward.stopFailureAndRepairProcess();
			left.stopFailureAndRepairProcess();
			right.stopFailureAndRepairProcess();
		}
		
		
		
		
	
}
