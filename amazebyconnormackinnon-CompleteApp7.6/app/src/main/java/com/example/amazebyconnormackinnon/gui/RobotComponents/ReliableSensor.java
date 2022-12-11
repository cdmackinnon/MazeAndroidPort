package com.example.amazebyconnormackinnon.gui.RobotComponents;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Direction;

/**
 * CRC:
 *Responsibilities: Knows Wall's Distance, Knows facing direction, Knows energy it spends
 *Collaborators: 	Maze (for wall info), Robot (for direction and (x,y) purposes)
 *
 *
 *Reminder! up is south, right is east, down is north, left is west
 *Bottom left corner is 0,0
 */

/**   
* @author Connor MacKinnon
*/
public class ReliableSensor implements DistanceSensor {

	Maze maze;
	Direction dir;
	
	
	
	
	//needs to update to return sensor failure error, double check power failure
	@Override
	//Distance sensing in one direction or checking if exit is visible in one direction: 1 energy
	public int distanceToObstacle(int[] currentPosition, CardinalDirection currentDirection, float[] powersupply)
			throws Exception {
		int [] cords = {currentPosition[0], currentPosition[1]};
		int distance = 0;
		
		if (powersupply[0] < 1) {
			throw new Exception("PowerFailureException");
		}
		while ( maze.isValidPosition(cords[0],cords[1]) &&  !(maze.hasWall(cords[0],cords[1], currentDirection)) ){
			distance+=1;
			if ((cords[0] > maze.getWidth()-1 || cords[0] < 0 || cords[1] > maze.getHeight()-1 || cords[1] < 0) && cords[0]==maze.getExitPosition()[0] && cords[1]==maze.getExitPosition()[1]){
				return Integer.MAX_VALUE;
			}
			else if(currentDirection == CardinalDirection.North) {
				cords[1]--;
			}
			else if(currentDirection == CardinalDirection.South) {
				cords[1]++;
			}
			else if(currentDirection == CardinalDirection.East) {
				cords[0]++;
			}
			else if(currentDirection == CardinalDirection.West) {
				cords[0]--;
			}
		}
		if (!maze.isValidPosition(cords[0], cords[1])&& currentPosition[0]==maze.getExitPosition()[0] && currentPosition[1]==maze.getExitPosition()[1]) {
			return Integer.MAX_VALUE;
		}
		return distance;
		
		//throws exception if maze and sensor direction have not been set (does it??)
		//checks if facing exit(returns max int)
		//checks if facing wall (returns 0)
		//keeps increasing coordinate value (corresponding x or y) and checks with maze reference if there is a wall
		//returns total steps to find the wall
		//review configuration of sensor with direction looking
	}

	@Override
	public void setMaze(Maze maze) {
		// uses this reference to the maze to local store a pointer for later access
		this.maze = maze;
	}

	@Override
	public void setSensorDirection(Direction mountedDirection) {
		//stores the relative direction of the sensor to the robot in a local variable
		dir = mountedDirection;
	}

	@Override
	//Distance sensing in one direction or checking if exit is visible in one direction: 1 energy
	public float getEnergyConsumptionForSensing() {
		//functions as a constant and returns the float value of sensing (1 energy)
		return 1;
	}

	@Override
	public void startFailureAndRepairProcess(int meanTimeBetweenFailures, int meanTimeToRepair)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void stopFailureAndRepairProcess() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		//Does Nothing

	}

}
