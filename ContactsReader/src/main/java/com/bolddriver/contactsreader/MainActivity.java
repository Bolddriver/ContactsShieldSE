package com.bolddriver.contactsreader;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    int contactsAmount = 0;
    TextView tv_contactsAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tv_contactsAmount = findViewById(R.id.tv_contactsAmount);
        mListView = findViewById(R.id.list_view);
        Button btn_refresh = findViewById(R.id.btn_refresh);

        // 检查应用是否具有读取联系人的权限
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            Toast.makeText(this,"没有读取联系人权限",Toast.LENGTH_SHORT).show();
            return;
        }

        getAndDisplayContacts();
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndDisplayContacts();
            }
        });
    }

    private void getAndDisplayContacts(){
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if(cursor!=null){
            Toast.makeText(this,"获取联系人成功",Toast.LENGTH_SHORT).show();
            contactsAmount = cursor.getCount();
            // 创建简单适配器，将联系人信息显示在列表视图中
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.list_item,
                    cursor,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                    new int[]{R.id.name, R.id.number}
            );
            mListView.setAdapter(adapter);
        } else Toast.makeText(this,"联系人为空",Toast.LENGTH_SHORT).show();
        tv_contactsAmount.setText(String.valueOf(contactsAmount));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getAndDisplayContacts();
        }
    }
}