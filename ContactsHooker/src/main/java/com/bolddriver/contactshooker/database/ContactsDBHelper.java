package com.bolddriver.contactshooker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactsDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "contacts.db";
    public static final String TABLE_NAME = "contacts_info";
    private static final int DB_VERSION = 1;
    private static ContactsDBHelper mHelper = null;

    private ContactsDBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    public static ContactsDBHelper getInstance(Context context){
        if (mHelper == null) {
            mHelper = new ContactsDBHelper(context);
        }
        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " display_name VARCHAR NOT NULL," +
                " data1 VARCHAR NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}