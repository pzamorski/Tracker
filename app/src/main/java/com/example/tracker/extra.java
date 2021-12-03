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

public class extra extends Activity {

    private Handler handler= new Handler();;
    private ListView listBluetooth ,dodaneUrzadzenia;
    private Switch trigerBluetooth;
    private ArrayAdapter aAdapter;
    private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra);

        log(String.valueOf(this));

        refreshingLog();

        listBluetooth = (ListView) findViewById(R.id.listBluetooth);
        dodaneUrzadzenia = (ListView) findViewById(R.id.listBluetoothDodane);
        trigerBluetooth = (Switch)findViewById(R.id.switchBluetooth2);

        if (var("autoPlay").trim().equals("true")) {
            trigerBluetooth.setChecked(true);
        }

        trigerBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(trigerBluetooth.isChecked()==true){
                    var("autoPlay","true");
                    log("Auto Play Włączony");
                }
                if(trigerBluetooth.isChecked()==false){
                    var("autoPlay","false");
                    log("Auto Play wyłączony");
                }

            }
        });

        if(bAdapter==null){
            log("Brak wsparcia Bluetooth");
        }
        else{
            Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();

            ArrayList<String> list = new ArrayList<String>();

            if(pairedDevices.size()>0){
                for(BluetoothDevice device: pairedDevices){
                    String devicename = device.getName();
                    String macAddress = device.getAddress();
                    list.add(devicename+"|"+macAddress);
                    log("Nazwa: "+devicename+"\nMAC Addres: "+macAddress);
                }
                MyCustomAdapter adapter = new MyCustomAdapter(list, this,true);
                listBluetooth.setAdapter(adapter);
            }



        }




    }
    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
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

    private  void log(String string){

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());


        boolean toast=false;
        boolean logd=true;
        String tag="main_log";

        if(logd){Log.d(tag,string);}
        if(toast){
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        }

        var("log",currentTime+" "+string+"\n",true);

        //logi.setText("");
        //logi.append(var("log",true));

        ;



    }

    //write
    private void var(String name_file, String data) {
        //write
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(name_file, Context.MODE_PRIVATE));

            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //append write
    private void var(String name_file, String data, boolean append) {
        //write
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(name_file, Context.MODE_APPEND));
            outputStreamWriter.append(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //read without " "
    private String var(String name_file) {

        //read
        String ret = "";

        try {
            InputStream inputStream = getApplicationContext().openFileInput(name_file);

            if (inputStream != null && fileExists(getApplicationContext(), name_file)) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            } else {
                ret = "0";
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret.replaceAll("\n", "");
    }

    //read apened string
    private String var(String name_file, boolean appened) {

        //read
        String ret = "";

        try {
            InputStream inputStream = getApplicationContext().openFileInput(name_file);

            if (inputStream != null && fileExists(getApplicationContext(), name_file)) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            } else {
                ret = "0";
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void refreshingLog() {
        new Thread(new extra.taskRefrechList()).start();
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
                                    if(var(devicename+"|"+macAddress+"M").equals(devicename+"|"+macAddress+"M")) {
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
