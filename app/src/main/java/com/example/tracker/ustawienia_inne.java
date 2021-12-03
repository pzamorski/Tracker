package com.example.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ustawienia_inne extends Activity {
    private EditText editTimeoutGPS;
    private TextView aktualnyKrok;
    private Button buttonSaveTimeoutGPS,resetPara;

    public static final String  DOJAZD_DO_CELU = "0";
    public static final String  DOJAZD_DO_CELU_GPS = "1";
    public static final String  WYJAZD_OD_CELU = "2";
    public static final String  WYJAZD_OD_CELU_GPS = "3";

    public static final String  info = GlobalMethod.info;

    GlobalMethod config=new GlobalMethod(this);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ustawienia_inne);

        config.init("global_para");
        config.init_log("main_logcat","log");

        Intent i = new Intent(getApplicationContext(), pracaWtle.class);
        getApplicationContext().stopService(i);

        editTimeoutGPS=(EditText)findViewById(R.id.editTimeout);
        aktualnyKrok=(TextView)findViewById(R.id.textResetKroku);
        buttonSaveTimeoutGPS = (Button)findViewById(R.id.buttonSaveTimeout);
        resetPara = (Button)findViewById(R.id.resetPara);

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Wybierz Krok","DOJAZD DO CELU", "DOJAZD DO CELU GPS", "WYJAZD OD CELU","WYJAZD OD CELU GPS"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter .setDropDownViewResource(R.layout.spinner);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch ((String) parent.getItemAtPosition(position)){
                    case "DOJAZD DO CELU":
                        aktualnyKrok.setText("Krok: DOJAZD DO CELU");
                        config.setPara("krok",DOJAZD_DO_CELU);
                        break;
                    case "DOJAZD DO CELU GPS":
                        aktualnyKrok.setText("Krok: DOJAZD DO CELU GPS");
                        config.setPara("krok",DOJAZD_DO_CELU_GPS);
                        break;
                    case "WYJAZD OD CELU":
                        aktualnyKrok.setText("Krok: WYJAZD OD CELU");
                        config.setPara("krok",WYJAZD_OD_CELU);
                        break;
                    case "WYJAZD OD CELU GPS":
                        aktualnyKrok.setText("Krok: WYJAZD OD CELU GPS");
                        config.setPara("krok",WYJAZD_OD_CELU_GPS);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        editTimeoutGPS.setText(config.getParaString("timeout_gps"));

        String temp=config.getParaString("krok");
        switch (temp){
            case DOJAZD_DO_CELU:
                aktualnyKrok.setText("Krok: DOJAZD DO CELU");
                break;
            case DOJAZD_DO_CELU_GPS:
                aktualnyKrok.setText("Krok: DOJAZD DO CELU GPS");
                break;
            case WYJAZD_OD_CELU:
                aktualnyKrok.setText("Krok: WYJAZD OD CELU");
                break;
            case WYJAZD_OD_CELU_GPS:
                aktualnyKrok.setText("Krok: WYJAZD OD CELU GPS");
                break;
        }




        buttonSaveTimeoutGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int temp=Integer.parseInt(editTimeoutGPS.getText().toString());
                config.setPara("timeout_gps",temp);
                config.log("Ustawiono Timeout dla GPS",info);
            }
        });
        resetPara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                config.logClear();
                config.delAllPara();
                finish();
                System.exit(0);
            }
        });

    }

}
