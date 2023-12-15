package com.bolddriver.contactshooker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bolddriver.contactshooker.provider.ContactsInfoContent;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "contactsBold";
    private ListView mListView;
    private SharedPreferences preferences;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swi_hookEnabled;
    RadioButton rb_null;
    RadioButton rb_selected;
    MyAdapter adapter;
    Cursor cursor;
//    Button btn_save;
    Button btn_save_configs;
    RadioGroup rg_return_config;

    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list_view);
        rg_return_config = findViewById(R.id.rg_return_config);
        rb_null = findViewById(R.id.rb_null);
        rb_selected = findViewById(R.id.rb_selected);
        swi_hookEnabled = findViewById(R.id.swi_hookEnabled);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btn_save_configs = findViewById(R.id.btn_save_configs);
        btn_save_configs.setOnClickListener(this);

        //SharedPreferences相关
        try {
            //创建SharedPreferences
            Context context = MainActivity.this;
            preferences = context.getSharedPreferences("HookConfig", Context.MODE_WORLD_READABLE);
            //从SharedPreferences中加载配置
            loadConfigFromPref();
        } catch (Exception e) {
            btn_save_configs.setEnabled(false);
            Log.d(TAG, "sharedprefs: "+e);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("错误");
            alertDialogBuilder.setMessage(e.getMessage()+"\n请在Xposed管理器中激活模块");
            alertDialogBuilder.setPositiveButton("确定", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        // 检查应用是否具有读取联系人的权限
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            return;
        }
        displayContacts dContacts = new displayContacts();
        dContacts.start();
    }

    class displayContacts extends Thread{
        @Override
        public void run() {
            MyAdapter myAdapter = getContacts();
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mListView.setAdapter(myAdapter);
                        }
                    }
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            displayContacts dContacts = new displayContacts();
            dContacts.start();
        }
        else{
            Toast.makeText(this,"未授予通讯录权限",Toast.LENGTH_SHORT).show();
        }
    }

    private void loadConfigFromPref(){
        boolean hookEnabled = preferences.getBoolean("hookEnabled",false);
        int returnMode = preferences.getInt("returnMode",1);
        if(hookEnabled) swi_hookEnabled.setChecked(true);
        switch(returnMode){
            case 1: rb_null.setChecked(true);break;
            case 2: rb_selected.setChecked(true);break;
        }
    }

    private MyAdapter getContacts(){
        //查询联系人信息
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        adapter = new MyAdapter(cursor, this);
        return adapter;
    }

    @Override
    public void onClick(View v) {
        //保存模块的开启状态和保护策略
        boolean hookEnabled = swi_hookEnabled.isChecked();//开启hook
        int returnMode = 0;
        if(rg_return_config.getCheckedRadioButtonId()==R.id.rb_null) returnMode=1;
        if(rg_return_config.getCheckedRadioButtonId()==R.id.rb_selected) returnMode=2;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hookEnabled",hookEnabled);
        editor.putInt("returnMode",returnMode);
        editor.apply();
        Toast.makeText(MainActivity.this,"保存配置成功",Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this,"没有闪退",Toast.LENGTH_SHORT).show();

        //保存选中的联系人
        if(rg_return_config.getCheckedRadioButtonId()==R.id.rb_selected){
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
//                    Log.d(TAG, "insert成功");
                }
            }
            //logd输出ContentProvider中的联系人
//            Log.d(TAG, "本地的联系人");
//            Cursor mCursor = getContentResolver().query(ContactsInfoContent.CONTENT_URI, null, null, null, null);
//            if (mCursor != null) {
//                while (mCursor.moveToNext()) {
//                    @SuppressLint("Range") String name = mCursor.getString(mCursor.getColumnIndex(ContactsInfoContent.C_NAME));
//                    @SuppressLint("Range") String number = mCursor.getString(mCursor.getColumnIndex(ContactsInfoContent.C_NUMBER));
//                    Log.d(TAG, "name:" + name + " numebr:" + number);
//                }
//            }
            Toast.makeText(MainActivity.this,"保存联系人成功",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}