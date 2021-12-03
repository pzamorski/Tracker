package com.example.tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

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
import java.util.Set;

public class ustawienia_bluetooth extends Activity {

    public static final String  info = GlobalMethod.info;

    private Handler handler= new Handler();;
    private ListView listBluetooth ,dodaneUrzadzenia;
    private Switch trigerBluetooth;
    private ArrayAdapter aAdapter;
    private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();

    GlobalMethod config=new GlobalMethod(this);

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ustawienia_bluetooth);

        config.init("global_para");
        config.init_log("main_logcat","log");

        Intent i = new Intent(getApplicationContext(), pracaWtle.class);
        getApplicationContext().stopService(i);

        config.log(String.valueOf(this),info);

        refreshingLog();

        listBluetooth = (ListView) findViewById(R.id.listBluetooth);
        dodaneUrzadzenia = (ListView) findViewById(R.id.listBluetoothDodane);
        trigerBluetooth = (Switch)findViewById(R.id.switchBluetooth);

        if (config.getParaBoolean("trigerBluetoth")) {
            trigerBluetooth.setChecked(true);
        }

        trigerBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(trigerBluetooth.isChecked()==true){
                    config.setPara("trigerBluetoth",true);
                    config.log("Triger Bluetooth Włączony",info);
                }
                if(trigerBluetooth.isChecked()==false){
                    config.setPara("trigerBluetoth",false);
                    config.log("Triger Bluetooth wyłączony",info);
                }

            }
        });

        if(bAdapter==null){
            config.log("Brak wsparcia Bluetooth",info);
        }
        else{
            Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();

            ArrayList<String> list = new ArrayList<String>();

            if(pairedDevices.size()>0){
                for(BluetoothDevice device: pairedDevices){
                    String devicename = device.getName();
                    String macAddress = device.getAddress();
                    list.add(devicename+"|"+macAddress);
                    config.log("Nazwa: "+devicename+"\nMAC Addres: "+macAddress,info);
                }
                MyCustomAdapter adapter = new MyCustomAdapter(list, this,false);
                listBluetooth.setAdapter(adapter);
            }



        }




    }

    public void refreshingLog() {
        new Thread(new ustawienia_bluetooth.taskRefrechList()).start();
    }

    class taskRefrechList implements Runnable {
        @Override
        public void run() {


            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(bAdapter==null){
                            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
                            ArrayList list = new ArrayList();
                            if(pairedDevices.size()>0){
                                for(BluetoothDevice device: pairedDevices){

                                    String devicename = device.getName();
                                    String macAddress = device.getAddress();
                                    if(config.getParaString(devicename+"|"+macAddress).equals(devicename+"|"+macAddress)) {
                                        list.add(devicename + " [" + macAddress+"]");
                                    }
                                }
                                aAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                                dodaneUrzadzenia.setAdapter(aAdapter);
                            }
                        }
                    }


                });

            }
        }
    }

}
