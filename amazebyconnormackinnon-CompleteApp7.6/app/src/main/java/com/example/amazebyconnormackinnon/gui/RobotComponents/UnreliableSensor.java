package com.example.amazebyconnormackinnon.gui.RobotComponents;


import android.util.Log;

import com.example.amazebyconnormackinnon.generation.CardinalDirection;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.Activities.PlayAnimationActivity;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot.Direction;


/**
 * CRC:
 *
 *Responsibilities: Knows Wall's Distance, Knows facing direction, Knows energy it spends, Can Break, Can be repaired
 *Collaborators:  	Maze (for wall info), UnreliableRobot (for direction and repairing purposes)
 *
* @author Connor MacKinnon
*/
public class UnreliableSensor implements DistanceSensor {
	
	Maze maze;
	Direction dir;
	volatile boolean operational = true;
	Thread operationalThread;
	boolean flagged = false;

	@Override
	public int distanceToObstacle(int[] currentPosition, CardinalDirection currentDirection, float[] powersupply)
			throws Exception {
		
		if (!operational) {
			throw new Exception();
		}
		if (powersupply[0] < 1) {
			throw new Exception("PowerFailureException");
		}
		
		int [] cords = {currentPosition[0], currentPosition[1]};
		int distance = 0;
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
	}

	@Override
	public void setMaze(Maze maze) {
		this.maze = maze;
	}

	@Override
	public void setSensorDirection(Direction mountedDirection) {
		dir = mountedDirection;

	}

	@Override
	public float getEnergyConsumptionForSensing() {
		//functions as a constant and returns the float value of sensing (1 energy)
		return 1;
	}

	
	public void setOperational(boolean state) {
		this.operational = state;
	}
	
	public boolean getStatus() {
		return (operational);
	}
	
	
	
	@Override
	public void startFailureAndRepairProcess(int meanTimeBetweenFailures, int meanTimeToRepair)
			throws UnsupportedOperationException {
		
		this.operationalThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	while(!Thread.currentThread().isInterrupted() && !flagged){
		    	setOperational(true);
				PlayAnimationActivity.sensorToggler(dir,true);
				try { //Sensor is operational
					Thread.sleep(meanTimeBetweenFailures * 1000);
				}catch(InterruptedException e){}
				
				setOperational(false);
				PlayAnimationActivity.sensorToggler(dir,false);
				try { //Sensor is not operational
					Thread.sleep(meanTimeToRepair * 1000);
				}catch (InterruptedException e) {}
		    	}
				Thread.interrupted();
		    }
		});  
		operationalThread.start();
		
	}

	@Override
	public void stopFailureAndRepairProcess() throws UnsupportedOperationException {
		//Log.v("STOPPING","unsuccess");
		//Thread.currentThread().interrupt();
		//operationalThread.interrupt();
		flagged = true;
	}

}
