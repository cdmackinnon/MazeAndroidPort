package com.example.amazebyconnormackinnon.gui.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.amazebyconnormackinnon.R;
import com.example.amazebyconnormackinnon.gui.Activities.AMazeActivity;

public class LosingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_losing);
        //back button to title
        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener( v -> toTitle());

        Bundle extras = getIntent().getExtras();
        TextView path = findViewById(R.id.PathLen);
        path.setText("Path Length: " + extras.getInt("steps"));

        TextView optimal = findViewById(R.id.Optimalpath);

        optimal.setText("Optimal Path: " + String.valueOf(GeneratingActivity.maze.getDistanceToExit(GeneratingActivity.maze.getStartingPosition()[0],GeneratingActivity.maze.getStartingPosition()[1])-1));

        TextView nrg = findViewById(R.id.energy);
        nrg.setText("Energy: "+ String.valueOf((int)extras.getFloat( "energy")));


    }

    public void toTitle(){
        Intent toTitle = new Intent(this, AMazeActivity.class);
        Log.v("Back", "Returning to title");
        startActivity(toTitle);
    }
}