package com.example.locdvd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalSQLiteOpenHelper extends SQLiteOpenHelper {

    static String DB_NAME="locDVD.db";
    static int DB_VERSION=2;

    public LocalSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlFilTable ="CREATE TABLE DVD(id INTEGER PRIMARY KEY," +
                "titre TEXT, annee NUMERIC, acteurs TEXT, resume TEXT, dateVisionnage NUMERIC);";
        db.execSQL(sqlFilTable);
    }

    public static void deleteDatabase(Context context)  {
        context.deleteDatabase(DB_NAME);
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = oldVersion;i<newVersion;i++)  {
            int versionToUpdate = i+1;
            if(versionToUpdate==2) {
                // Code pour mettre la base de données en version 2
                upgradeToVersion2(db);
            } else if(versionToUpdate==3) {
                // Code pour mettre la base de données en version 3
            }
            //[...]
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        String sqlCommand = "ALTER TABLE DVD ADD COLUMN dateVisionnage NUMERIC";
        db.execSQL(sqlCommand);
    }
}
