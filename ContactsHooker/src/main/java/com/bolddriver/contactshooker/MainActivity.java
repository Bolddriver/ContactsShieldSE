package com.bolddriver.contactshooker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bolddriver.contactshooker.provider.ContactsInfoContent;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "contactsBold";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            ContentValues values = new ContentValues();
            values.put(ContactsInfoContent.C_NAME, "hello");
            values.put(ContactsInfoContent.C_NUMBER, "123123123123");
            getContentResolver().insert(ContactsInfoContent.CONTENT_URI, values);
            Log.d(TAG, "insert成功");

            Cursor cursor = getContentResolver().query(ContactsInfoContent.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsInfoContent.C_NAME));
                    @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsInfoContent.C_NUMBER));
                    Log.d(TAG, "name:" + name + " numebr:" + number);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "exception:" + e);
        }
    }
}