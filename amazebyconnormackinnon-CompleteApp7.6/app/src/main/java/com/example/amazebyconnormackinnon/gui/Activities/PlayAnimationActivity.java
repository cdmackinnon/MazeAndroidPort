package com.example.amazebyconnormackinnon.gui.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amazebyconnormackinnon.R;
import com.example.amazebyconnormackinnon.generation.DefaultOrder;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.generation.MazeFactory;
import com.example.amazebyconnormackinnon.generation.Order;
import com.example.amazebyconnormackinnon.gui.Constants;
import com.example.amazebyconnormackinnon.gui.GameInterface.MazePanel;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Robot;
import com.example.amazebyconnormackinnon.gui.RobotComponents.RobotDriver;
import com.example.amazebyconnormackinnon.gui.RobotComponents.UnreliableRobot;
import com.example.amazebyconnormackinnon.gui.RobotComponents.WallFollower;
import com.example.amazebyconnormackinnon.gui.RobotComponents.Wizard;
import com.example.amazebyconnormackinnon.gui.StatePlaying;

import java.util.Arrays;
import java.util.Random;

public class PlayAnimationActivity extends AppCompatActivity {

    private static boolean paused = false;
    boolean playing = true;
    StatePlaying play;
    UnreliableRobot robot;
    private static int speed;
    static Button forward;
    static Button back;
    static Button right;
    static Button left;
    static TextView energy;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_animation);

        /* BUTTONS FOR MENU TESTING PURPOSES
        Button win = findViewById(R.id.Win);
        win.setOnClickListener( v -> toWin() );
        Button lose = findViewById(R.id.lose);
        lose.setOnClickListener( v -> toLose() );
         */

        //Intializes the energy cost
        energy = findViewById(R.id.PathLen);
        energy.setText("Energy: " + 3500  );

        ImageButton title = findViewById(R.id.back);
        title.setOnClickListener(v -> toTitle());


        forward = findViewById(R.id.ForwardSensor);
        back = findViewById(R.id.BackSensor);
        right = findViewById(R.id.RightSensor);
        left = findViewById(R.id.LeftSensor);


        //Zooms the robot in and out
        ImageButton plus = findViewById(R.id.Zoom_In);
        plus.setOnClickListener(v -> zoomIn());
        ImageButton minus = findViewById(R.id.Zoom_Out);
        minus.setOnClickListener(v -> zoomOut());

        //Pauses or Unpauses the robot
        ImageButton pause = findViewById(R.id.pause);
        pause.setOnClickListener(v -> pause());

        //Maze Controls
        Button fullMaze = findViewById(R.id.fullMazeButton);
        fullMaze.setOnClickListener(v-> showFullMaze());
        Button solution = findViewById(R.id.solutionButton);
        solution.setOnClickListener(v-> showSolution());
        Button visibleWalls = findViewById(R.id.WallButton);
        visibleWalls.setOnClickListener(v-> showWalls());

        //updates when the speed is changed
        SeekBar dif = findViewById(R.id.speed);
        dif.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
           }
           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {
           }
           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {
               speed = seekBar.getProgress();
               Log.v("Speed Bar", "Animation Speed Updated to: " + speed);
                }
            });

        //Updates energy counter
        handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                energy.setText("Energy: " + (int)robot.getBatteryLevel());
                handler.postDelayed(this, 100);
            }
        }, 100);

        //Initializes the game view
        MazePanel panel = findViewById(R.id.MazePanel);
        play = new StatePlaying();
        play.setMaze(GeneratingActivity.maze);
        //Initializes Robot Sensors
        //Sensor string corresponds to forward left right back,
        String sensorString = "1111";
        Bundle extras = getIntent().getExtras();
        switch (extras.getInt("RoboType")){
            case 0: sensorString = "1111";
                    break;
            case 1: sensorString = "1001";
                    break;
            case 2: sensorString = "0110";
                    break;
            case 3: sensorString = "0000";
                    break;
        }

        play.setSensorString(sensorString);
        robot = new UnreliableRobot(play);
        RobotDriver driver;
        if (extras.getInt("DriverType") == 1){
            driver = new Wizard();
        }
        else{
            assert(extras.getInt("DriverType") == 2);
            driver = new WallFollower();
        }
        driver.setMaze(play.getMaze());
        driver.setRobot(robot);
        robot.setController(play);
        play.start(panel);
        showWalls();
        showFullMaze();
        new Thread(new Runnable() {
            public void run() {
        try {driver.drive2Exit(); animationOver();} catch(Exception e) {e.printStackTrace();  animationOver();}}}).start();

        panel.commit();

    }

    public static boolean isPaused(){
        return paused;
    }

    public static int getSpeed(){
        return speed;
    }


    private void animationOver(){
        if (Arrays.equals(play.getPosition(), play.getMaze().getExitPosition())){
            runOnUiThread(() -> toWin());
        }
        else{
            runOnUiThread(() -> toLose());
        }
        Thread.currentThread().interrupt();
    }

    /**
     * Shows the user all the walls visible to them
     */
    public void showWalls(){
        play.handleUserInput(Constants.UserInput.TOGGLELOCALMAP,1);
        //runOnUiThread(() -> Toast.makeText(PlayAnimationActivity.this, "Showing visible walls", Toast.LENGTH_SHORT).show());
        Log.v("ShowWalls", "Showing visible walls");
    }
    /**
     * Shows the user the solution path to the maze
     */
    public void showSolution(){
        play.handleUserInput(Constants.UserInput.TOGGLESOLUTION,1);
        //runOnUiThread(() -> Toast.makeText(PlayAnimationActivity.this, "Showing Solution", Toast.LENGTH_SHORT).show());
        Log.v("showSolution", "Showing Solution");
    }

    /**
     * Shows the user the entire maze
     */
    public void showFullMaze(){
        play.handleUserInput(Constants.UserInput.TOGGLEFULLMAP, 1);
        //runOnUiThread(() -> Toast.makeText(PlayAnimationActivity.this, "Showing Full Maze", Toast.LENGTH_SHORT).show());
        Log.v("showFullMaze", "Showing Full Maze");
    }



    /**
     * Pauses or unpauses the robot
     */
    public void pause(){
        paused = !paused;
        runOnUiThread(() -> Toast.makeText(PlayAnimationActivity.this, "Toggled Robot", Toast.LENGTH_SHORT).show());
        Log.v("pause", "Toggled Robot");
    }

    /**
     * Zooms the map in
     */
    public void zoomIn(){
        play.handleUserInput(Constants.UserInput.ZOOMIN, 1);
        //runOnUiThread(() -> Toast.makeText(PlayAnimationActivity.this, "Zoomed in", Toast.LENGTH_SHORT).show());
        Log.v("zoomIn", "Zoomed in");
    }
    /**
     * Zooms the map out
     */
    public void zoomOut(){
        play.handleUserInput(Constants.UserInput.ZOOMOUT, 1);
        //runOnUiThread(() -> Toast.makeText(PlayAnimationActivity.this, "Zoomed out", Toast.LENGTH_SHORT).show());
        Log.v("zoomOut", "Zoomed out");
    }

    public void toTitle(){
        Intent toTitle = new Intent(this, AMazeActivity.class);
        playing = false;
        robot.disableSensors();
        play.switchToTitle();
        handler.removeCallbacksAndMessages(null);
        startActivity(toTitle);
        Log.v("Title", "Returning to the title screen");
    }

    public void toWin(){
        Intent win = new Intent(this, WinningActivity.class);
        win.putExtra("steps", robot.getOdometerReading());
        win.putExtra("Optimalpath", play.getMaze().getDistanceToExit(play.getMaze().getStartingPosition()[0],play.getMaze().getStartingPosition()[1]));
        win.putExtra("energy", robot.getBatteryLevel());
        playing = false;
        robot.disableSensors();
        handler.removeCallbacksAndMessages(null);
        startActivity(win);
        Log.v("Won", "The game has been won");
    }

    public void toLose(){
        Intent lose = new Intent(this, LosingActivity.class);
        lose.putExtra("steps", robot.getOdometerReading());
        lose.putExtra("Optimalpath", play.getMaze().getDistanceToExit(play.getMaze().getStartingPosition()[0],play.getMaze().getStartingPosition()[1]));
        lose.putExtra("energy", robot.getBatteryLevel());
        playing = false;
        robot.disableSensors();
        handler.removeCallbacksAndMessages(null);
        startActivity(lose);
        Log.v("Lost", "The game has been lost");
    }

    public static void sensorToggler(Robot.Direction dir, Boolean state) {
        if (state) {
            switch (dir) {
                case FORWARD:
                    forward.setBackgroundColor(0xFF17CD1E);
                    break;
                case BACKWARD:
                    back.setBackgroundColor(0xFF17CD1E);
                    break;
                case LEFT:
                    left.setBackgroundColor(0xFF17CD1E);
                    break;
                case RIGHT:
                    right.setBackgroundColor(0xFF17CD1E);
                    break;
            }
        }
        else{
            switch (dir) {
                case FORWARD:
                    forward.setBackgroundColor(0xFFFF0000);
                    break;
                case BACKWARD:
                    back.setBackgroundColor(0xFFFF0000);
                    break;
                case LEFT:
                    left.setBackgroundColor(0xFFFF0000);
                    break;
                case RIGHT:
                    right.setBackgroundColor(0xFFFF0000);
                    break;
            }
        }
    }

    /*  FOR TESTING SENSORS ON ROBOT
    public void sensorThread(Button sensor ){
        new Thread(new Runnable() {
            public void run() {
                try {
                    while(playing){
                        Thread.sleep(4000);
                        sensor.setBackgroundColor(0xFFFF0000);
                        Thread.sleep(2000);
                        sensor.setBackgroundColor(0xFF17CD1E);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().interrupt();
            }
        }).start();
    }
     */



}