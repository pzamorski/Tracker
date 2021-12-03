package com.example.tracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracker.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;
    private boolean mode;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;



    public MyCustomAdapter(ArrayList<String> list, Context context, boolean mode) {
        this.list = list;
        this.context = context;
        this.mode=mode;


    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;//list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.my_custom_list_layout, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        //Handle buttons and add onClickListeners
        Button deleteBtn = (Button)view.findViewById(R.id.delete_btn);
        Button addBtn = (Button)view.findViewById(R.id.add_btn);

        this.pref = this.context.getSharedPreferences("global_para", MODE_PRIVATE);
        this.editor = pref.edit();

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                //list.remove(position); //or some other task
                if(mode==true) {
                    this.setPara(list.get(position) + "M", "off");
                }else {
                    this.setPara(list.get(position), "off");
                }
                notifyDataSetChanged();
            }

            private void setPara(String namePara, String value) {
                    editor.putString(namePara, value);
                    editor.apply();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                if(mode==true) {
                    this.setPara(list.get(position) + "M", list.get(position) + "M");
                }else {
                    this.setPara(list.get(position), list.get(position));
                }
                notifyDataSetChanged();
            }

            private void setPara(String namePara, String value) {
                editor.putString(namePara, value);
                editor.apply();
            }
        });

        return view;
    }


}

