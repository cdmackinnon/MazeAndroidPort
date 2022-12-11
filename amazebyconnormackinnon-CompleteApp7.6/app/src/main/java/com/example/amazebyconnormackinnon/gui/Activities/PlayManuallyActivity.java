package com.example.amazebyconnormackinnon.gui.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.amazebyconnormackinnon.R;
import com.example.amazebyconnormackinnon.generation.DefaultOrder;
import com.example.amazebyconnormackinnon.generation.MazeFactory;
import com.example.amazebyconnormackinnon.generation.Order;
import com.example.amazebyconnormackinnon.gui.CompassRose;
import com.example.amazebyconnormackinnon.gui.Constants;
import com.example.amazebyconnormackinnon.gui.GameInterface.MazePanel;

import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.gui.StatePlaying;

import java.util.Arrays;
import java.util.Random;

public class PlayManuallyActivity extends AppCompatActivity {

    StatePlaying playing;
    int steps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_manually);

        //back button to title
        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener( v -> toTitle());

        //this should be jump but file directory has issues
        Button win = findViewById(R.id.Jump);
        win.setOnClickListener( v -> jump());

        //Zoom in and zoom out
        ImageButton plus = findViewById(R.id.Zoom_In);
        plus.setOnClickListener(v -> zoomIn());
        ImageButton minus = findViewById(R.id.Zoom_Out);
        minus.setOnClickListener(v -> zoomOut());

        //Maze Controls
        Button fullMaze = findViewById(R.id.fullMazeButton);
        fullMaze.setOnClickListener(v-> showFullMaze());
        Button solution = findViewById(R.id.solutionButton);
        solution.setOnClickListener(v-> showSolution());
        Button visibleWalls = findViewById(R.id.WallButton);
        visibleWalls.setOnClickListener(v-> showWalls());


        ImageButton forward = findViewById(R.id.upArrow);
        forward.setOnClickListener(v -> forward());
        ImageButton down = findViewById(R.id.downArrow);
        down.setOnClickListener(v -> backward());
        ImageButton right = findViewById(R.id.rightArrow);
        right.setOnClickListener(v -> right());
        ImageButton left = findViewById(R.id.leftArrow);
        left.setOnClickListener(v -> left());


        /*
        Random rand = new Random();
        int seed = rand.nextInt()*1000;
        DefaultOrder order = new DefaultOrder(0, Order.Builder.DFS, true, seed);
        MazeFactory factory = new MazeFactory();
        factory.order(order);
        factory.waitTillDelivered();
        Maze m = order.getMaze();
        Maze generation testing
         */

        MazePanel panel = findViewById(R.id.MazePanel);
        playing = new StatePlaying();
        playing.setMaze(GeneratingActivity.maze);
        playing.start(panel);

        panel.commit();

    }

    public void checkWon(){
        if(Arrays.equals(GeneratingActivity.maze.getExitPosition(),playing.getPosition() )){
            Log.v("THREAD", "Exit: " + GeneratingActivity.maze.getExitPosition()[1] + "POS: " + playing.getPosition()[1]);
            toWin();
        }
    }

    /**
     * Moves the user one space forward
     */
    public void forward(){
        steps++;
        playing.handleUserInput(Constants.UserInput.UP,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Moved Forward", Toast.LENGTH_SHORT).show());
        Log.v("forward", "Moved Forward");
        checkWon();
    }

    /**
     * Moves the user one space backward
     */
    public void backward(){
        steps++;
        playing.handleUserInput(Constants.UserInput.DOWN,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Moved Backward", Toast.LENGTH_SHORT).show());
        Log.v("backward", "Moved backward");
        checkWon();
    }

    /**
     * Turns the user to the right
     */
    public void right(){
        playing.handleUserInput(Constants.UserInput.RIGHT,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Turned Right", Toast.LENGTH_SHORT).show());
        Log.v("right", "Turned right");
    }

    /**
     * Turns the user to the left
     */
    public void left(){
        playing.handleUserInput(Constants.UserInput.LEFT,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Turned Left", Toast.LENGTH_SHORT).show());
        Log.v("left", "Turned left");
    }


    /**
     * Shows the user all the walls visible to them
     */
    public void showWalls(){
        playing.handleUserInput(Constants.UserInput.TOGGLELOCALMAP,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Showing visible walls", Toast.LENGTH_SHORT).show());
        Log.v("showWalls", "Showing visible walls");
    }
    /**
     * Shows the user the solution path to the maze
     */
    public void showSolution(){
        playing.handleUserInput(Constants.UserInput.TOGGLESOLUTION,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Showing Solution", Toast.LENGTH_SHORT).show());
        Log.v("showSolution", "Showing Solution");
    }

    /**
     * Shows the user the entire maze
     */
    public void showFullMaze(){
        playing.handleUserInput(Constants.UserInput.TOGGLEFULLMAP,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Showing Full Maze", Toast.LENGTH_SHORT).show());
        Log.v("showFullMaze", "Showing Full Maze");
    }


    /**
     * Zooms the map in
     */
    public void zoomIn(){
        playing.handleUserInput(Constants.UserInput.ZOOMIN,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Zoomed in", Toast.LENGTH_SHORT).show());
        Log.v("zoomIn", "Zoomed in");
    }
    /**
     * Zooms the map out
     */
    public void zoomOut(){
        playing.handleUserInput(Constants.UserInput.ZOOMOUT,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Zoomed out", Toast.LENGTH_SHORT).show());
        Log.v("zoomOut", "Zoomed out");
    }


    public void toTitle(){
        Intent toTitle = new Intent(this, AMazeActivity.class);
        Log.v("Back", "Returning to title");
        startActivity(toTitle);
    }
    public void toWin(){
        Intent win = new Intent(this, WinningActivity.class);
        Log.v("Won", "The game has been won");
        win.putExtra("steps", steps);
        startActivity(win);
    }

    public void jump(){
        steps++;
        playing.handleUserInput(Constants.UserInput.JUMP,1);
        //runOnUiThread(() -> Toast.makeText(PlayManuallyActivity.this, "Zoomed out", Toast.LENGTH_SHORT).show());
        Log.v("Jump Button", "Jumped");
        checkWon();
    }

}