package com.example.amazebyconnormackinnon.gui.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.amazebyconnormackinnon.R;
import com.example.amazebyconnormackinnon.gui.SQLiteHelper;



public class AMazeActivity extends AppCompatActivity {
    private boolean rooms = true;
    //0 = DFS 1= Boruvka 2 = Prim
    private int algorithm = 0;
    private int difficulty = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button explore_button;
        Button revisit;
        //Initializes and sets view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toggles the presence of rooms or not for generation
        Switch room_switch = findViewById(R.id.room_switch);
        room_switch.setOnClickListener(v -> toggleRooms());

        //Begins generation for a new unique maze depending on the user selections
        explore_button = findViewById(R.id.explore_button);
        explore_button.setOnClickListener(v -> startGenerating());

        //this will eventually be changed to pass specific
        //arguments calling for an identical maze generated
        revisit = findViewById(R.id.Revisit_button);
        revisit.setOnClickListener(v -> revisit());

        //Updates which algorithm is being used
        RadioButton dfs = findViewById(R.id.DFS_button);
        dfs.setOnClickListener(v -> dfs());
        RadioButton boruvka = findViewById(R.id.Boruvka_button);
        boruvka.setOnClickListener(v -> boruvka());
        RadioButton prim = findViewById(R.id.Prim_button);
        prim.setOnClickListener(v -> prim());

        //updates when the difficulty is changed
        SeekBar dif = findViewById(R.id.difficult_seekBar);
        dif.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                difficulty = seekBar.getProgress();
                //Toast.makeText(AMazeActivity.this, "Difficulty " + difficulty, Toast.LENGTH_SHORT).show();
                Log.v("Difficulty Meter", "Difficulty Updated");
            }
        });

    }
    //sets the correct algorithm
    private void dfs(){algorithm=0;
        //runOnUiThread(() -> Toast.makeText(AMazeActivity.this, "Algorithm: DFS", Toast.LENGTH_SHORT).show());
        Log.v("algorithm", "DFS");}
    private void boruvka(){algorithm=1;
        //runOnUiThread(() -> Toast.makeText(AMazeActivity.this, "Algorithm: Boruvka", Toast.LENGTH_SHORT).show());
        Log.v("algorithm", "Boruvka");}
    private void prim(){algorithm=2;
        //runOnUiThread(() -> Toast.makeText(AMazeActivity.this, "Algorithm: Prim", Toast.LENGTH_SHORT).show());
        Log.v("algorithm", "Prim");}


    /** Passes maze parameters to Generating Activity using a new and randomized seed
     * The parameters and generated seed are stored in a SQLite database for
     * persistent storage and later retrieval
     */
    public void startGenerating(){
        int seed = (int)(Math.random()*(10000));
        Log.v("Seed:", " " + seed);
        Intent gen_activity = new Intent(this, GeneratingActivity.class);
        gen_activity.putExtra("Seed", seed);
        gen_activity.putExtra("Rooms", rooms);
        gen_activity.putExtra("Algorithm", algorithm);
        gen_activity.putExtra("Level", difficulty);
        SQLiteHelper db = new SQLiteHelper(AMazeActivity.this);
        db.addPreset(rooms, algorithm, difficulty, seed);
        startActivity(gen_activity);
    }

    /**Refers to the SQLite Database for the previously visited seed. This
     * seed is then passed to a new Generating Activity
     */
    public void revisit(){
        SQLiteHelper db = new SQLiteHelper(getApplicationContext());
        int seed = db.getSeed(rooms, algorithm, difficulty);

        if (seed == -1){
            Toast.makeText(AMazeActivity.this, "Nothing to revisit!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.v("Seed:", " " + seed);


        Intent gen_activity = new Intent(this, GeneratingActivity.class);
        gen_activity.putExtra("Seed", seed);
        gen_activity.putExtra("Rooms", rooms);
        gen_activity.putExtra("Algorithm", algorithm);
        gen_activity.putExtra("Level", difficulty);
        startActivity(gen_activity);
    }

    /**Toggles the presence of rooms or not for generation
     * Alerts the user with Toast
     */
    private void toggleRooms(){
        rooms = !rooms;
        if (rooms) {
            //runOnUiThread(() -> Toast.makeText(AMazeActivity.this, "Rooms Enabled", Toast.LENGTH_SHORT).show());
            Log.v("Toggle rooms", "Rooms Enabled");
        }
        else{
            //runOnUiThread(() -> Toast.makeText(AMazeActivity.this, "Rooms Disabled", Toast.LENGTH_SHORT).show());
            Log.v("Toggle rooms", "Rooms Disabled");
        }
    }

}