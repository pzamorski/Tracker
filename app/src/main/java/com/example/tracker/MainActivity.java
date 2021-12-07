package com.example.tracker;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;

import android.content.Context;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


import android.content.Intent;

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

import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    public static final String  DOJAZD_DO_CELU = "0";
    public static final String  DOJAZD_DO_CELU_GPS = "1";
    public static final String  WYJAZD_OD_CELU = "2";
    public static final String  WYJAZD_OD_CELU_GPS = "3";

    public static final String  info = GlobalMethod.info;

    GlobalMethod config=new GlobalMethod(this);


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private Handler handler = new Handler();

    private TextView latituteField, longitudeField, distField, do_celu, krok, textTimeout, logi;
    private Button ustawieniaLokalizacji, ustawieniaWykonywania, ustawieniaBluettoh, ustawieniaInne, cleatLog, start;
    private Switch automat, refresh;private ProgressBar load_dist,load_timeout;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        config.init("global_para");
        config.init_log("main_logcat","log");
        config.log("Start",info);


        permision();
        firstStartApp();
        inicjalizacja();
        zdarzenia();



    }

    @Override
    protected void onResume() {

        latituteField.setText("Current Latitude: ?");
        longitudeField.setText("Current Longitude: ?");
        distField.setText("Odległość: ");


        if (config.getParaBoolean("automat")) {
            automat.setChecked(true);
        }
        do_celu.setText("Otwieranie Bramy:"+config.getParaString("dist")+" m");

        String temp=config.getParaString("krok");
            switch (temp){
                case DOJAZD_DO_CELU:
                    krok.setText("Krok: DOJAZD DO CELU");
                    break;
                case DOJAZD_DO_CELU_GPS:
                    krok.setText("Krok: DOJAZD DO CELU GPS");
                    break;
                case WYJAZD_OD_CELU:
                    krok.setText("Krok: WYJAZD OD CELU");
                    break;
                case WYJAZD_OD_CELU_GPS:
                    krok.setText("Krok: WYJAZD OD CELU GPS");
                    break;
            }

        textTimeout.setText("Timeout GPS: "+config.getParaString("timeout_gps")+" s");

        config.log("Resume Main Activity",info);
        config.setPara("isAktive",true);
        super.onResume();
    }

    @Override
    protected void onStart() {
        config.setPara("isAktive",true);
        super.onStart();
    }

    @Override
    protected void onPause() {
        config.setPara("isAktive",false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        config.setPara("isAktive",false);
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void refreshing() {
        new Thread(new taskRefrechLog()).start();
    }

    class taskRefrechLog implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        latituteField.setText("Current Latitude: "+config.getParaString("curentlat"));
                        longitudeField.setText("Current Longitude: "+config.getParaString("curentlng"));
                        distField.setText("Odległość: "+config.getParaString("ui_dist")+" m");

                        //int actualProgres=varStartProgresBar-Integer.parseInt(var("ui_dist"));
//                        if(varStartProgresBar>=actualProgres){
//                            load_dist.setProgress(actualProgres);
//                        }
                        if(refresh.isChecked()==false){
                            logi.setText("");
                            logi.append(Html.fromHtml(config.logLoad()));
                            cleatLog.setText("WYCZYŚĆ LOGI ("+config.getSizeLogFile()+" Kb)");
                        }


                        String temp=config.getParaString("krok");
                        switch (temp){
                            case DOJAZD_DO_CELU:
                                krok.setText("Krok: DOJAZD DO CELU");
                                break;
                            case DOJAZD_DO_CELU_GPS:
                                krok.setText("Krok: DOJAZD DO CELU GPS");
                                break;
                            case WYJAZD_OD_CELU:
                                krok.setText("Krok: WYJAZD OD CELU");
                                break;
                            case WYJAZD_OD_CELU_GPS:
                                krok.setText("Krok: WYJAZD OD CELU GPS");
                                break;
                        }

                        if(isMyServiceRunning(bluetooth.class)){
                            start.setBackgroundColor(Color.parseColor("#FF47780E"));

                        }else {
                            start.setBackgroundColor(Color.parseColor("#CF1414"));
                        }

                    }
                });

            }
        }
    }

    private void permision(){
        if(!checkPermissions()){
            requestPermissions();
        }
        if(!checkPermissions2()){
            requestPermissions2();
        }
    }

    private  void firstStartApp(){

        if (!config.getParaBoolean("firstStartApp")) {
            // here you can launch another activity if you like

            config.log("PIERSZE URUCHOMIENIE WITAM",info);
            Toast.makeText(this, "PIERSZE URUCHOMIENIE WITAM", Toast.LENGTH_SHORT).show();
            config.setPara("dist",120);
            config.setPara("distGPS",400);
            config.setPara("distKrok1",430);
            config.setPara("speed",0);
            config.setPara("lat", 51.277500f);
            config.setPara("lon",15.565074f);
            config.setPara("link","https://svr26.supla.org/direct/1999/cE3GfcbGn2wRkwL/open-close");
            config.setPara("timeout_gps",300);
            config.setPara("krok","0");
            config.setPara("automat",true);
            config.setPara("odleglosc", 999999);
            config.setPara("bluettohConnect",false);
            config.setPara("curentlat",1.1f);
            config.setPara("curentlng",1.1f);
            config.setPara("ui_dist",99999);
            config.setPara("firstStartApp",true);

        }
    }

    public void inicjalizacja(){

        int dystans_do_zadziałania = config.getParaInt("dist");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        refreshing();

        latituteField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);
        distField = (TextView) findViewById(R.id.TextView05);
        do_celu = (TextView) findViewById(R.id.do_celu);
        krok = (TextView) findViewById(R.id.edit_krok);
        textTimeout = (TextView) findViewById(R.id.textTimeout);
        logi = (TextView) findViewById(R.id.textLogi);
        logi.setMovementMethod(new ScrollingMovementMethod());


        ustawieniaLokalizacji = (Button) findViewById(R.id.activity_ustawienia_lokalizacja);
        ustawieniaWykonywania = (Button) findViewById(R.id.activity_ustawienia_wykonywania);
        ustawieniaBluettoh = (Button) findViewById(R.id.activity_ustawienia_bluetooth);
        ustawieniaInne = (Button) findViewById(R.id.activity_ustawienia_inne);
        cleatLog = (Button) findViewById(R.id.clear_log);
        start = (Button) findViewById(R.id.start);
        load_dist = (ProgressBar) findViewById(R.id.load_dist);
        load_timeout = (ProgressBar) findViewById(R.id.load_progres_timeout);

        automat = (Switch) findViewById(R.id.switchBluetooth);
        refresh = (Switch) findViewById(R.id.refreshLog);

        if (config.getParaBoolean("automat")) {
            automat.setChecked(true);
        }


        do_celu.setText("Otwieranie Bramy:" + config.getParaString("dist") + " m");

        String temp=config.getParaString("krok");
        switch (temp){
            case DOJAZD_DO_CELU:
                krok.setText("Krok: DOJAZD DO CELU");
                break;
            case DOJAZD_DO_CELU_GPS:
                krok.setText("Krok: DOJAZD DO CELU GPS");
                break;
            case WYJAZD_OD_CELU:
                krok.setText("Krok: WYJAZD OD CELU");
                break;
            case WYJAZD_OD_CELU_GPS:
                krok.setText("Krok: WYJAZD OD CELU GPS");
                break;
        }

        textTimeout.setText("Timeout GPS: " + config.getParaString("timeout_gps") + " s");

    }

    public void zdarzenia(){
        ustawieniaLokalizacji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo(ustawienia_lokalizacji.class);
            }
        });
        ustawieniaWykonywania.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo(ustawienia_wykonania.class);
            }
        });
        ustawieniaBluettoh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo(ustawienia_bluetooth.class);
            }
        });
        ustawieniaInne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo(ustawienia_inne.class);
            }
        });

        cleatLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                config.logClear();
                logi.setText(config.logLoad());
                refresh.setChecked(false);
            }
        });

        automat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (automat.isChecked()) {
                    config.setPara("automat", true);
                    config.log("Automatyka włączona",info);
                } else {
                    config.setPara("automat", false);
                    config.log("Automatyka wyłączona",info);
                }

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent i = new Intent(getApplicationContext(), bluetooth.class);
                Intent i2 = new Intent(getApplicationContext(), pracaWtle.class);
                if(!isMyServiceRunning(bluetooth.class)){
                    MainActivity.this.startService(i);}else {
                    MainActivity.this.stopService(i);
                    if(isMyServiceRunning(pracaWtle.class))
                    {
                        MainActivity.this.stopService(i2);
                    }
                }
            }
        });

        logi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                refresh.setChecked(true);
                return false;
            }
        });

    }

    public void switchTo(Class startClass){
        Intent myIntent = new Intent(MainActivity.this,startClass);
        MainActivity.this.startActivity(myIntent);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions2() {
    boolean shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION);

    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if (shouldProvideRationale) {
        Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    }
                })
                .show();
    } else {
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}

    private boolean checkPermissions2() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }





}