package com.example.tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ustawienia_wykonania extends Activity {
    private EditText link;
    private Button saveLink;

    public static final String  info = GlobalMethod.info;


    GlobalMethod config=new GlobalMethod(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ustawienia_wykonania);

        config.init("global_para");

        Intent i = new Intent(getApplicationContext(), pracaWtle.class);
        getApplicationContext().stopService(i);
        
        config.log(String.valueOf(this),info);

        link=(EditText)findViewById(R.id.editLink);
        saveLink = (Button)findViewById(R.id.saveLink);

        link.setText(config.getParaString("link"));

        saveLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                config.setPara("link", link.getText().toString());
                config.log("Link zapisany",info);
            }
        });

    }
}
