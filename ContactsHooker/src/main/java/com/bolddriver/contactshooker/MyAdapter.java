package com.bolddriver.contactshooker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bolddriver.contactshooker.provider.ContactsInfoContent;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAdapter extends BaseAdapter {
    private List<String> name;
    private List<String> number;
    private Context context;
    public Map<Integer,Boolean> map=new HashMap<>();
    @SuppressLint("Range")
    public MyAdapter(Cursor cursor, Context context){
        this.name = new ArrayList<String>();
        this.number = new ArrayList<String>();
        this.context=context;
        if(cursor!=null){
            int pos=0;
            while(cursor.moveToNext()){
                String iname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String inumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                this.name.add(iname);
                this.number.add(inumber);
            }
        }
    }
    @Override
    public int getCount() {
        //return返回的是int类型，也就是页面要显示的数量。
        return name.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView==null){
            //通过一个打气筒 inflate 可以把一个布局转换成一个view对象
            view=View.inflate(context,R.layout.list_item,null);
        }else {
            view=convertView;//复用历史缓存对象
        }
        //单选按钮的文字
        TextView radioText=(TextView)view.findViewById(R.id.name);
        radioText.setText(name.get(position));
        TextView radioNumber = (TextView)view.findViewById(R.id.number);
        radioNumber.setText(number.get(position));
        //单选按钮
        final CheckBox checkBox=(CheckBox)view.findViewById(R.id.cb_itemChecked);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()){
                    map.put(position,true);

                }else {
                    map.remove(position);

                }
            }
        });
        if(map!=null&&map.containsKey(position)){
            checkBox.setChecked(true);
        }else {
            checkBox.setChecked(false);
        }
        return view;
    }
}
