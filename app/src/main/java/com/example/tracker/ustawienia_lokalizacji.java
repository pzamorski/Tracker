//        locationA.setLatitude(51.277536);
//        locationA.setLongitude(15.564793);

package com.example.tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
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

public class ustawienia_lokalizacji extends Activity {

    public static final String  info = GlobalMethod.info;
    private EditText odlegloscField, editLat, editLon,odlegloscFieldGPS, editSpeed, editDistKrok1;
    private Button saveDist,saveDistGPS, saveLok, buttonSpeed,saveDistKrok1;

    GlobalMethod config=new GlobalMethod(this);



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ustawienia_lokalizacji);

        config.init("global_para");
        config.init_log("main_logcat","log");

        odlegloscField =(EditText)findViewById(R.id.odleglosc);
        odlegloscFieldGPS=(EditText)findViewById(R.id.odleglosc_gps);
        editSpeed=(EditText)findViewById(R.id.editSpeed);
        editLat =(EditText)findViewById(R.id.lat);
        editLon =(EditText)findViewById(R.id.lon);
        editDistKrok1=(EditText)findViewById(R.id.odleglosc_krok1);



        saveDist =(Button) findViewById(R.id.save_dist);
        saveDistGPS =(Button) findViewById(R.id.save_dist_gps);
        buttonSpeed =(Button) findViewById(R.id.buttonSpeed);
        saveLok =(Button) findViewById(R.id.save_lok);
        saveDistKrok1=(Button) findViewById(R.id.save_dist_network);

        odlegloscField.setText(config.getParaString("dist"));
        odlegloscFieldGPS.setText(config.getParaString("distGPS"));
        editDistKrok1.setText(config.getParaString("distKrok1"));
        editSpeed.setText(config.getParaString("speed"));
        editLat.setText(config.getParaString("lat"));
        editLon.setText(config.getParaString("lon"));

        saveDist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp=Integer.parseInt(odlegloscField.getText().toString());
                config.setPara("dist", temp);
                odlegloscField.setText(config.getParaString("dist"));
                config.log("Zapisano dystans zadziałania: "+config.getParaString("dist"),info);
            }
        });


        saveDistGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp=Integer.parseInt(odlegloscFieldGPS.getText().toString());
                config.setPara("distGPS",temp);
                odlegloscFieldGPS.setText(config.getParaString("distGPS"));
                config.log("Zapisano dystans dla GPS: "+config.getParaString("distGPS"),info);
            }
        });
        saveDistKrok1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp=Integer.parseInt(editDistKrok1.getText().toString());
                config.setPara("distKrok1", temp);
                editDistKrok1.setText(config.getParaString("distKrok1"));
                config.log("Zapisano dystans dla Kroku 1: "+config.getParaString("distKrok1"),info);
            }
        });

        buttonSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp=Integer.parseInt(editSpeed.getText().toString());
                config.setPara("speed", temp);
                editSpeed.setText(config.getParaString("speed"));
                config.log("Zapisano prędkość zadziałania: "+config.getParaString("speed"),info);
            }
        });

        saveLok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float latTemp=Float.parseFloat(editLat.getText().toString());
                float lonTemp=Float.parseFloat(editLon.getText().toString());
                config.setPara("lat",latTemp);
                config.setPara("lon",lonTemp);
                editLat.setText(config.getParaString("lat"));
                editLon.setText(config.getParaString("lon"));
                config.log("Zapisano lokalizacje",info);
            }
        });
    }

    }
