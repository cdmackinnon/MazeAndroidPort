
package com.example.amazebyconnormackinnon.gui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;


public class SQLiteHelper extends SQLiteOpenHelper {

    //reused parameters for sql queries
    public static final String Database = "presets.db";
    public static final String Table = "presets_table";
    public static final String Rooms_Field = "rooms";
    public static final String Algorithm_Field = "algorithm";
    public static final String Difficulty_Field = "difficulty";
    public static final String Seed_Field = "seed";

    public SQLiteHelper(Context context) {
        super(context, Database, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //I should really use the parameters I just created...but this is working code...
        sqLiteDatabase.execSQL("CREATE TABLE " + Table + "(rooms INT,algorithm INT,difficulty INT,seed INT PRIMARY KEY)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ Table);
        onCreate(sqLiteDatabase);
    }

    /** Passes the argyments into an sql insertion into the database for persistent storage
     * @param rooms indicates whether the preset allows rooms
     * @param algorithm indicates which algorithm the preset is using
     * @param difficulty indicates which difficulty the preset is using
     * @param seed indicates which seed the preset is using
     */
    public boolean addPreset(Boolean rooms, int algorithm, int difficulty, int seed){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int bool = rooms ? 1 : 0;
        contentValues.put(Rooms_Field, bool);
        contentValues.put(Algorithm_Field, algorithm);
        contentValues.put(Difficulty_Field, difficulty);
        contentValues.put(Seed_Field, seed);
        long insert = sqLiteDatabase.insert(Table, null, contentValues);
        //Checks that the insert was successful or not
        if (insert == 1){
            return true;
        }
        else{
            return false;
        }

    }

    /**
     *
     * @param rooms indicates whether the preset allows rooms
     * @param algorithm indicates which algorithm the preset is using
     * @param difficulty indicates which difficulty the preset is using
     * @return the most recent seed of the parameters from the database or -1 if the parameters aren't stored
     */
    public int getSeed(Boolean rooms, int algorithm, int difficulty){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        int bool = rooms ? 1 : 0;
        Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM " + Table + " WHERE " + Rooms_Field + " = " + bool + " AND "+ Algorithm_Field + " = " +algorithm +" AND " + Difficulty_Field + " = "+ difficulty, null);
        //checks that the input exists in the database
        if (result.getCount() < 1){
            return -1;
        }
        //chooses the most recent instance of a maze
        result.moveToLast();
        return result.getInt(3);

    }

}
