package com.example.amazebyconnormackinnon.gui.RobotComponents;

import android.util.Log;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Direction;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Turn;

/**
 *CRC:
 *
 *Responsibilities: Can link to a robot, Knows tile's exit distance, Knows how much energy it used, Knows Journey Length
 *Collaborators: 	Maze (for floorplan and distance), Reliable Robot (to carry out driving)
 */

/**   
* @author Connor MacKinnon
*/
public class Wizard implements RobotDriver {

	Robot robot;
	Maze maze;
	
	@Override
	public void setRobot(Robot r) {
		//attaches the driver to the robot
		//stores a reference to the robot for later movement
		robot = r;
	}

	@Override
	public void setMaze(Maze maze) {
		//stores a reference to the maze for later information
		this.maze = maze;
	}

	@Override
	public boolean drive2Exit() throws Exception {
		//gets to the exit one step at a time
		//calls the drive1step2exit over and over until the robot is there (while loop)
		
		//check also in the while loop that the robot is facing eternity
		
		//steps towards the exit while the robot is not:
		//at the exit
		//facing the exit
		//out of battery
		//or crashed
		robot.setBatteryLevel(3500);

		//checking if in exit positon, checking robot cannot see through exit, checking battery > 0, checking not stopped
		while(!robot.isAtExit() && robot.getBatteryLevel()>0 && !robot.hasStopped()) {
			drive1Step2Exit();
		}
		while(!(robot.canSeeThroughTheExitIntoEternity(Direction.FORWARD))){
			robot.rotate(Turn.LEFT);
		}
		return true;
		
		//throw new Exception();
		//Requires both exit position and see through eternity
		//because exit could be at the end of a long hallway
		
	}

	@Override
	public boolean drive1Step2Exit() throws Exception {
		//checks all of the surrounding tiles for the shortest path to the exit
		//orients the robot towards that tile
		//moves towards it
		//(could be optimized to avoid checking the tile the robot came from)
		int[] position = robot.getCurrentPosition();
		int[] closer = maze.getNeighborCloserToExit(position[0], position[1]);
		//this is east case closer
		if (position != closer || robot.hasStopped()) {
			if ((closer[0] - position[0]) > 0) {
				robot.directionFacer(CardinalDirection.East);
			}//this is west case closer
			else if ((closer[0] - position[0]) < 0) {
				robot.directionFacer(CardinalDirection.West);
			}//this is south case closer
			else if((closer[1] - position[1]) > 0) {
				robot.directionFacer(CardinalDirection.South);
			}//this is north case closer
			else if ((closer[1] - position[1]) < 0) {
				robot.directionFacer(CardinalDirection.North);
			}
			robot.move(1);
			return true;
		}
		
		
		else {
		//returns false if robot didn't move (already at exit)
		//this could occur if the robot is stopped
		return false;
		}
	}
	
	

	@Override
	public float getEnergyConsumption() {
		//checks that the robot is at the exit (else error)
		//subtracts the robot starting energy with the current energy
		//returns that value
		try {
			drive2Exit();
		} catch (Exception e) {
			return -1;
			}
		return 3500 - robot.getBatteryLevel();
	}

	@Override
	public int getPathLength() {
		//returns the maze.distancetoexit on robot current position
		//subtracts this by 1 for the remaining path to the exit
		try {
			return maze.getDistanceToExit(robot.getCurrentPosition()[0], robot.getCurrentPosition()[1]) - 1;
			
			//if the robot position is out of bound Max Value is return max value
		} catch (Exception e) {
			return Integer.MAX_VALUE;
		}
	}

}
