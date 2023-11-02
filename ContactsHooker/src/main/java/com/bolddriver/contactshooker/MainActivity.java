package com.bolddriver.contactshooker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bolddriver.contactshooker.provider.ContactsInfoContent;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "contactsBold";
    private ListView mListView;
    private SharedPreferences preferences;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swi_hookEnabled;
    RadioButton rb_null;
    RadioButton rb_selected;

    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list_view);
        RadioGroup rg_return_config = findViewById(R.id.rg_return_config);
        rb_null = findViewById(R.id.rb_null);
        rb_selected = findViewById(R.id.rb_selected);
        swi_hookEnabled = findViewById(R.id.swi_hookEnabled);

        //创建SharedPreference
        try {
            Context context = MainActivity.this;
            preferences = context.getSharedPreferences("HookConfig", Context.MODE_WORLD_READABLE);
        } catch (Exception e) {
            Log.d(TAG, "sharedprefs: "+e);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("错误");
            alertDialogBuilder.setMessage(e.getMessage()+"\n请在Xposed管理器中激活模块");
            alertDialogBuilder.setPositiveButton("确定", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }
        //从SharedPreference中加载配置
        loadConfigFromPref();

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

        //点击选好了按钮
        Button btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除所有数据
                getContentResolver().delete(ContactsInfoContent.CONTENT_URI,null,null);
                //获取选中的信息
                int count = mListView.getCount();
                Map<Integer,Boolean> map = adapter.map;
                for (int i = 0; i < count; i++) {
                    if(Boolean.TRUE.equals(map.get(i))){
                        cursor.moveToPosition(i);
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
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
                Toast.makeText(MainActivity.this,"自定义联系人成功",Toast.LENGTH_SHORT).show();
            }
        });

        //点击保存配置按钮
        Button btn_save_configs = findViewById(R.id.btn_save_configs);
        btn_save_configs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hookEnabled = swi_hookEnabled.isChecked();//开启hook
                int returnMode = 0;
                if(rg_return_config.getCheckedRadioButtonId()==R.id.rb_null) returnMode=1;
                if(rg_return_config.getCheckedRadioButtonId()==R.id.rb_selected) returnMode=2;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("hookEnabled",hookEnabled);
                editor.putInt("returnMode",returnMode);
                editor.apply();
                Toast.makeText(MainActivity.this,"保存配置成功",Toast.LENGTH_SHORT).show();
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

    private void loadConfigFromPref(){
        boolean hookEnabled = preferences.getBoolean("hookEnabled",false);
        int returnMode = preferences.getInt("returnMode",1);
        if(hookEnabled) swi_hookEnabled.setChecked(true);
        switch(returnMode){
            case 1: rb_null.setChecked(true);break;
            case 2:rb_selected.setChecked(true);break;
        }
    }
}