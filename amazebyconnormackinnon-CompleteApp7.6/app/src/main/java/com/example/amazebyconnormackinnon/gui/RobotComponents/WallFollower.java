package com.example.amazebyconnormackinnon.gui.RobotComponents;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Direction;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Turn;

/**
 *CRC:
 *
 *Responsibilities: Can link to a reliable/unreliable robot, detects and follows walls, Knows how much energy it used, Knows Journey Length
 *Collaborators: 	Maze (for floorplan and wall access),  Reliable/Unreliable Robot (to carry out driving)
 *  
 * @author Connor MacKinnon
 */
public class WallFollower implements RobotDriver {

	Robot robot;
	Maze maze;
	
	@Override
	public void setRobot(Robot r) {
		robot = r;
	}

	@Override
	public void setMaze(Maze maze) {
		this.maze = maze;
	}

	@Override
	public boolean drive2Exit() throws Exception {
		Thread.sleep(4000); //for sensor initializing
		
		
		robot.setBatteryLevel(3500);
		
		//checking if robot is: in exit positon, battery > 0, and checking not stopped
		while(!robot.isAtExit() && robot.getBatteryLevel()>0 && !robot.hasStopped()) {
			drive1Step2Exit();
		}
		//robot failure conditions
		if (robot.getBatteryLevel()<=0 || robot.hasStopped()) {
			throw new Exception();
		}
		//turning the robot towards the exit
		while(!(robot.canSeeThroughTheExitIntoEternity(Direction.FORWARD))){
			robot.rotate(Turn.LEFT);
		}
		return true;
	}

	@Override
	public boolean drive1Step2Exit() throws Exception {
		//robot failure conditions
		if (robot.getBatteryLevel()<=0 || robot.hasStopped()) {
			throw new Exception();
		}
		//robot doesn't move because it is already at the exit
		if (robot.canSeeThroughTheExitIntoEternity(Direction.FORWARD)) {
			return false;
		}
		
		//unreliable robot
		if (robot instanceof UnreliableRobot){
		//case where there is an open left turn:
			
			//waiting when sensor is unavailable
			//could be better updated with an alternate sensor using function

			if(((UnreliableRobot) robot).distanceToObstacle(Direction.LEFT) == -1){
				Thread.sleep(2010);
			}
			else if( ((UnreliableRobot) robot).distanceToObstacle(Direction.LEFT) > 0){
 				robot.rotate(Turn.LEFT);
				robot.move(1);
				return true;
			}
		
		//case where there is no left
		//moves forward if open
		//turns right otherwise
			else {
				if(((UnreliableRobot) robot).distanceToObstacle(Direction.FORWARD) == -1){
					Thread.sleep(2010);
				}
				if( ((UnreliableRobot) robot).distanceToObstacle(Direction.FORWARD) > 0){
					robot.move(1);
					return true;
				}
				else {
					robot.rotate(Turn.RIGHT);
					return true;
				}
			}
		}
				
		return false;
	}
	

	@Override
	public float getEnergyConsumption() {
		return (3500 - robot.getBatteryLevel());
	}

	@Override
	public int getPathLength() {
		return robot.getOdometerReading();
	}

}
