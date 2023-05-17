package com.example.latur_application.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DataBaseHelper.class.getSimpleName();
    // SQLiteDatabase
    SQLiteDatabase db;
    // Context
    Context ctx;
    // DataBase Name
    public static final String DATABASE_NAME = "Latur_Application.db";
    // DataBase Version
    public static final int DATABASE_VERSION = 13;





    //---------------------------------------------------------- Constructor ----------------------------------------------------------------------------------------------------------------------

    public DataBaseHelper(Context c){
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = c;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


    }

//---------------------------------------------------------- Open Database ----------------------------------------------------------------------------------------------------------------

    public void open() {
        db = this.getWritableDatabase();
    }

//---------------------------------------------------------- Close Database ----------------------------------------------------------------------------------------------------------------

    @Override
    public void close() {
        db.close();
    }

//---------------------------------------------------------- Execute Query ----------------------------------------------------------------------------------------------------------------

    public void executeQuery(String query) {
        db.execSQL(query);
    }

//---------------------------------------------------------- Execute Cursor ----------------------------------------------------------------------------------------------------------------

    public Cursor executeCursor(String selectQuery) {
        return db.rawQuery(selectQuery, null);
    }

    // ######################################################### Logout ##############################################################################################################

    public void logout() {
        open();
        close();
    }
}
