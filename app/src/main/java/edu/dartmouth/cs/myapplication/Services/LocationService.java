package edu.dartmouth.cs.myapplication.Services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import edu.dartmouth.cs.myapplication.MapsActivity;
import edu.dartmouth.cs.myapplication.R;

//Aaron Svendsen
//service to get location updates

public class LocationService extends Service {

    //debug tag
    private static final String TAG="Service";

    //keys
    public static final String LOC_KEY = "loc";
    public static final int NEW_LOCATION = 1;
    public static final String ACTION_KEY = "new location";
    private static final String CHANNEL_ID = "ID";

    private NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        //Intent brings app to foreground
        Intent touch = new Intent(this, MapsActivity.class);
        touch.setAction(Intent.ACTION_MAIN);
        touch.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, touch, 0);

        //set up notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Tracking Location")
                .setContentText("MyRuns tracking Location")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("MyRuns tracking Location"))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

         notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "channel";
        String description = "myruns channel";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);
        // Register the channel with the system

        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, mBuilder.build());

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        LocationListener locationListener=setUpListeners();
        getLocation(locationListener);
        Log.d(TAG,"setup location listener");
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(0);
        super.onDestroy();
    }

    //get location updates
    private LocationListener setUpListeners() {
        return new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged");
                updateWithNewLocation(location);
            }

            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled");
            }

            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled");
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                Log.d(TAG, "onStatusChanged");
            }
        };
    }

    //sends new location
    private void updateWithNewLocation(Location location) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(LOC_KEY,location);
        intent.putExtra(ACTION_KEY,NEW_LOCATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    //set up criteria
    public void getLocation(LocationListener locationListener) {
        LocationManager locationManager;
        //String svcName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        String provider;
        if (locationManager != null) {
            provider = locationManager.getBestProvider(criteria, true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                Location l = locationManager.getLastKnownLocation(provider);

                updateWithNewLocation(l);

                locationManager.requestLocationUpdates(provider, 2000, 0,
                        locationListener);
            }
        }
    }
}
