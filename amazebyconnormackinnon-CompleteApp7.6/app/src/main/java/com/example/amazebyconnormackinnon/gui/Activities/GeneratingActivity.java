package com.example.amazebyconnormackinnon.gui.Activities;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.amazebyconnormackinnon.R;
import com.example.amazebyconnormackinnon.generation.DefaultOrder;
import com.example.amazebyconnormackinnon.generation.Maze;
import com.example.amazebyconnormackinnon.generation.MazeFactory;
import com.example.amazebyconnormackinnon.generation.Order;

public class GeneratingActivity extends AppCompatActivity {
    int driver_type = -1;
    int robot_type = -1;
    int seed;
    boolean rooms;
    int algorithm;
    int difficulty;
    DefaultOrder order;
    public static Maze maze;
    private MazeFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generating);

        //separate thread needed for building the maze and updating the progress bar
        //for now we pretend the maze is being generated
        ProgressBar bar = findViewById(R.id.progressBar);
        progressThread(bar);

        //back button to title
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener( v -> toTitle());

        //updates the driver state updates:
        RadioButton manual = findViewById(R.id.Manual);
        manual.setOnClickListener( v -> Manual() );
        RadioButton wizard = findViewById(R.id.Wizard);
        wizard.setOnClickListener( v -> Wizard() );
        RadioButton wallF = findViewById(R.id.Wall_Follower);
        wallF.setOnClickListener( v -> WallFollower() );
        RadioButton premium = findViewById(R.id.PremiumButton);
        premium.setOnClickListener( v -> Premium() );
        RadioButton mediocre = findViewById(R.id.MediocreButton);
        mediocre.setOnClickListener( v -> Mediocre() );
        RadioButton soso = findViewById(R.id.SoSoButton);
        soso.setOnClickListener( v -> Soso() );
        RadioButton shaky = findViewById(R.id.ShakyButton);
        shaky.setOnClickListener( v -> Shaky() );
        /*
        Random rand = new Random();
        int seed = rand.nextInt()*1000;
        DefaultOrder order = new DefaultOrder(0, Order.Builder.DFS, true, seed);
        MazeFactory factory = new MazeFactory();
        factory.order(order);
        factory.waitTillDelivered();
        Maze m = order.getMaze();
        MAZE GENERATION TESTING
         */
        Bundle extras = getIntent().getExtras();
        seed = extras.getInt("Seed");
        rooms = extras.getBoolean("Rooms");
        //0 = DFS 1= Boruvka 2 = Prim
        algorithm = extras.getInt("Algorithm");
        difficulty = extras.getInt("Level");
        generateMaze(seed,algorithm,rooms,difficulty);
    }

    private void generateMaze(int seed, int algorithm, boolean rooms, int difficulty) {
        switch (algorithm) {
            case 1:
                order = new DefaultOrder(difficulty, Order.Builder.Boruvka, rooms, seed);
                break;
            case 2:
                order = new DefaultOrder(difficulty, Order.Builder.Prim, rooms, seed);
                break;
            default:
                order = new DefaultOrder(difficulty, Order.Builder.DFS, rooms, seed);
                break;
        }
        factory = new MazeFactory();
        factory.order(order);
    }

    private void Manual(){
        driver_type = 0;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Manual Driver", Toast.LENGTH_SHORT).show());
        Log.v("Manual", "Manual Driver");
    }
    private void Wizard(){
        driver_type = 1;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Wizard Driver", Toast.LENGTH_SHORT).show());
        Log.v("Wizard", "Wizard Driver");
    }
    private void WallFollower() {
        driver_type = 2;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Wall Follower Driver", Toast.LENGTH_SHORT).show());
        Log.v("WallFollower", "Wall Follower Driver");
    }
    private void Premium() {
        robot_type = 0;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Premium Robot", Toast.LENGTH_SHORT).show());
        Log.v("Premium", "Premium Robot");
    }
    private void Mediocre() {
        robot_type = 1;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Mediocre Robot", Toast.LENGTH_SHORT).show());
        Log.v("Mediocre", "Mediocre Robot");
    }
    private void Soso() {
        robot_type = 2;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "So-So Robot", Toast.LENGTH_SHORT).show());
        Log.v("SoSp", "SoSo Robot");
    }
    private void Shaky() {
        robot_type = 3;
        runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Shaky Robot", Toast.LENGTH_SHORT).show());
        Log.v("Shaky", "Shaky Robot");
    }

    private void startManual(){
        Intent manual_activity = new Intent(getBaseContext(), PlayManuallyActivity.class);
        startActivity(manual_activity);
    }

    private void startAnimation(){
        Intent autoActivity = new Intent(getBaseContext(), PlayAnimationActivity.class);
        autoActivity.putExtra("DriverType",driver_type);
        autoActivity.putExtra("RoboType", robot_type);
        startActivity(autoActivity);
    }

    private void toTitle(){
        Intent toTitle = new Intent(this, AMazeActivity.class);
        driver_type = -2;
        robot_type = -2;
        startActivity(toTitle);
    }

    private void progressThread(ProgressBar bar){
        new Thread(new Runnable() {
            public void run() {
                while (bar.getProgress() != 100) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bar.setProgress(order.getProgress());
                }
                //progress bar and maze generation can be several milliseconds off
                //this ensures the maze is delivered on time
                factory.waitTillDelivered();
                maze = order.getMaze();
                if (driver_type == -1) {
                    runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Choose a driver to continue!", Toast.LENGTH_LONG).show());
                    Log.v("ProgressBar", "Driver Needed");
                }
                while (driver_type == -1) {
                    try {
                        //LENGTH_LONG in toast is 3500 ms
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (driver_type == 0){
                    //ends current thread
                    try {Thread.currentThread().interrupt(); } catch (Exception e){}
                    runOnUiThread(() ->startManual());
                }
                else if (driver_type == 1 || driver_type == 2){
                    runOnUiThread(() -> Toast.makeText(GeneratingActivity.this, "Choose a robot type to continue!", Toast.LENGTH_LONG).show());
                    Log.v("ProgressBar", "Robot Needed");
                    while(robot_type == -1){
                        try {
                            //LENGTH_LONG in toast is 3500 ms
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {Thread.currentThread().interrupt(); } catch (Exception e) {}
                    runOnUiThread(() -> startAnimation());
                }
                //if the user returns to the title the thread is terminated
                //This is done by changing the driver and robot variables
                else{
                    factory.cancel();
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}