package com.bolddriver.contactshooker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bolddriver.contactshooker.provider.ContactsInfoContent;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "contactsBold";
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list_view);

        // 检查应用是否具有读取联系人的权限
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            return;
        }

        //查询联系人信息
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        // 创建适配器，将联系人信息显示在列表视图中
        MyAdapter adapter = new MyAdapter(cursor, this);
        mListView.setAdapter(adapter);

        //保存按钮的点击事件
        Button btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = mListView.getCount();
                for (int i = 0; i < count; i++) {
                    View view = mListView.getChildAt(i);
                    CheckBox checkBox = view.findViewById(R.id.cb_itemChecked);
                    if (checkBox.isChecked()) {
                        // 获取选中项的联系人信息
                        cursor.moveToPosition(i);
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // 打印联系人信息
                        Log.d(TAG, "Name: " + name + ", Number: " + number);
                        //将选中的联系人插入ContentProvider
                        ContentValues values = new ContentValues();
                        values.put(ContactsInfoContent.C_NAME, name);
                        values.put(ContactsInfoContent.C_NUMBER, number);
                        getContentResolver().insert(ContactsInfoContent.CONTENT_URI, values);
                        Log.d(TAG, "insert成功");
                    }
                }
                //logd输出ContentProvider中的联系人
                Log.d(TAG, "本地的联系人");
                Cursor mCursor = getContentResolver().query(ContactsInfoContent.CONTENT_URI, null, null, null, null);
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        @SuppressLint("Range") String name = mCursor.getString(mCursor.getColumnIndex(ContactsInfoContent.C_NAME));
                        @SuppressLint("Range") String number = mCursor.getString(mCursor.getColumnIndex(ContactsInfoContent.C_NUMBER));
                        Log.d(TAG, "name:" + name + " numebr:" + number);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //查询联系人信息
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

            // 创建适配器，将联系人信息显示在列表视图中
            MyAdapter adapter = new MyAdapter(cursor, this);
            mListView.setAdapter(adapter);
        }
    }

}