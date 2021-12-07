package com.example.tracker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class bluetooth extends Service {

    GlobalMethod config = new GlobalMethod(this);


    public static final String info = GlobalMethod.info;
    public static final String bluetooh = GlobalMethod.bluetooh;

    Context context;


    @Override
    public void onCreate() {
        context=getBaseContext();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        config.init("global_para");
        config.init_log("main_logcat", "log");
//dla bluetooh
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);


//start jesli bluettoh triger nie właczony
        if (!config.getParaBoolean("trigerBluetoth")) {
            Intent i = new Intent(context, pracaWtle.class);
            if(!isMyServiceRunning(pracaWtle.class))
            {
                context.startService(i);
            }
        }

//zmienne przepisywane przy starcie usługi

        Toast.makeText(this, "Start usługi", Toast.LENGTH_SHORT).show();

        config.log("Start", info);
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Stop usługi", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        this.stopSelf();
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Intent i = new Intent(context, pracaWtle.class);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                if (config.getParaString(device.getName() + "|" + device.getAddress()).equals(device.getName() + "|" + device.getAddress()) && config.getParaBoolean("trigerBluetoth") == true) {
                    config.log("Bluetooth Połączony: " + device.getName(), bluetooh);
                    if(!isMyServiceRunning(pracaWtle.class))
                    {
                        context.startService(i);
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && config.getParaBoolean("trigerBluetoth") == true) {
                //Device is about to disconnect
                if (config.getParaString(device.getName() + "|" + device.getAddress()).equals(device.getName() + "|" + device.getAddress()) && config.getParaBoolean("trigerBluetoth") == true) {
                    config.log("Bluetooth Rozłączony: " + device.getName(), bluetooh);
                    if(isMyServiceRunning(pracaWtle.class))
                    {
                        context.stopService(i);
                    }

                }
            }
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}