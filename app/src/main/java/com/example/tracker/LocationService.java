package com.example.tracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class LocationService extends IntentService implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String TAG = LocationService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Context context;

    private static final long INTERVAL = 1000 * 30 * 60;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    private static final long MEDIUM_INTERVAL = 1000 * 1;

    public static final String CHANNEL_ID_LOCATION = "ForegroundServiceChannel";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *

     */   public LocationService() {
        super("LocationService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        super.onHandleIntent(intent);
        Start();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());

    }





    public void Start(){
        Create();
        StartCommand();
        //setNotyfi();


    }

    private void Create() {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void StartCommand() {

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            startLocationUpdates();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MEDIUM_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_LOCATION,
                    Context.NOTIFICATION_SERVICE,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void UpdateNoification(String string) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_LOCATION)
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

    public void Destroy() {
        stopLocationUpdates();
        Log.v(TAG, "Service Stopped!");
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.v(TAG, "Location update started ..............: ");
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && LocationServices.FusedLocationApi != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.v(TAG, "Location update stopped .......................");
            mGoogleApiClient.disconnect();
        }
    }


}