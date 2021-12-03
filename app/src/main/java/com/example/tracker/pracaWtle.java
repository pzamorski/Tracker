package com.example.tracker;

import android.Manifest;
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
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class pracaWtle extends Service {

    GlobalMethod config = new GlobalMethod(this);

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private long UPDATE_INTERVAL_FAST = 1000 * 1;  /* 1 secs */
    private long UPDATE_INTERVAL_SAVE_BATERRY = 1000 * 300; /* 5 minut */

    public static final int POWER_SAVE_METER = 5000;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String CHANNEL_ID2 = "ForegroundServiceChannel2";

    public static final String DOJAZD_DO_CELU = "0";
    public static final String DOJAZD_DO_CELU_GPS = "1";
    public static final String WYJAZD_OD_CELU = "2";
    public static final String WYJAZD_OD_CELU_GPS = "3";

    public static final String info = GlobalMethod.info;
    public static final String stop = GlobalMethod.stop;
    public static final String network = GlobalMethod.network;
    public static final String gps = GlobalMethod.gps;
    public static final String notyfication = GlobalMethod.notyfication;
    public static final String bluetooh = GlobalMethod.bluetooh;


    Location locationA = new Location("HOME");  //lokalizacja bramy
    Location location;

    double distance_in_meter;
    double distans_switch_to_gps;

    int speed;
    int dystans_do_zadziałania;
    int dystans_do_wyjazdu;
    int incress_dyst_of_speed = 0;
    boolean power_save_mode=false;


    @Override
    public void onCreate() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
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

//notyficacja
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Usługa lokalizacji")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setGroup("data")
                .build();
        startForeground(1, notification);

//start jesli bluettoh triger nie właczony
        if (!config.getParaBoolean("trigerBluetoth")) {
            setUpdateNetwork(UPDATE_INTERVAL_FAST);
        }

//zmienne przepisywane przy starcie usługi
        speed = config.getParaInt("speed");
        distans_switch_to_gps = config.getParaInt("distGPS");
        dystans_do_zadziałania = config.getParaInt("dist");
        dystans_do_wyjazdu = config.getParaInt("distKrok1");

        float homeLatitude = config.getParaFloat("lat");
        float homeLongitude = config.getParaFloat("lon");
        if (homeLatitude != 0) {
            locationA.setLatitude(homeLatitude);
        }
        if (homeLongitude != 0) {
            locationA.setLongitude(homeLongitude);
        }


        Toast.makeText(this, "Start usługi", Toast.LENGTH_SHORT).show();

        if (location != null) {
            onLocationChanged(location);
        }
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
        stopLocationUpdates();
        super.onDestroy();
        this.stopSelf();
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                if (config.getParaString(device.getName() + "|" + device.getAddress()).equals(device.getName() + "|" + device.getAddress()) && config.getParaBoolean("trigerBluetoth") == true) {
                    config.log("Bluetooth Połączony: " + device.getName(), bluetooh);
                    UpdateNoification("Lokalizacja: Aktywna.");
                    setUpdateNetwork(UPDATE_INTERVAL_FAST);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && config.getParaBoolean("trigerBluetoth") == true) {
                //Device is about to disconnect
                if (config.getParaString(device.getName() + "|" + device.getAddress()).equals(device.getName() + "|" + device.getAddress()) && config.getParaBoolean("trigerBluetoth") == true) {
                    config.log("Bluetooth Rozłączony: " + device.getName(), bluetooh);
                    stopLocationUpdates();
                    UpdateNoification("Lokalizacja: Nie aktywna.");

                }
            }
        }
    };


    private void openGate(String url) {
        String URL = url;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("{\"success\":true}")) {
                            config.log("Otwieranie bramy", notyfication);
                            notyfication("Otwieram Brame");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Check Error: ", "Error");
                        //Toast.makeText(MainActivity.this, (CharSequence) error, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return new byte[]{};

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_MAX_RETRIES));
        queue.add(request);

    }

    private void NextStep(String NextTo) {
        config.setPara("krok", NextTo);
        switch (config.getParaString("krok")) {
            case DOJAZD_DO_CELU:
                config.log("Krok= DOJAZD_DO_CELU", info);
                break;
            case DOJAZD_DO_CELU_GPS:
                config.log("Krok= DOJAZD_DO_CELU_GPS", info);
                break;
            case WYJAZD_OD_CELU:
                config.log("Krok= WYJAZD_OD_CELU", info);
                break;
            case WYJAZD_OD_CELU_GPS:
                config.log("Krok= WYJAZD_OD_CELU_GPS", info);
                break;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    Context.NOTIFICATION_SERVICE,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void UpdateNoification(String string) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Usługa lokalizacji")
                .setContentText(string)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    private void notyfication(String tytuł) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("myChannelId", "My Channel", importance);
            channel.setDescription("Reminders");
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder mBuilder =
                // Builder class for devices targeting API 26+ requires a channel ID
                new NotificationCompat.Builder(this, "myChannelId")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        //.setContentTitle("Tracker:")
                        .setContentText(tytuł);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(2, mBuilder.build());
    }

    public void setUpdateNetwork(long interval) {
        // Create the location request to start receiving updates
        stopLocationUpdates();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(interval);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        startLocationUpdates();
    }

    public void setUpdateGPS() {
        // Create the location request to start receiving updates
        stopLocationUpdates();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        startLocationUpdates();
        timeout(config.getParaInt("timeout_gps"));
    }

    public void onLocationChanged(Location location) {
//
//Inicjalizacja
        float curentlat = (float) (location.getLatitude());
        float curentlng = (float) (location.getLongitude());
        float current_speed = (float) location.getSpeed();
        config.setPara("curentlat", curentlat);
        config.setPara("curentlng", curentlng);
        distance_in_meter = location.distanceTo(locationA);
        config.setPara("ui_dist", (int) distance_in_meter);
        String step = config.getParaString("krok");
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        int INTdistance_in_meter = (int) distance_in_meter;

//------główny watek--------------------------------------------------------------------------------
        switch (step) {
            case DOJAZD_DO_CELU:
                config.log("Strefa GPS: " + INTdistance_in_meter + " < " + distans_switch_to_gps, network);
                UpdateNoification("Czas: " + currentTime + "\nDojazd do strefy GPS");
                if (distance_in_meter < distans_switch_to_gps) {
                    config.log("W zasiegu dla GPS", network);
                    setUpdateGPS();
                    NextStep(DOJAZD_DO_CELU_GPS);
                }
                break;
            case DOJAZD_DO_CELU_GPS:
                config.log("Strefa Bramy: " + INTdistance_in_meter + " < " + dystans_do_zadziałania + " " + current_speed + " >= " + speed, gps);
                UpdateNoification("Czas: " + currentTime + "\nDojazd do bramy");
                if (distance_in_meter < (dystans_do_zadziałania + incress_dyst_of_speed) && current_speed >= speed) {
                    if (config.getParaBoolean("automat") == true) {
                        config.log("Przygotowanie do otwarcia bramy", notyfication);
                        openGate(config.getParaString("link"));
                    }
                    stopLocationUpdates();
                    NextStep(WYJAZD_OD_CELU);// lub timeout dla gps do kroku DOJAZD_DO_CELU gps->network
                }
                break;
            case WYJAZD_OD_CELU:
                config.log("Wyjazd ze strefy: " + INTdistance_in_meter + " > " + dystans_do_wyjazdu, network);
                UpdateNoification("Czas: " + currentTime + "\nWyjazd ze strefy GPS");
                if (distance_in_meter > dystans_do_wyjazdu) {
                    config.log("Precyzyjny wyjazd z obszaru bramy", network);
                    setUpdateGPS();
                    NextStep(WYJAZD_OD_CELU_GPS);
                }
                break;
            case WYJAZD_OD_CELU_GPS:
                config.log("Precyzyjny wyjazd: " + INTdistance_in_meter + " > " + dystans_do_wyjazdu, gps);
                UpdateNoification("Czas: " + currentTime + "\nWyjazd ze strefy GPS");
                if (distance_in_meter > dystans_do_wyjazdu) {
                    config.log("Wyjazd z obszaru bramy", gps);
                    setUpdateNetwork(UPDATE_INTERVAL_FAST);
                    NextStep(DOJAZD_DO_CELU); // lub timeout dla gps do kroku WYJAZD_OD_CELU gps->network
                }
                break;
        }
//-----oszczedzanie energi-------------------------------------------------------------------------------
        if(distance_in_meter>POWER_SAVE_METER&&power_save_mode==false){
            config.log("Power Save On",info);
            setUpdateNetwork(UPDATE_INTERVAL_SAVE_BATERRY);
            power_save_mode=true;
        }else if(distance_in_meter<POWER_SAVE_METER&&power_save_mode==true){
            config.log("Power Save Off",info);
            setUpdateNetwork(UPDATE_INTERVAL_FAST);
            power_save_mode=false;
        }
    }

    private void startLocationUpdates() {
        config.log("Start Lokalizacji",info);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        config.log("Stop lokalizacja", stop);
    }

    private void timeout(int s){

        new CountDownTimer(s*1000,1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                if(config.getParaString("krok").equals(DOJAZD_DO_CELU_GPS)){
                    config.log("Timeout Usługi 1",gps);
                    notyfication("Timeout GPS");
                    NextStep(DOJAZD_DO_CELU);
                    setUpdateNetwork(UPDATE_INTERVAL_FAST);}

                if(config.getParaString("krok").equals(WYJAZD_OD_CELU_GPS)){
                    config.log("Timeout Usługi 3",gps);
                    notyfication("Timeout GPS");
                    NextStep(WYJAZD_OD_CELU);
                    setUpdateNetwork(UPDATE_INTERVAL_FAST);}

            }
        }.start();
    }
}